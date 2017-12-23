package us.msu.cse.repair.ec.operators.crossover;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.operators.crossover.Crossover;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import us.msu.cse.repair.ec.representation.ArrayIntAndBinarySolutionType;

public class RandomWithOutCrossover extends Crossover {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final List<?> VALID_TYPES = Arrays.asList(ArrayIntAndBinarySolutionType.class);

	public RandomWithOutCrossover(HashMap<String, Object> parameters) {
		super(parameters);
		// TODO Auto-generated constructor stub
	}

	public Solution[] doCrossover(Solution parent1, Solution parent2) throws JMException, ClassNotFoundException {
		Problem problem = parent1.getProblem();
		Solution[] offSpring = new Solution[2];
		offSpring[0] = new Solution(problem);
		offSpring[1] = new Solution(problem);
		return offSpring;
	}

	@Override
	public Object execute(Object object) throws JMException {
		Solution[] parents = (Solution[]) object;

		if (!(VALID_TYPES.contains(parents[0].getType().getClass())
				&& VALID_TYPES.contains(parents[1].getType().getClass()))) {

			Configuration.logger_.severe("RandomWithOutCrossover.execute: the solutions "
					+ "are not of the right type. The type should be 'ArrayIntAndBinary', but " + parents[0].getType()
					+ " and " + parents[1].getType() + " are obtained");

			Class<?> cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if

		if (parents.length < 2) {
			Configuration.logger_.severe("RandomWithOutCrossover.execute: operator " + "needs two parents");
			Class<?> cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		}

		Solution[] offSpring = null;
		try {
			offSpring = doCrossover(parents[0], parents[1]);
			// -> Update the offSpring solutions
			for (int i = 0; i < offSpring.length; i++) {
				offSpring[i].setCrowdingDistance(0.0);
				offSpring[i].setRank(0);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return offSpring;
	} // execute

}
