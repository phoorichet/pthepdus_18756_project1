package NetworkElements;

import DataTypes.*;

import java.util.*;

public class SONETRouter extends SONETRouterTA{
	// Debug enabling for more details
	protected Boolean debug = false; 
	
	/**
	 * Construct a new SONET router with a given address
	 * @param	address the address of the new SONET router
	 */
	public SONETRouter(String address){
		super(address);
	}
	
	/**
	 * This method processes a frame when it is received from any location (including being created on this router
	 * from the source method). It either drops the frame from the line, or forwards it around the ring
	 * @param	frame the SONET frame to be processed
	 * @param	wavelength the wavelength the frame was received on
	 * @param	nic the NIC the frame was received on
	 */
	public void receiveFrame(SONETFrame frame, int wavelength, OpticalNICTA nic){
		/**
		If a frame is on the routers drop frequency it takes it off of the line (it might be for that router, or has to be forwarded to somewhere off of the ring)
		If a frame is on the routers drop frequency, and is also the routers destination frequency, the frame is forwarded to the sink(frame, wavelength) method. In real life the sink method would be sending the data to the layer above)
		If a frame is not on the routers drop frequency the frame is forwarded on all interfaces, except the interface the frame was received on (sendRingFrame())
		**/
		
//		for (OpticalNICTA n : this.NICs) {
//			System.out.println(n);
//		}
		
		boolean isSelfDropFrequency = this.dropFrequency.contains(new Integer(wavelength));
		boolean isDestinationFrequency = nic != null ? this.destinationFrequencies.get(nic.getParent().getAddress()) == wavelength : false;
		boolean isKnownDestinationFrequency = this.destinationFrequencies.values().contains(wavelength);
		
		if (isSelfDropFrequency){ // Take of the line if the given wavelength is contained.
			if (isDestinationFrequency){
				if(debug)
					System.out.format(">>SONETRouter[%s]: NIC=%s, Recieved as %s, Working=%s, Protection=%s\n",
						this.getAddress(),
						nic, 
						nic.getSendAsWorkingNIC()==true? "W": "P",
						nic.getInLink().getSource().getWorkingNIC(),
						nic.getInLink().getSource().getProtectionNIC());
				if(nic.getSendAsWorkingNIC())
					this.sink(frame, wavelength);
			}else{
				// Take off the ring or router
				// Do not forward the the packet
				this.takeOff(frame, wavelength);
			}
		}else{
			if (isKnownDestinationFrequency){
				// Forward to all interfaces except incoming interface
				if (debug) System.out.format("\n");
				this.sendRingFrame(frame, wavelength, nic);
			}else{
				System.out.format("SONETRouter.receiveFrame(): Not destination frequency for %s; frame dropped.\n", wavelength);
			}
		}
		
	}
	
	/**
	 * Remove the frame from the router.
	 * @param frame
	 * @param wavelength
	 */
	private void takeOff(SONETFrame frame, int wavelength) {
		//it takes it off of the line (it might be for that router, or has to be forwarded to somewhere off of the ring
		if(debug) System.out.format("SONETRouter.takeOff(): Take off frame %s\n",frame);
	}

