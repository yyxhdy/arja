package us.msu.cse.repair;

import jmetal.util.JMException;

class Main {
	public static void main(String args[]) throws Exception {
		if (args[0].equalsIgnoreCase("Arja"))
			ArjaMain.main(args);
		else if (args[0].equalsIgnoreCase("GenProg"))
			GenProgMain.main(args);
		else if (args[0].equalsIgnoreCase("RSRepair"))
			RSRepairMain.main(args);
		else if (args[0].equalsIgnoreCase("Kali"))
			KaliMain.main(args);
		else if (args[0].equalsIgnoreCase("-listParameters"))
			ParameterInfoMain.main(args);
		else {
			throw new JMException("The repair apporach " + args[0] + " does not exist!");
		}
	}
}