package us.msu.cse.repair.algorithms.genprog;

import us.msu.cse.repair.core.AbstractRepairAlgorithm;
import us.msu.cse.repair.ec.algorithms.GenProgGA;
import us.msu.cse.repair.ec.problems.GenProgProblem;

public class GenProg extends AbstractRepairAlgorithm {
	public GenProg(GenProgProblem problem) throws Exception {
		algorithm = new GenProgGA(problem);
	}
}
