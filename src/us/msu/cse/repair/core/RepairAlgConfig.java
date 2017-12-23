package us.msu.cse.repair.core;

import java.util.HashMap;

import jmetal.operators.crossover.Crossover;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.selection.Selection;
import jmetal.operators.selection.SelectionFactory;
import us.msu.cse.repair.algorithms.arja.Arja;
import us.msu.cse.repair.algorithms.arja.ArjaSingle;
import us.msu.cse.repair.algorithms.genprog.GenProg;
import us.msu.cse.repair.algorithms.kali.Kali;
import us.msu.cse.repair.algorithms.kali.KaliAlg;
import us.msu.cse.repair.algorithms.rsrepair.RSRepair;
import us.msu.cse.repair.ec.operators.crossover.ExtendedCrossoverFactory;
import us.msu.cse.repair.ec.operators.mutation.ExtendedMutationFactory;
import us.msu.cse.repair.ec.problems.ArjaProblem;
import us.msu.cse.repair.ec.problems.GenProgProblem;

public class RepairAlgConfig {

	public static AbstractRepairAlgorithm getRepairAlg(String algName, HashMap<String, Object> parameters)
			throws Exception {
		if (algName.equalsIgnoreCase("Arja"))
			return getArjaAlg(parameters);
		else if (algName.equalsIgnoreCase("ArjaSingle"))
			return getArjaSingleAlg(parameters);
		else if (algName.equalsIgnoreCase("ArjaRandom"))
			return getArjaRandomAlg(parameters);
		else if (algName.equalsIgnoreCase("GenProg"))
			return getGenProgAlg(parameters);
		else if (algName.equalsIgnoreCase("RSRepair"))
			return getRSRepairAlg(parameters);
		else if (algName.equalsIgnoreCase("ArjaVarMatch"))
			return getArjaVarMatchAlg(parameters);
		else if (algName.equalsIgnoreCase("ArjaMethodMatch"))
			return getArjaMethodMatchAlg(parameters);
		else if (algName.equalsIgnoreCase("ArjaVMMatch"))
			return getArjaVMMatchAlg(parameters);
		else if (algName.equalsIgnoreCase("Kali"))
			return getKaliAlg(parameters);
		else
			throw new Exception(algName + " does not exist!");
	}

