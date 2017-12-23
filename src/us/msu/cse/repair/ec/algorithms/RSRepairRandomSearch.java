package us.msu.cse.repair.ec.algorithms;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import us.msu.cse.repair.ec.variable.Edits;

public class RSRepairRandomSearch extends Algorithm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RSRepairRandomSearch(Problem problem) {
		super(problem);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		// TODO Auto-generated method stub
		int maxEvaluations = ((Integer) this.getInputParameter("maxEvaluations")).intValue();
		Operator mutationOperator = this.operators_.get("mutation");
		SolutionSet population = new SolutionSet(maxEvaluations);

		Solution newIndividual;
		for (int i = 0; i < maxEvaluations; i++) {
			newIndividual = new Solution(problem_);
			mutation(newIndividual, mutationOperator);
			problem_.evaluate(newIndividual);

			if (newIndividual.getObjective(0) == 0)
				population.add(newIndividual);

		} // for

		return population;
	}

	private void mutation(Solution solution, Operator mutationOperator) throws JMException {
		do {
			mutationOperator.execute(solution);
		} while (((Edits) solution.getDecisionVariables()[0]).getNumberOfEdits() == 0);
	}

}
