package us.msu.cse.repair.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;

public abstract class AbstractRepairAlgorithm {
	protected Algorithm algorithm;

	public SolutionSet execute() throws ClassNotFoundException, JMException {
		SolutionSet solutionSet = algorithm.execute();
		try {
			deleteWorkingDirs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return solutionSet;
	}

	public void setInputParameter(String name, Object object) {
		algorithm.setInputParameter(name, object);
	}

	public void addOperator(String name, Operator operator) {
		algorithm.addOperator(name, operator);
	}

	public Problem getProblem() {
		return algorithm.getProblem();
	}
	
	private void deleteWorkingDirs() throws IOException {
		AbstractRepairProblem repairProblem = (AbstractRepairProblem) getProblem();
		String binWorkingRoot = repairProblem.getBinWorkingRoot();
		String orgPosTestsInfoPath = repairProblem.getOrgPosTestsInfoPath();
		String finalTestsInfoPath = repairProblem.getFinalTestsInfoPath();
		File binWorkingFile = new File(binWorkingRoot);
		File posTestsFile = new File(orgPosTestsInfoPath);
		File finalTestsFile = new File(finalTestsInfoPath);
		
		if (binWorkingFile.exists())
			FileUtils.deleteDirectory(binWorkingFile);
		if (posTestsFile.exists())
			posTestsFile.delete();
		if (finalTestsFile.exists())
			finalTestsFile.delete();
	}

}
