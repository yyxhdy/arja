package us.msu.cse.repair.ec.operators.mutation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import us.msu.cse.repair.ec.representation.ArrayIntAndBinarySolutionType;
import jmetal.core.Solution;
import jmetal.encodings.variable.ArrayInt;
import jmetal.encodings.variable.Binary;
import jmetal.operators.mutation.Mutation;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

public class BitFilpUniformMutation extends Mutation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final List<?> VALID_TYPES = Arrays.asList(ArrayIntAndBinarySolutionType.class);

	private Double mutationProbability_ = null;

	public BitFilpUniformMutation(HashMap<String, Object> parameters) {
		super(parameters);
		if (parameters.get("probability") != null)
			mutationProbability_ = (Double) parameters.get("probability");
	} // BitFlipMutation

	public void doMutation(double probability, Solution solution) throws JMException {
		ArrayInt var0 = (ArrayInt) solution.getDecisionVariables()[0];
		for (int i = 0; i < var0.array_.length; i++) {
			if (PseudoRandom.randDouble() < probability) {
				int lp = (int) var0.getLowerBound(i);
				int up = (int) var0.getUpperBound(i);
				int value = PseudoRandom.randInt(lp, up);
				var0.setValue(i, value);
			} // if
		}

		Binary var1 = (Binary) solution.getDecisionVariables()[1];
		for (int j = 0; j < var1.getNumberOfBits(); j++) {
			if (PseudoRandom.randDouble() < probability) {
				var1.bits_.flip(j);
			}
		}

	} // doMutation

	@Override
	public Object execute(Object object) throws JMException {
		Solution solution = (Solution) object;

		if (!VALID_TYPES.contains(solution.getType().getClass())) {
			Configuration.logger_.severe("BitFilpUniformMutation.execute: the solution "
					+ "is not of the right type. The type should be 'ArrayIntAndBinarySolution'," + "but "
					+ solution.getType() + " is obtained");

			Class<?> cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if

		doMutation(mutationProbability_, solution);
		return solution;
	} // execute

}
