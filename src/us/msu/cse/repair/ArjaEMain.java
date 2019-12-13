package us.msu.cse.repair;

import java.util.HashMap;

import jmetal.operators.crossover.Crossover;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.selection.Selection;
import jmetal.operators.selection.SelectionFactory;
import us.msu.cse.repair.algorithms.arjae.ArjaE;
import us.msu.cse.repair.core.AbstractRepairAlgorithm;
import us.msu.cse.repair.ec.operators.crossover.ExtendedCrossoverFactory;
import us.msu.cse.repair.ec.operators.mutation.ExtendedMutationFactory;
import us.msu.cse.repair.ec.problems.ArjaEProblem;

public class ArjaEMain {
	public static void main(String args[]) throws Exception {
		HashMap<String, String> parameterStrs = Interpreter.getParameterStrings(args);
		HashMap<String, Object> parameters = Interpreter.getBasicParameterSetting(parameterStrs);
		
		parameters.put("ingredientScreenerName", "Direct2");
		parameters.put("testExecutorName", "ExternalTestExecutor2");
		parameters.put("manipulationNames", new String[] { "Replace", "InsertBefore", "Delete" });
	
		if (parameters.get("maxNumberOfModificationPoints") == null)
			parameters.put("maxNumberOfModificationPoints", 60);
		
		String repSimS = parameterStrs.get("repSim");
		if (repSimS != null) {
			double repSim = Double.parseDouble(repSimS);
			parameters.put("repSim", repSim);
		}
		
		String insRelS = parameterStrs.get("insRel");
		if (insRelS != null) {
			double insRel = Double.parseDouble(insRelS);
			parameters.put("insRel", insRel);
		}
		
		int populationSize = 40;
		int maxGenerations = 50;
		int maxTime = 60 * 60 * 1000;
		
		String populationSizeS = parameterStrs.get("populationSize");
		if (populationSizeS != null)
			populationSize = Integer.parseInt(populationSizeS);
		String maxGenerationsS = parameterStrs.get("maxGenerations");
		if (maxGenerationsS != null)
			maxGenerations = Integer.parseInt(maxGenerationsS);
		String maxTimeS = parameterStrs.get("maxTime");
		if (maxTimeS != null)
			maxTime = Integer.parseInt(maxTimeS) * 60 * 1000;
		
		
		ArjaEProblem problem = new ArjaEProblem(parameters);
		AbstractRepairAlgorithm repairAlg = new ArjaE(problem);

		repairAlg.setInputParameter("populationSize", populationSize);
		repairAlg.setInputParameter("maxEvaluations", populationSize * maxGenerations);
		repairAlg.setInputParameter("maxTime", maxTime);

		parameters = new HashMap<String, Object>();

		Crossover crossover;
		Mutation mutation;
		Selection selection;

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0); 
		crossover = ExtendedCrossoverFactory.getCrossoverOperator("PureHUXCrossover2", parameters);
		

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0);
		parameters.put("modificationPoints", problem.getExtendedModificationPoints());
		mutation = ExtendedMutationFactory.getMutationOperator("GuidedMutation", parameters);
		
		// Selection Operator
		parameters = null;
		selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);
		
		
		// Add the operators to the algorithm
		repairAlg.addOperator("crossover", crossover);
		repairAlg.addOperator("mutation", mutation);
		repairAlg.addOperator("selection", selection);
		
		repairAlg.execute();
	}
}
