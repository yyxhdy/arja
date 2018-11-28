package us.msu.cse.repair.algorithms.kali;

import us.msu.cse.repair.core.AbstractRepairAlgorithm;

public class KaliAlg extends AbstractRepairAlgorithm {

	public KaliAlg(Kali problem) throws Exception {
		algorithm = new KaliAlgInterface(problem);
	}
}
