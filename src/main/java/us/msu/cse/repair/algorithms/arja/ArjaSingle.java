package us.msu.cse.repair.algorithms.arja;

import us.msu.cse.repair.core.AbstractRepairAlgorithm;
import us.msu.cse.repair.ec.algorithms.GA;
import us.msu.cse.repair.ec.problems.ArjaProblem;

public class ArjaSingle extends AbstractRepairAlgorithm {
	public ArjaSingle(ArjaProblem problem) throws Exception {
		algorithm = new GA(problem);
	}
}