	static AbstractRepairAlgorithm getGenProgAlg(HashMap<String, Object> parameters) throws Exception {
		parameters.put("ingredientFilterRule", false);
		parameters.put("manipulationFilterRule", false);
		parameters.put("ingredientScreenerName", "Simple");
		GenProgProblem problem = new GenProgProblem(parameters);

		AbstractRepairAlgorithm repairAlg = new GenProg(problem);

		repairAlg.setInputParameter("populationSize", 40);
		repairAlg.setInputParameter("maxEvaluations", 2000);

		Crossover crossover;
		Mutation mutation;
		Selection selection;

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0);
		crossover = ExtendedCrossoverFactory.getCrossoverOperator("GenProgSinglePointCrossover", parameters);

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 0.06);
		mutation = ExtendedMutationFactory.getMutationOperator("GenProgMutation", parameters);

		// Selection Operator
		parameters = null;
		selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters);

		// Add the operators to the algorithm
		repairAlg.addOperator("crossover", crossover);
		repairAlg.addOperator("mutation", mutation);
		repairAlg.addOperator("selection", selection);
		return repairAlg;
	}

	static AbstractRepairAlgorithm getRSRepairAlg(HashMap<String, Object> parameters) throws Exception {
		parameters.put("ingredientFilterRule", false);
		parameters.put("manipulationFilterRule", false);
		parameters.put("ingredientScreenerName", "Simple");
		GenProgProblem problem = new GenProgProblem(parameters);

		AbstractRepairAlgorithm repairAlg = new RSRepair(problem);
		repairAlg.setInputParameter("maxEvaluations", 2000);

		Mutation mutation;
		parameters = new HashMap<String, Object>();
		parameters.put("probability", 0.06);
		mutation = ExtendedMutationFactory.getMutationOperator("GenProgMutation", parameters);

		// Add the operators to the algorithm
		repairAlg.addOperator("mutation", mutation);
		return repairAlg;
	}

	static AbstractRepairAlgorithm getArjaAlg(HashMap<String, Object> parameters) throws Exception {
		ArjaProblem problem = new ArjaProblem(parameters);
		AbstractRepairAlgorithm repairAlg = new Arja(problem);

		repairAlg.setInputParameter("populationSize", 40);
		repairAlg.setInputParameter("maxEvaluations", 2000);

		parameters = new HashMap<String, Object>();

		Crossover crossover;
		Mutation mutation;
		Selection selection;

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0);
		crossover = ExtendedCrossoverFactory.getCrossoverOperator("HUXSinglePointCrossover", parameters);

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0 / problem.getNumberOfModificationPoints());
		mutation = ExtendedMutationFactory.getMutationOperator("BitFilpUniformMutation", parameters);

		// Selection Operator
		parameters = null;
		selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);

		// Add the operators to the algorithm
		repairAlg.addOperator("crossover", crossover);
		repairAlg.addOperator("mutation", mutation);
		repairAlg.addOperator("selection", selection);

		return repairAlg;
	}

	static AbstractRepairAlgorithm getArjaRandomAlg(HashMap<String, Object> parameters) throws Exception {
		ArjaProblem problem = new ArjaProblem(parameters);
		AbstractRepairAlgorithm repairAlg = new Arja(problem);

		repairAlg.setInputParameter("populationSize", 40);
		repairAlg.setInputParameter("maxEvaluations", 2000);

		parameters = new HashMap<String, Object>();

		Crossover crossover;
		Mutation mutation;
		Selection selection;

		parameters = null;
		crossover = ExtendedCrossoverFactory.getCrossoverOperator("RandomWithOutCrossover", parameters);

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 0.0);
		mutation = ExtendedMutationFactory.getMutationOperator("BitFilpUniformMutation", parameters);

		// Selection Operator
		parameters = null;
		selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);

		// Add the operators to the algorithm
		repairAlg.addOperator("crossover", crossover);
		repairAlg.addOperator("mutation", mutation);
		repairAlg.addOperator("selection", selection);

		return repairAlg;
	}

	static AbstractRepairAlgorithm getArjaSingleAlg(HashMap<String, Object> parameters) throws Exception {
		parameters.put("numberOfObjectives", 1);
		ArjaProblem problem = new ArjaProblem(parameters);
		AbstractRepairAlgorithm repairAlg = new ArjaSingle(problem);

		repairAlg.setInputParameter("populationSize", 40);
		repairAlg.setInputParameter("maxEvaluations", 2000);

		parameters = new HashMap<String, Object>();

		Crossover crossover;
		Mutation mutation;
		Selection selection;

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0);
		crossover = ExtendedCrossoverFactory.getCrossoverOperator("HUXSinglePointCrossover", parameters);

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0 / problem.getNumberOfModificationPoints());
		mutation = ExtendedMutationFactory.getMutationOperator("BitFilpUniformMutation", parameters);

		// Selection Operator
		parameters = null;
		selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters);

		// Add the operators to the algorithm
		repairAlg.addOperator("crossover", crossover);
		repairAlg.addOperator("mutation", mutation);
		repairAlg.addOperator("selection", selection);

		return repairAlg;
	}

	static AbstractRepairAlgorithm getArjaVarMatchAlg(HashMap<String, Object> parameters) throws Exception {
		parameters.put("ingredientScreenerName", "VarTypeMatch");

		ArjaProblem problem = new ArjaProblem(parameters);
		AbstractRepairAlgorithm repairAlg = new Arja(problem);

		repairAlg.setInputParameter("populationSize", 40);
		repairAlg.setInputParameter("maxEvaluations", 2000);

		parameters = new HashMap<String, Object>();

		Crossover crossover;
		Mutation mutation;
		Selection selection;

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0);
		crossover = ExtendedCrossoverFactory.getCrossoverOperator("HUXSinglePointCrossover", parameters);

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0 / problem.getNumberOfModificationPoints());
		mutation = ExtendedMutationFactory.getMutationOperator("BitFilpUniformMutation", parameters);

		// Selection Operator
		parameters = null;
		selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);

		// Add the operators to the algorithm
		repairAlg.addOperator("crossover", crossover);
		repairAlg.addOperator("mutation", mutation);
		repairAlg.addOperator("selection", selection);

		return repairAlg;
	}

	static AbstractRepairAlgorithm getArjaMethodMatchAlg(HashMap<String, Object> parameters) throws Exception {
		parameters.put("ingredientScreenerName", "MethodTypeMatch");

		ArjaProblem problem = new ArjaProblem(parameters);
		AbstractRepairAlgorithm repairAlg = new Arja(problem);

		repairAlg.setInputParameter("populationSize", 40);
		repairAlg.setInputParameter("maxEvaluations", 2000);

		parameters = new HashMap<String, Object>();

		Crossover crossover;
		Mutation mutation;
		Selection selection;

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0);
		crossover = ExtendedCrossoverFactory.getCrossoverOperator("HUXSinglePointCrossover", parameters);

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0 / problem.getNumberOfModificationPoints());
		mutation = ExtendedMutationFactory.getMutationOperator("BitFilpUniformMutation", parameters);

		// Selection Operator
		parameters = null;
		selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);

		// Add the operators to the algorithm
		repairAlg.addOperator("crossover", crossover);
		repairAlg.addOperator("mutation", mutation);
		repairAlg.addOperator("selection", selection);

		return repairAlg;
	}

	static AbstractRepairAlgorithm getArjaVMMatchAlg(HashMap<String, Object> parameters) throws Exception {
		parameters.put("ingredientScreenerName", "VMTypeMatch");

		ArjaProblem problem = new ArjaProblem(parameters);
		AbstractRepairAlgorithm repairAlg = new Arja(problem);

		repairAlg.setInputParameter("populationSize", 40);
		repairAlg.setInputParameter("maxEvaluations", 2000);

		parameters = new HashMap<String, Object>();

		Crossover crossover;
		Mutation mutation;
		Selection selection;

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0);
		crossover = ExtendedCrossoverFactory.getCrossoverOperator("HUXSinglePointCrossover", parameters);

		parameters = new HashMap<String, Object>();
		parameters.put("probability", 1.0 / problem.getNumberOfModificationPoints());
		mutation = ExtendedMutationFactory.getMutationOperator("BitFilpUniformMutation", parameters);

		// Selection Operator
		parameters = null;
		selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);

		// Add the operators to the algorithm
		repairAlg.addOperator("crossover", crossover);
		repairAlg.addOperator("mutation", mutation);
		repairAlg.addOperator("selection", selection);

		return repairAlg;
	}

	static AbstractRepairAlgorithm getKaliAlg(HashMap<String, Object> parameters) throws Exception {
		Kali problem = new Kali(parameters);
		AbstractRepairAlgorithm repairAlg = new KaliAlg(problem);

		return repairAlg;
	}

}
