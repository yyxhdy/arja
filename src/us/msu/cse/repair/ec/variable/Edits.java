package us.msu.cse.repair.ec.variable;

import java.util.ArrayList;
import java.util.List;

import jmetal.core.Problem;
import jmetal.core.Variable;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

public class Edits extends Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Problem problem;

	private List<Integer> opList;
	private List<Integer> locList;
	private List<Integer> ingredList;
	private double[] susp;

	private int numberOfLocations;

	private int[] lowerBounds;
	private int[] upperBounds;

	public Edits(int numberOfLocations, double[] susp, Problem problem) {
		this.problem = problem;
		this.opList = new ArrayList<Integer>();
		this.locList = new ArrayList<Integer>();
		this.ingredList = new ArrayList<Integer>();
		this.numberOfLocations = numberOfLocations;
		this.susp = susp;

		lowerBounds = new int[2 * this.numberOfLocations];
		upperBounds = new int[2 * this.numberOfLocations];

		for (int i = 0; i < 2 * this.numberOfLocations; i++) {
			lowerBounds[i] = (int) this.problem.getLowerLimit(i);
			upperBounds[i] = (int) this.problem.getUpperLimit(i);
		}

	} // Constructor

	public Edits(int numberOfLocations, double susp[], int num, Problem problem) {
		this(numberOfLocations, susp, problem);
		for (int i = 0; i < num; i++) {
			int loc = PseudoRandom.randInt(0, this.numberOfLocations - 1);
			int op = PseudoRandom.randInt(lowerBounds[loc], upperBounds[loc]);
			int ing = PseudoRandom.randInt(lowerBounds[loc + this.numberOfLocations],
					upperBounds[loc + this.numberOfLocations]);
			locList.add(loc);
			opList.add(op);
			ingredList.add(ing);
		}
	} // Constructor

	private Edits(Edits edits) {
		this(edits.numberOfLocations, edits.susp, edits.problem);
		for (int i = 0; i < edits.opList.size(); i++) {
			this.opList.add(edits.opList.get(i));
			this.locList.add(edits.locList.get(i));
			this.ingredList.add(edits.ingredList.get(i));
		}
	} // Copy Constructor

	@Override
	public Variable deepCopy() {
		// TODO Auto-generated method stub
		return new Edits(this);
	}

	public double getLowerBound(int index) throws JMException {
		if ((index >= 0) && (index < 2 * numberOfLocations))
			return lowerBounds[index];
		else {
			Configuration.logger_.severe(us.msu.cse.repair.ec.variable.Edits.class + ".getLowerBound(): index value ("
					+ index + ") invalid");
			throw new JMException(
					us.msu.cse.repair.ec.variable.Edits.class + ".getLowerBound: index value (" + index + ") invalid");
		} // else
	} // getLowerBound

	/**
	 * Get the upper bound of a value
	 * 
	 * @param index
	 *            The index of the value
	 * @return the upper bound
	 */
	public double getUpperBound(int index) throws JMException {
		if ((index >= 0) && (index < 2 * numberOfLocations))
			return upperBounds[index];
		else {
			Configuration.logger_.severe(us.msu.cse.repair.ec.variable.Edits.class + ".getUpperBound(): index value ("
					+ index + ") invalid");
			throw new JMException(
					us.msu.cse.repair.ec.variable.Edits.class + ".getUpperBound: index value (" + index + ") invalid");
		} // else
	} // getLowerBound

	public int getNumberOfEdits() {
		return this.opList.size();
	}

	public int getNumberOfLocations() {
		return this.numberOfLocations;
	}

	public List<Integer> getOpList() {
		return this.opList;
	}

	public List<Integer> getLocList() {
		return this.locList;
	}

	public List<Integer> getIngredList() {
		return this.ingredList;
	}

	public double[] getSusp() {
		return this.susp;
	}
}
