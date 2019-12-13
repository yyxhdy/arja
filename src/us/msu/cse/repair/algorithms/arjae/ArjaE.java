package us.msu.cse.repair.algorithms.arjae;

import us.msu.cse.repair.core.AbstractRepairAlgorithm;

import us.msu.cse.repair.ec.algorithms.ndNSGAII;
import us.msu.cse.repair.ec.problems.ArjaEProblem;

public class ArjaE extends AbstractRepairAlgorithm {
	public ArjaE(ArjaEProblem problem) throws Exception {
		algorithm = new ndNSGAII(problem);
	}
}
