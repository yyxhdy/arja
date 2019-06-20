package us.msu.cse.repair.ec.algorithms;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import us.msu.cse.repair.ec.variable.Edits;

public class GenProgGA extends Algorithm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int populationSize;
	int maxEvaluations;
	int evaluations;

	SolutionSet population;

	Operator mutationOperator;
	Operator crossoverOperator;
	Operator selectionOperator;

	public GenProgGA(Problem problem) {
		super(problem);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		// TODO Auto-generated method stub

		// Read the params
		populationSize = ((Integer) this.getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) this.getInputParameter("maxEvaluations")).intValue();

		// Initialize the variables
		evaluations = 0;

		// Read the operators
		mutationOperator = this.operators_.get("mutation");
		crossoverOperator = this.operators_.get("crossover");
		selectionOperator = this.operators_.get("selection");

		initPopulation();

		while (evaluations < maxEvaluations) {
			SolutionSet matingPopulation = getMatingPopulation();
			if (matingPopulation == null)
				return new SolutionSet(0);

			population.clear();

			for (int i = 0; i < populationSize / 4; i++) {
				Solution[] parents = new Solution[2];

				parents[0] = (Solution) selectionOperator.execute(matingPopulation);
				parents[1] = (Solution) selectionOperator.execute(matingPopulation);

				// Crossover
				Solution[] offspring = (Solution[]) crossoverOperator.execute(parents);

				population.add(parents[0]);
				population.add(parents[1]);
				population.add(offspring[0]);
				population.add(offspring[1]);
			}

			for (int i = 0; i < populationSize; i++) {
				Solution individual = population.get(i);
				mutationOperator.execute(individual);
				problem_.evaluate(individual);
				evaluations++;
			}
		}

		return population;
	}

	private SolutionSet getMatingPopulation() throws ClassNotFoundException, JMException {
		while (true && evaluations < maxEvaluations) {
			SolutionSet matingPopulation = new SolutionSet(populationSize);
			for (int i = 0; i < populationSize; i++) {
				if (population.get(i).getObjective(0) != Double.MAX_VALUE)
					matingPopulation.add(population.get(i));
			}

			if (matingPopulation.size() > 0)
				return matingPopulation;
			else
				initPopulation();
		}
		return null;
	}

	private void initPopulation() throws ClassNotFoundException, JMException {
		// Create the initial population
		population = new SolutionSet(populationSize);
		Solution newIndividual;
		for (int i = 0; i < populationSize; i++) {
			newIndividual = new Solution(problem_);
			mutation(newIndividual, mutationOperator);
			problem_.evaluate(newIndividual);
			evaluations++;
			population.add(newIndividual);

		} // for
	}

	private void mutation(Solution solution, Operator mutationOperator) throws JMException {
		do {
			mutationOperator.execute(solution);
		} while (((Edits) solution.getDecisionVariables()[0]).getNumberOfEdits() == 0);
	}
}
