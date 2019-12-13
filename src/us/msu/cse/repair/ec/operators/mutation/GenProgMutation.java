package us.msu.cse.repair.ec.operators.mutation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Solution;
import jmetal.operators.mutation.Mutation;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import us.msu.cse.repair.ec.representation.GenProgSolutionType;
import us.msu.cse.repair.ec.variable.Edits;

public class GenProgMutation extends Mutation {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final List<?> VALID_TYPES = Arrays.asList(GenProgSolutionType.class);

	private Double mutationProbability_ = null;

	public GenProgMutation(HashMap<String, Object> parameters) {
		super(parameters);
		if (parameters.get("probability") != null)
			mutationProbability_ = (Double) parameters.get("probability");
	} // GenProgMutation

	public void doMutation(double probability, Solution solution) throws JMException {
		Edits edits = (Edits) solution.getDecisionVariables()[0];
		double[] susp = edits.getSusp();

		for (int j = 0; j < edits.getNumberOfLocations(); j++) {
			if (PseudoRandom.randDouble() <= probability && PseudoRandom.randDouble() <= susp[j]) {
				int lowOp = (int) edits.getLowerBound(j);
				int upOp = (int) edits.getUpperBound(j);
				int lowIng = (int) edits.getLowerBound(j + edits.getNumberOfLocations());
				int upIng = (int) edits.getUpperBound(j + edits.getNumberOfLocations());
				int op = PseudoRandom.randInt(lowOp, upOp);
				int ing = PseudoRandom.randInt(lowIng, upIng);
				edits.getLocList().add(j);
				edits.getOpList().add(op);
				edits.getIngredList().add(ing);
			}
		}
	}

	@Override
	public Object execute(Object object) throws JMException {
		Solution solution = (Solution) object;

		if (!VALID_TYPES.contains(solution.getType().getClass())) {
			Configuration.logger_.severe("GenProgMutation.execute: the solution "
					+ "is not of the right type. The type should be 'GenProgSolution'," + "but " + solution.getType()
					+ " is obtained");

			Class<?> cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if

		doMutation(mutationProbability_, solution);
		return solution;
	} // execute
}
