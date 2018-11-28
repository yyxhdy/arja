package us.msu.cse.repair.algorithms.kali;

import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;

public class KaliAlgInterface extends Algorithm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public KaliAlgInterface(Problem problem) {
		super(problem);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		// TODO Auto-generated method stub
		try {
			((Kali) this.problem_).execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
