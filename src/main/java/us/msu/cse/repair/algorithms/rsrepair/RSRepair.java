package us.msu.cse.repair.algorithms.rsrepair;

import us.msu.cse.repair.core.AbstractRepairAlgorithm;
import us.msu.cse.repair.core.AbstractRepairProblem;
import us.msu.cse.repair.ec.algorithms.RSRepairRandomSearch;

public class RSRepair extends AbstractRepairAlgorithm {
	public RSRepair(AbstractRepairProblem problem) throws Exception {
		algorithm = new RSRepairRandomSearch(problem);
	}
}