	/**
	 * Sends a frame out onto the ring that this SONET router is joined to
	 * @param	frame the frame to be sent
	 * @param	wavelength the wavelength to send the frame on
	 * @param	nic the wavelength this frame originally came from (as we don't want to send it back to the sender)
	 */
	public void sendRingFrame(SONETFrame frame, int wavelength, OpticalNICTA nic){
		
		// Refresh nextHop table
		// The shortestPathList will contain NICs that are consider the shortest path.
		ArrayList<OpticalNICTA> shortestPathList = this.getShortestPaths(wavelength, nic);
		
		// According to the architecture, the maximum number of output is 2: either working 
		// or protection.
		OpticalNICTA outGoingWorkingNIC = null;
		OpticalNICTA outGoingProtectionNIC = null;
		if(shortestPathList.size() >=2){
			outGoingWorkingNIC = shortestPathList.get(0);
			outGoingProtectionNIC = shortestPathList.get(1);
			
			outGoingProtectionNIC.setIsProtection(outGoingWorkingNIC);
			outGoingWorkingNIC.setIsWorking(outGoingProtectionNIC);
		}else if (shortestPathList.size() == 1){
			outGoingWorkingNIC = shortestPathList.get(0);
			outGoingWorkingNIC.setIsWorking(outGoingWorkingNIC);
		}else{
			//System.err.println("There is no path available for sending data.");
		}
		
		if(debug) System.out.format("\tSONETRouter[%s] ShortestPathList=%s\n", this.getAddress(), shortestPathList);
		if(debug) System.out.format("\tSONETRouter[%s] NIC info: Working=%s, Protection=%s\n",this.getAddress(), outGoingWorkingNIC, outGoingProtectionNIC);
		if(debug) System.out.format("\tSONETRouter[%s] Recieved form NIC=%s\n", this.getAddress(), nic);
		
		if (nic == null){// Configured as the first add point
			// Forward to both working and protection paths
			if (outGoingWorkingNIC != null){
				SONETFrame newFrame = new SONETFrame(frame.getSPE().clone());
				if(debug) System.out.format("\tSONETRouter[%s] Sending %s wavelength [%d] via working %s\n",this.address, newFrame, wavelength, outGoingWorkingNIC);
				OtoOLink w = (OtoOLink) outGoingWorkingNIC.getOutLink();
				w.setLinkType(true);
				outGoingWorkingNIC.sendFrame(newFrame, wavelength);
			}
			if (outGoingProtectionNIC != null){
				SONETFrame newFrame = new SONETFrame(frame.getSPE().clone());
				if(debug) System.out.format("\tSONETRouter[%s] Sending %s wavelength [%d] via protection %s\n",this.address, newFrame, wavelength, outGoingProtectionNIC);
				outGoingProtectionNIC.sendFrame(newFrame, wavelength);
			}
		}else{ // Receive frames from another NIC
			// There are two steps here:
			// Step1, if it is inComingWorkingNic, send out via outGoingWorkingNic and vise versa for protectionNic
			// Step2, if it is inComingProtectionNic and outGoingProtectionNic is not working, send via 
			// outGoingWorkingNIC and set outGoingWorkingNIC to be outGoingProtectionNic so that
			// the next hop router will know if it is protection link.
			
			// Step2
			if (outGoingWorkingNIC!=null){ 
				if (outGoingProtectionNIC != null){ // There are always 2 links available.
					// Do Step1
					// Prevent duplicated sending
					boolean sendFlag = false;
					
					// Working
					if (!this.isOutgoingNICParentSameAsInComingNIC(outGoingWorkingNIC, nic)){
						SONETFrame newFrame = new SONETFrame(frame.getSPE().clone());
						if(debug) System.out.format("\tSONETRouter[%s] Sending %s wavelength [%d] via working %s\n",this.address, newFrame, wavelength, outGoingWorkingNIC);
						
						// Set link type based on incoming NIC
						OtoOLink w = (OtoOLink) outGoingWorkingNIC.getOutLink();
						w.setLinkType(nic.getInLink().getSource().getSendAsWorkingNIC());
						outGoingWorkingNIC.sendFrame(newFrame, wavelength);
						sendFlag = true;
					}
					
					// Protection
					if (!sendFlag && !this.isOutgoingNICParentSameAsInComingNIC(outGoingProtectionNIC, nic)){
						SONETFrame newFrame = new SONETFrame(frame.getSPE().clone());
						if(debug) System.out.format("\tSONETRouter[%s] Sending %s wavelength [%d] via protection %s\n",this.address, newFrame, wavelength, outGoingProtectionNIC);
						
						// Set link type based on incoming NIC
						OtoOLink w = (OtoOLink) outGoingProtectionNIC.getOutLink();
						w.setLinkType(nic.getInLink().getSource().getSendAsWorkingNIC());
						outGoingProtectionNIC.sendFrame(newFrame, wavelength);
					}
				}else{ // There are always 1 link available.
					// Do step2
					if (!this.isOutgoingNICParentSameAsInComingNIC(outGoingWorkingNIC, nic)){
						SONETFrame newFrame = new SONETFrame(frame.getSPE().clone());
						if(debug) System.out.format("\tSONETRouter[%s] Sending %s wavelength [%d] via working as protection %s\n",this.address, newFrame, wavelength, outGoingWorkingNIC);
						
						// Set link type based on incoming NIC
						OtoOLink w = (OtoOLink) outGoingWorkingNIC.getOutLink();
						w.setLinkType(nic.getInLink().getSource().getSendAsWorkingNIC());
						outGoingWorkingNIC.sendFrame(newFrame, wavelength);
					}
				}
			}else{ // There is no link available.
				if(debug) System.out.format("\tSONETRouter[%s]: No outgoing link available\n", this.address);
			}

		}
		
		
	}
	
