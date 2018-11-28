package us.msu.cse.repair.ec.operators.crossover;

import java.util.HashMap;

import jmetal.operators.crossover.Crossover;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.util.JMException;

public class ExtendedCrossoverFactory {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Crossover getCrossoverOperator(String name, HashMap parameters) throws JMException {
		if (name.equalsIgnoreCase("HUXSinglePointCrossover"))
			return new HUXSinglePointCrossover(parameters);
		else if (name.equalsIgnoreCase("PureSinglePointCrossover"))
			return new PureSinglePointCrossover(parameters);
		else if (name.equalsIgnoreCase("GenProgSinglePointCrossover"))
			return new GenProgSinglePointCrossover(parameters);
		else if (name.equalsIgnoreCase("RandomWithOutCrossover"))
			return new RandomWithOutCrossover(parameters);
		else
			return CrossoverFactory.getCrossoverOperator(name, parameters);
	}
}
