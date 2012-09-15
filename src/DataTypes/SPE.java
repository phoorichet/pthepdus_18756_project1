package DataTypes;

public class SPE extends SPETA{
	/**
	 * Creates a new SPE, with a virtual pointer set
	 * @param	VTPointer the VT number to be set for this SPE
	 */
	public SPE(int VTPointer){
		super(VTPointer);
	}
	
	/**
	 * Increases the delay that this SPE has encountered during it's travel
	 * @param	delay the additional delay to be added
	 */
	public void addDelay(int delay){
		this.delay += delay;
	}
	
	/**
	 * Returns a clone of this SPE object
	 */
	public SPE clone(){
		// Make sure that all properties are copied.
		SPE clonedSPE = new SPE(this.getVTPointer());
		clonedSPE.addDelay(this.delay);
		return clonedSPE; 
	}
	
	/**
	 * Print details in string format
	 */
	public String toString(){
		return String.format("[SPE=>VTPointer=%d, SPEDelay=%d]", this.getVTPointer(), this.getDelay());
	}
}
