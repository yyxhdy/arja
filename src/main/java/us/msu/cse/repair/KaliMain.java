package us.msu.cse.repair;

import java.util.HashMap;

import us.msu.cse.repair.algorithms.kali.Kali;
import us.msu.cse.repair.algorithms.kali.KaliAlg;
import us.msu.cse.repair.core.AbstractRepairAlgorithm;

public class KaliMain {
	public static void main(String args[]) throws Exception {
		HashMap<String, String> parameterStrs = Interpreter.getParameterStrings(args);
		HashMap<String, Object> parameters = Interpreter.getBasicParameterSetting(parameterStrs);
	
		Kali problem = new Kali(parameters);
		AbstractRepairAlgorithm repairAlg = new KaliAlg(problem);	
		repairAlg.execute();
	}
}
