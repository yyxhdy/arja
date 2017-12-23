package us.msu.cse.repair.ec.operators.crossover;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Solution;
import jmetal.operators.crossover.Crossover;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import us.msu.cse.repair.ec.representation.GenProgSolutionType;
import us.msu.cse.repair.ec.variable.Edits;

public class GenProgSinglePointCrossover extends Crossover {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final List<?> VALID_TYPES = Arrays.asList(GenProgSolutionType.class);

	private Double crossoverProbability_ = null;

	public GenProgSinglePointCrossover(HashMap<String, Object> parameters) {
		super(parameters);
		if (parameters.get("probability") != null)
			crossoverProbability_ = (Double) parameters.get("probability");
	} // SinglePointCrossover

	public Solution[] doCrossover(double probability, Solution parent1, Solution parent2) throws JMException {
		Solution[] offSpring = new Solution[2];
		offSpring[0] = new Solution(parent1);
		offSpring[1] = new Solution(parent2);

		if (PseudoRandom.randDouble() < probability) {
			List<Integer> p1_locList = ((Edits) parent1.getDecisionVariables()[0]).getLocList();
			List<Integer> p1_opList = ((Edits) parent1.getDecisionVariables()[0]).getOpList();
			List<Integer> p1_ingredList = ((Edits) parent1.getDecisionVariables()[0]).getIngredList();

			List<Integer> p2_locList = ((Edits) parent2.getDecisionVariables()[0]).getLocList();
			List<Integer> p2_opList = ((Edits) parent2.getDecisionVariables()[0]).getOpList();
			List<Integer> p2_ingredList = ((Edits) parent2.getDecisionVariables()[0]).getIngredList();

			List<Integer> c1_locList = ((Edits) offSpring[0].getDecisionVariables()[0]).getLocList();
			List<Integer> c1_opList = ((Edits) offSpring[0].getDecisionVariables()[0]).getOpList();
			List<Integer> c1_ingredList = ((Edits) offSpring[0].getDecisionVariables()[0]).getIngredList();

			List<Integer> c2_locList = ((Edits) offSpring[1].getDecisionVariables()[0]).getLocList();
			List<Integer> c2_opList = ((Edits) offSpring[1].getDecisionVariables()[0]).getOpList();
			List<Integer> c2_ingredList = ((Edits) offSpring[1].getDecisionVariables()[0]).getIngredList();

			int cut1 = PseudoRandom.randInt(0, p1_locList.size() - 1);
			int cut2 = PseudoRandom.randInt(0, p2_locList.size() - 1);

			swap(p1_locList, p2_locList, c1_locList, c2_locList, cut1, cut2);
			swap(p1_opList, p2_opList, c1_opList, c2_opList, cut1, cut2);
			swap(p1_ingredList, p2_ingredList, c1_ingredList, c2_ingredList, cut1, cut2);
		}

		return offSpring;
	}

	private void swap(List<Integer> p1List, List<Integer> p2List, List<Integer> c1List, List<Integer> c2List, int cut1,
			int cut2) {
		for (int i = c1List.size() - 1; i >= cut1; i--)
			c1List.remove(c1List.size() - 1);
		for (int i = c2List.size() - 1; i >= cut2; i--)
			c2List.remove(c2List.size() - 1);

		for (int i = cut2; i < p2List.size(); i++)
			c1List.add(p2List.get(i));
		for (int i = cut1; i < p1List.size(); i++)
			c2List.add(p1List.get(i));
	}

	@Override
	public Object execute(Object object) throws JMException {
		Solution[] parents = (Solution[]) object;

		if (!(VALID_TYPES.contains(parents[0].getType().getClass())
				&& VALID_TYPES.contains(parents[1].getType().getClass()))) {

			Configuration.logger_.severe("GenProgSinglePointCrossover.execute: the solutions "
					+ "are not of the right type. The type should be 'GenProgSolutionType', but " + parents[0].getType()
					+ " and " + parents[1].getType() + " are obtained");

			Class<?> cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if

		if (parents.length < 2) {
			Configuration.logger_.severe("GenProgSinglePointCrossover.execute: operator " + "needs two parents");
			Class<?> cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		}

		Solution[] offSpring;
		offSpring = doCrossover(crossoverProbability_, parents[0], parents[1]);

		// -> Update the offSpring solutions
		for (int i = 0; i < offSpring.length; i++) {
			offSpring[i].setCrowdingDistance(0.0);
			offSpring[i].setRank(0);
		}
		return offSpring;
	} // execute
}
