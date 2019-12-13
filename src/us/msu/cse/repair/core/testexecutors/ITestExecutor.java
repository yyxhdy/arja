package us.msu.cse.repair.core.testexecutors;

import java.util.Map;


public interface ITestExecutor {
	public boolean runTests() throws Exception;

	public int getFailureCountInPositive();

	public int getFailureCountInNegative();

	public double getRatioOfFailuresInPositive();

	public double getRatioOfFailuresInNegative();

	public boolean isTimeout();
	public boolean isIOExceptional();

	public Map<String, Double> getFailedTests();
}
