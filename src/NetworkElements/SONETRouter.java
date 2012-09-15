package NetworkElements;

import DataTypes.*;
import java.util.*;

public class SONETRouter extends SONETRouterTA{
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
		boolean isDestinationFrequency = this.destinationFrequencies.containsValue(new Integer(wavelength));
		
		if (isSelfDropFrequency){ // Take of the line if the given wavelength is contained.
			if (isDestinationFrequency){
				System.out.format("SONETRouter.receiveFrame(): isSelfDropFrequency and isDestinationFrequency are true!\n");
				this.sink(frame, wavelength);
			}else{
				// TODO
				System.out.format("log: Not destination => forward to who?");
			}
		}else{
			// Forward to all interfaces except incoming interface
			System.out.println("log: Send data through other interfaces.");
			this.sendRingFrame(frame, wavelength, nic);
		}
		
	}
	
	/**
	 * Sends a frame out onto the ring that this SONET router is joined to
	 * @param	frame the frame to be sent
	 * @param	wavelength the wavelength to send the frame on
	 * @param	nic the wavelength this frame originally came from (as we don't want to send it back to the sender)
	 */
	public void sendRingFrame(SONETFrame frame, int wavelength, OpticalNICTA nic){
		// Loop through the interfaces sending the frame on interfaces that are on the ring
		// except the one it was received on. Basically what UPSR does
		for(OpticalNICTA NIC: this.NICs)
			if(NIC.getIsOnRing() && !NIC.equals(nic)){
				// Create a new frame to offset sending the same frame instance.
				SONETFrame newFrame = new SONETFrame(frame.getSPE().clone());
				NIC.sendFrame(newFrame, wavelength);
			}
	}
	

}
