package us.msu.cse.repair;

import java.util.HashMap;

import jmetal.operators.mutation.Mutation;
import us.msu.cse.repair.algorithms.rsrepair.RSRepair;
import us.msu.cse.repair.core.AbstractRepairAlgorithm;
import us.msu.cse.repair.ec.operators.mutation.ExtendedMutationFactory;
import us.msu.cse.repair.ec.problems.GenProgProblem;

public class RSRepairMain {
	public static void main(String args[]) throws Exception {
		HashMap<String, String> parameterStrs = Interpreter.getParameterStrings(args);
		HashMap<String, Object> parameters = Interpreter.getBasicParameterSetting(parameterStrs);
		

		parameters.put("ingredientFilterRule", false);
		parameters.put("manipulationFilterRule", false);
		parameters.put("ingredientScreenerName", "Simple");
				
		int populationSize = 40;
		int maxGenerations = 50;
		
		String populationSizeS = parameterStrs.get("populationSize");
		if (populationSizeS != null)
			populationSize = Integer.parseInt(populationSizeS);
		
		String maxGenerationsS = parameterStrs.get("maxGenerations");
		if (maxGenerationsS != null)
			maxGenerations = Integer.parseInt(maxGenerationsS);
		
		GenProgProblem problem = new GenProgProblem(parameters);
		AbstractRepairAlgorithm repairAlg = new RSRepair(problem);
		repairAlg.setInputParameter("maxEvaluations", populationSize * maxGenerations);

		Mutation mutation;
		parameters = new HashMap<String, Object>();
		parameters.put("probability", 0.06);
		mutation = ExtendedMutationFactory.getMutationOperator("GenProgMutation", parameters);

		// Add the operators to the algorithm
		repairAlg.addOperator("mutation", mutation);
		
		repairAlg.execute();
	}
}
