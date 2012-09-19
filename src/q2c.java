import DataTypes.SONETFrame;
import DataTypes.SPE;
import NetworkElements.OpticalNICTA;
import NetworkElements.OtoOLink;
import NetworkElements.SONETRouter;


public class q2c {
	public void twoRings(){
		System.out.println("Setting up three routers with BLSR");
		// Create two SONET routers
		SONETRouter router1 = new SONETRouter("00:11:22");
		SONETRouter router2 = new SONETRouter("88:77:66");
		SONETRouter router3 = new SONETRouter("33:44:55");
		
		// tell routers a wavelength to add/drop on (in this case their own frequencies)
		router1.addDropWavelength(1310);
		router2.addDropWavelength(1490);
		router3.addDropWavelength(1550);
		
		// tell router 1 the wavelength each router is add/dropping on
		router1.addDestinationFrequency("00:11:22", 1310);
		router1.addDestinationFrequency("88:77:66", 1490);
		router1.addDestinationFrequency("33:44:55", 1550);
		
		// tell router 2 the wavelength each router is add/dropping on
		router2.addDestinationFrequency("00:11:22", 1310);
		router2.addDestinationFrequency("88:77:66", 1490);
		router2.addDestinationFrequency("33:44:55", 1550);
		
		// tell router 3 the wavelength each router is add/dropping on
		router3.addDestinationFrequency("00:11:22", 1310);
		router3.addDestinationFrequency("88:77:66", 1490);
		router3.addDestinationFrequency("33:44:55", 1550);
		
		// Create an interface for each router
		// 1->2 
		OpticalNICTA nicRouter121 = new OpticalNICTA(router1);
		nicRouter121.setID(121);
		OpticalNICTA nicRouter122 = new OpticalNICTA(router1);
		nicRouter122.setID(122);
		
		// 1=>3
		OpticalNICTA nicRouter131 = new OpticalNICTA(router1);
		nicRouter131.setID(131);
		OpticalNICTA nicRouter132 = new OpticalNICTA(router1);
		nicRouter132.setID(132);
		
		// 2->1
		OpticalNICTA nicRouter211 = new OpticalNICTA(router2);
		nicRouter211.setID(211);
		OpticalNICTA nicRouter212 = new OpticalNICTA(router2);
		nicRouter212.setID(212);
		
		// 2->3
		OpticalNICTA nicRouter231 = new OpticalNICTA(router2);
		nicRouter231.setID(231);
		OpticalNICTA nicRouter232 = new OpticalNICTA(router2);
		nicRouter232.setID(232);
		
		// 3-1
		OpticalNICTA nicRouter311 = new OpticalNICTA(router3);
		nicRouter311.setID(311);
		OpticalNICTA nicRouter312 = new OpticalNICTA(router3);
		nicRouter312.setID(312);
		
		// 3->2
		OpticalNICTA nicRouter321 = new OpticalNICTA(router3);
		nicRouter321.setID(321);
		OpticalNICTA nicRouter322 = new OpticalNICTA(router3);
		nicRouter322.setID(322);
		
		
		
		// Create two-uni directional links between the routers
		OtoOLink OneToTwo1 = new OtoOLink(nicRouter121, nicRouter211);
		OtoOLink OneToTwo2 = new OtoOLink(nicRouter122, nicRouter212);
		
		OtoOLink OneToThree1 = new OtoOLink(nicRouter131, nicRouter311);
		OtoOLink OneToThree2 = new OtoOLink(nicRouter132, nicRouter312);
		
		OtoOLink TwoToOne1 = new OtoOLink(nicRouter211, nicRouter121);
		OtoOLink TwoToOne2 = new OtoOLink(nicRouter212, nicRouter122);
		
		OtoOLink TwoToThree1 = new OtoOLink(nicRouter231, nicRouter321);
		OtoOLink TwoToThree2 = new OtoOLink(nicRouter232, nicRouter322);
		
		OtoOLink ThreeToOne1 = new OtoOLink(nicRouter311, nicRouter131);
		OtoOLink ThreeToOne2 = new OtoOLink(nicRouter312, nicRouter132);
		
		OtoOLink ThreeToTwo1 = new OtoOLink(nicRouter321, nicRouter231);
		OtoOLink ThreeToTwo2 = new OtoOLink(nicRouter322, nicRouter232);
		
		
		// Test link cut
		System.out.format("q2c.twoRings(): %s\n","Case1");
		router1.source(new SONETFrame(new SPE(0)), 1490);
		
		System.out.format("---end----\nbq2c.twoRings(): %s\n","Case2");
		OneToTwo2.cutLink();
		TwoToOne2.cutLink();
		router1.source(new SONETFrame(new SPE(0)), 1490);
		OneToTwo2.uncutLink();
		TwoToOne2.uncutLink();
		
		System.out.format("---end----\nbq2c.twoRings(): %s\n","Case3");
		OneToTwo2.cutLink();
		TwoToOne2.cutLink();
		OneToTwo1.cutLink();
		TwoToOne1.cutLink();
		router1.source(new SONETFrame(new SPE(0)), 1490);
		OneToTwo2.uncutLink();
		TwoToOne2.uncutLink();
		OneToTwo1.uncutLink();
		TwoToOne1.uncutLink();
		
		System.out.format("---end----\nbq2c.twoRings(): %s\n","Case4");
		OneToTwo2.cutLink();
		TwoToOne2.cutLink();
		TwoToThree2.cutLink();
		ThreeToTwo2.cutLink();
		router1.source(new SONETFrame(new SPE(0)), 1490);
		OneToTwo2.uncutLink();
		TwoToOne2.uncutLink();
		TwoToThree2.uncutLink();
		ThreeToTwo2.uncutLink();
		
		System.out.format("---end----\nbq2c.twoRings(): %s\n","Case5");
		OneToTwo2.cutLink();
		TwoToOne2.cutLink();
		TwoToThree1.cutLink();
		ThreeToTwo1.cutLink();
		TwoToThree2.cutLink();
		ThreeToTwo2.cutLink();
		router1.source(new SONETFrame(new SPE(0)), 1490);
		OneToTwo2.uncutLink();
		TwoToOne2.uncutLink();
		TwoToThree1.uncutLink();
		ThreeToTwo1.uncutLink();
		TwoToThree2.uncutLink();
		ThreeToTwo2.uncutLink();
		
		System.out.format("---end----\nbq2c.twoRings(): %s\n","Case6");
		OneToTwo2.cutLink();
		TwoToOne2.cutLink();
		OneToTwo1.cutLink();
		TwoToOne1.cutLink();
		OneToThree1.cutLink();
		ThreeToOne1.cutLink();
		router1.source(new SONETFrame(new SPE(0)), 1490);
		OneToTwo2.uncutLink();
		TwoToOne2.uncutLink();
		OneToTwo1.uncutLink();
		TwoToOne1.uncutLink();
		OneToThree1.uncutLink();
		ThreeToOne1.uncutLink();
		
	}
	
	public static void main(String args[]){
		q2c go = new q2c();
		go.twoRings();
	}
}
