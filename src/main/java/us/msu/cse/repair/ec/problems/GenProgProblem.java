package us.msu.cse.repair.ec.problems;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.JavaFileObject;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import jmetal.core.Solution;
import jmetal.util.JMException;
import us.msu.cse.repair.core.AbstractRepairProblem;
import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.testexecutors.ITestExecutor;
import us.msu.cse.repair.core.util.IO;
import us.msu.cse.repair.ec.representation.GenProgSolutionType;
import us.msu.cse.repair.ec.variable.Edits;

public class GenProgProblem extends AbstractRepairProblem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Double wpos;
	Double wneg;

	public GenProgProblem(Map<String, Object> parameters) throws Exception {
		super(parameters);
		wpos = (Double) parameters.get("wpos");
		if (wpos == null)
			wpos = 1.0;

		wneg = (Double) parameters.get("wneg");
		if (wneg == null)
			wneg = 2.0;

		setProblemParams();
	}

	void setProblemParams() throws JMException {
		numberOfVariables_ = 1;
		numberOfObjectives_ = 1;
		numberOfConstraints_ = 0;
		problemName_ = "GenProgProblem";

		int size = modificationPoints.size();

		double[] prob = new double[size];
		for (int i = 0; i < size; i++)
			prob[i] = modificationPoints.get(i).getSuspValue();

		solutionType_ = new GenProgSolutionType(this, size, prob);

		upperLimit_ = new double[2 * size];
		lowerLimit_ = new double[2 * size];
		for (int i = 0; i < size; i++) {
			lowerLimit_[i] = 0;
			upperLimit_[i] = availableManipulations.get(i).size() - 1;
		}

		for (int i = size; i < 2 * size; i++) {
			lowerLimit_[i] = 0;
			upperLimit_[i] = modificationPoints.get(i - size).getIngredients().size() - 1;
		}
	}

	@Override
	public void evaluate(Solution solution) throws JMException {
		// TODO Auto-generated method stub
		System.out.println("One fitness evaluation starts...");
		Edits edits = (Edits) solution.getDecisionVariables()[0];
		List<Integer> locList = edits.getLocList();
		List<Integer> opList = edits.getOpList();
		List<Integer> ingredList = edits.getIngredList();

		Map<String, ASTRewrite> astRewriters = new HashMap<String, ASTRewrite>();

		for (int i = 0; i < locList.size(); i++) {
			int loc = locList.get(i);
			int op = opList.get(i);
			int ingred = ingredList.get(i);
			ModificationPoint mp = modificationPoints.get(loc);
			String manipName = availableManipulations.get(loc).get(op);
			Statement ingredStatement = mp.getIngredients().get(ingred);
			manipulateOneModificationPoint(mp, manipName, ingredStatement, astRewriters);
		}

		Map<String, String> modifiedJavaSources = getModifiedJavaSources(astRewriters);
		Map<String, JavaFileObject> compiledClasses = getCompiledClassesForTestExecution(modifiedJavaSources);

		boolean status = false;
		if (compiledClasses != null) {
			try {
				status = invokeTestExecutor(compiledClasses, solution);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			solution.setObjective(0, Double.MAX_VALUE);
			System.out.println("Compilation fails!");
		}

		if (status) {
			save(solution, modifiedJavaSources, compiledClasses);
		}

		evaluations++;
		System.out.println("One fitness evaluation is finished...");
	}

	void save(Solution solution, Map<String, String> modifiedJavaSources, Map<String, JavaFileObject> compiledClasses) {
		Edits edits = ((Edits) solution.getDecisionVariables()[0]);
		List<Integer> locList = edits.getLocList();
		List<Integer> opList = edits.getOpList();
		List<Integer> ingredList = edits.getIngredList();
		try {
			if (addTestAdequatePatch(opList, locList, ingredList)) {
				if (diffFormat) {
					try {
						IO.savePatch(modifiedJavaSources, srcJavaDir, this.patchOutputRoot, globalID);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				saveTestAdequatePatch(opList, locList, ingredList);
				globalID++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	boolean invokeTestExecutor(Map<String, JavaFileObject> compiledClasses, Solution solution) throws Exception {
		Set<String> samplePosTests = getSamplePositiveTests();
		ITestExecutor testExecutor = getTestExecutor(compiledClasses, samplePosTests);
		boolean status = testExecutor.runTests();
		if (status && percentage != null && percentage < 1) {
			testExecutor = getTestExecutor(compiledClasses, getPositiveTests());
			status = testExecutor.runTests();
		}

		int failureCountInPositive = testExecutor.getFailureCountInPositive();
		int failureCountInNegative = testExecutor.getFailureCountInNegative();

		boolean allFailed = (failureCountInPositive == samplePosTests.size()
				&& failureCountInNegative == negativeTests.size());
		if (!testExecutor.isExceptional() && !allFailed) {
			double fitness = wpos * failureCountInPositive + wneg * failureCountInNegative;
			solution.setObjective(0, fitness);		
			System.out.println("Number of failed tests: " + (failureCountInPositive + failureCountInNegative));
			System.out.println("Fitness: " + fitness);
		} else {
			solution.setObjective(0, Double.MAX_VALUE);
			System.out.println("Timeout occurs!");
		}

		return status;
	}

}