	/**
	 * Find a list of shortest path and order the list based on the hop count as ascending order.
	 * 
	 * @param wavelength : The wavelength on which a frame will be sent 
	 * @param inNIC NIC to lookup adjacent NIC
	 * @return
	 */
	public ArrayList<OpticalNICTA> getShortestPaths(int wavelength, OpticalNICTA inNIC){
		this.buildNexHopTable();
		ArrayList<OpticalNICTA> shortestPathList = new ArrayList<OpticalNICTA>();
		
		// First round, add available next hop
		for(OpticalNICTA outNIC: this.NICs){
			if(outNIC.getIsOnRing() && !outNIC.equals(inNIC) && (!outNIC.getOutLink().linkCut)){
				if (!this.isOutgoingNICParentSameAsInComingNIC(outNIC, inNIC)){
					ArrayList<Integer> nextHopList = this.destinationNextHop.get(wavelength);
					if (nextHopList != null && nextHopList.contains(outNIC.getID())){
						shortestPathList.add(outNIC);
					}
				}
				
			}
		}
		// Second round, add the others. NOTE The largest size of the network is 3.
		for(OpticalNICTA outNIC: this.NICs){
			if(outNIC.getIsOnRing() && !outNIC.equals(inNIC) && (!outNIC.getOutLink().linkCut)){
				if (!this.isOutgoingNICParentSameAsInComingNIC(outNIC, inNIC)){
					ArrayList<Integer> nextHopList = this.destinationNextHop.get(wavelength);
					if (nextHopList != null && nextHopList.contains(outNIC.getID())){
						
					}else{
						shortestPathList.add(outNIC);
					}
				}
				
			}
		}
		
		return shortestPathList;
	}
	
	/**
	 * Create a internal hash map for maintaining mapping between wavelength and NIC ID.
	 */
	public void buildNexHopTable(){
		this.destinationNextHop = new TreeMap<Integer, ArrayList<Integer>>();
		for (Map.Entry<String, Integer> destFrequency : this.destinationFrequencies.entrySet()) {
			String address = destFrequency.getKey();
			Integer wavelength = destFrequency.getValue();
			ArrayList<Integer> nextHop = this.destinationNextHop.get(wavelength) == null? new ArrayList<Integer>() : this.destinationNextHop.get(wavelength);
			for(OpticalNICTA NIC: this.NICs){
				String nextHopAddress = NIC.getOutLink().getDest().getParent().getAddress();
				if (address == nextHopAddress){
					nextHop.add(NIC.getID());
				}
			}
			this.addDestinationHopCount(wavelength, nextHop);
		}
	}

	/**
	 * Prevent a loop to the incoming router by determining if the next hop shares the same address.
	 * @param out
	 * @param in
	 * @return
	 */
	private boolean isOutgoingNICParentSameAsInComingNIC(OpticalNICTA out, OpticalNICTA in){
		if (out==null || in ==null)
			return false;
		return out.getOutLink().getDest().getParent().getAddress() == in.getInLink().getSource().getParent().getAddress();
	}
	
	
	
	
	
}
