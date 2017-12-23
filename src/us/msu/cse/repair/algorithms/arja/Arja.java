package us.msu.cse.repair.algorithms.arja;

import us.msu.cse.repair.core.AbstractRepairAlgorithm;
import us.msu.cse.repair.ec.problems.ArjaProblem;
import jmetal.metaheuristics.nsgaII.NSGAII;

public class Arja extends AbstractRepairAlgorithm {
	public Arja(ArjaProblem problem) throws Exception {
		algorithm = new NSGAII(problem);
	}
}
