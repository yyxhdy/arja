package us.msu.cse.repair.ec.algorithms;

import jmetal.core.*;
import jmetal.util.JMException;
import jmetal.util.comparators.ObjectiveComparator;

import java.util.Comparator;

/**
 * Class implementing a generational genetic algorithm
 */
public class GA extends Algorithm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 * Constructor Create a new GGA instance.
	 * 
	 * @param problem
	 *            Problem to solve.
	 */
	public GA(Problem problem) {
		super(problem);
	} // GGA

	/**
	 * Execute the GGA algorithm
	 * 
	 * @throws JMException
	 */
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		int populationSize;
		int maxEvaluations;
		int evaluations;

		SolutionSet population;
		SolutionSet offspringPopulation;

		Operator mutationOperator;
		Operator crossoverOperator;
		Operator selectionOperator;

		@SuppressWarnings("rawtypes")
		Comparator comparator;
		comparator = new ObjectiveComparator(0); // Single objective comparator

		// Read the params
		populationSize = ((Integer) this.getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) this.getInputParameter("maxEvaluations")).intValue();

		// Initialize the variables
		population = new SolutionSet(populationSize);
		offspringPopulation = new SolutionSet(populationSize);

		evaluations = 0;

		// Read the operators
		mutationOperator = this.operators_.get("mutation");
		crossoverOperator = this.operators_.get("crossover");
		selectionOperator = this.operators_.get("selection");

		// Create the initial population
		Solution newIndividual;
		for (int i = 0; i < populationSize; i++) {
			newIndividual = new Solution(problem_);
			problem_.evaluate(newIndividual);
			evaluations++;
			population.add(newIndividual);
		} // for

		while (evaluations < maxEvaluations) {
			for (int i = 0; i < populationSize / 2; i++) {
				// Selection
				Solution[] parents = new Solution[2];

				parents[0] = (Solution) selectionOperator.execute(population);
				parents[1] = (Solution) selectionOperator.execute(population);

				// Crossover
				Solution[] offspring = (Solution[]) crossoverOperator.execute(parents);

				// Mutation
				mutationOperator.execute(offspring[0]);
				mutationOperator.execute(offspring[1]);

				// Evaluation of the new individual
				problem_.evaluate(offspring[0]);
				problem_.evaluate(offspring[1]);

				evaluations += 2;

				offspringPopulation.add(offspring[0]);
				offspringPopulation.add(offspring[1]);
			} // for

			// The offspring population becomes the new current population
			population.clear();
			for (int i = 0; i < populationSize; i++) {
				population.add(offspringPopulation.get(i));
			}
			offspringPopulation.clear();
		} // while

		// Return a population with the best individual
		population.sort(comparator);
		SolutionSet resultPopulation = new SolutionSet(1);
		resultPopulation.add(population.get(0));

		return resultPopulation;
	} // execute
}