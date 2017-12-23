package us.msu.cse.repair.ec.representation;

import jmetal.core.Problem;
import jmetal.core.SolutionType;
import jmetal.core.Variable;
import us.msu.cse.repair.ec.variable.Edits;

public class GenProgSolutionType extends SolutionType {
	private final int numberOfLocations;
	private final double[] prob;

	public GenProgSolutionType(Problem problem, int numberOfLocations, double[] prob) {
		super(problem);
		this.numberOfLocations = numberOfLocations;
		this.prob = prob;
		// TODO Auto-generated constructor stub
	}

	@Override
	public Variable[] createVariables() throws ClassNotFoundException {
		// TODO Auto-generated method stub
		Variable[] variables = new Variable[1];
		variables[0] = new Edits(numberOfLocations, prob, problem_);
		return variables;
	}

}
