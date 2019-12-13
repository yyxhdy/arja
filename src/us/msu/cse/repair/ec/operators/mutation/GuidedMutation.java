package us.msu.cse.repair.ec.operators.mutation;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import jmetal.core.Solution;
import jmetal.encodings.variable.ArrayInt;
import jmetal.encodings.variable.Binary;
import jmetal.operators.mutation.Mutation;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import us.msu.cse.repair.core.parser.ExtendedModificationPoint;
import us.msu.cse.repair.ec.representation.ArrayIntAndBinarySolutionType2;

public class GuidedMutation extends Mutation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final List<?> VALID_TYPES = Arrays.asList(
			ArrayIntAndBinarySolutionType2.class);

	
	private Double mutationProbability_ = null;
	
	private List<ExtendedModificationPoint> modificationPoints_ = null;
	
	private double[] ratios;
	
	@SuppressWarnings("unchecked")
	public GuidedMutation(HashMap<String, Object> parameters) {
		super(parameters);
		// TODO Auto-generated constructor stub
		if (parameters.get("probability") != null)
			mutationProbability_ = (Double) parameters.get("probability");
		if (parameters.get("modificationPoints") != null)
			modificationPoints_ = (List<ExtendedModificationPoint>) parameters.get("modificationPoints");
		
		ratios = new double[modificationPoints_.size()];
		double sum = 0;
		for (int i = 0; i < ratios.length; i++) {
			double susp = modificationPoints_.get(i).getSuspValue();
			ratios[i] = susp + (i > 0 ? ratios[i - 1] : 0);
			sum += susp;
		}
      
		for (int i = 0; i < ratios.length; i++)
			ratios[i] /= sum;
		
	}

	public void doMutation(double mutationProbability, List<ExtendedModificationPoint> modificationPoints, Solution solution)
			throws JMException {
		ArrayInt var0 = (ArrayInt) solution.getDecisionVariables()[0];
		Binary var1 = (Binary) solution.getDecisionVariables()[1];

		int size = modificationPoints.size();
		int k = getMutatingLocation();
		
		double prob = PseudoRandom.randDouble();
			
		
		if (prob < mutationProbability_) {
			var1.bits_.flip(k);
			
			int lp = (int) var0.getLowerBound(k);
			int up = (int) var0.getUpperBound(k);
			int value = PseudoRandom.randInt(lp, up);
			var0.setValue(k, value);
			
			lp = (int) var0.getLowerBound(k + size);
			up = (int) var0.getUpperBound(k + size);
			value = PseudoRandom.randInt(lp, up);
			var0.setValue(k + size, value);
			
			lp = (int) var0.getLowerBound(k + 2 * size);
			up = (int) var0.getUpperBound(k + 2 * size);
			value = PseudoRandom.randInt(lp, up);
			var0.setValue(k + 2 * size, value);
		}
		else {
			int u = PseudoRandom.randInt(0, 2);
			
			if (u == 0) 
				var1.bits_.flip(k);
			else if (u == 1) {
				int lp = (int) var0.getLowerBound(k);
				int up = (int) var0.getUpperBound(k);
				int value = PseudoRandom.randInt(lp, up);
				var0.setValue(k, value);
			}
			else if (u == 2) {
				int r = k + size;
				if (var0.getValue(k) == 1)
					r = k + 2 * size;
				int lp = (int) var0.getLowerBound(r);
				int up = (int) var0.getUpperBound(r);
				int value = PseudoRandom.randInt(lp, up);
				var0.setValue(r, value);
			}
		}
		
		
	}
	
	int getMutatingLocation() {
		double rnd = PseudoRandom.randDouble();
		
		for (int i = 0; i < ratios.length; i++) {
			if (rnd < ratios[i])
				return i;
		}
		
		return 0;
	}
	
	
	@Override
	public Object execute(Object object) throws JMException {
		Solution solution = (Solution) object;

		if (!VALID_TYPES.contains(solution.getType().getClass())) {
			Configuration.logger_.severe("GuidedMutation.execute: the solution "
					+ "is not of the right type. The type should be 'ArrayIntAndBinarySolution2'," + "but "
					+ solution.getType() + " is obtained");

			Class<?> cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if

		doMutation(mutationProbability_, modificationPoints_, solution);
		return solution;
	} // execute

}
