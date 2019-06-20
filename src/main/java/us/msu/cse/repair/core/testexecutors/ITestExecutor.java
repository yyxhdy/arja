package us.msu.cse.repair.core.testexecutors;

import java.util.Set;

public interface ITestExecutor {
	public boolean runTests() throws Exception;

	public int getFailureCountInPositive();

	public int getFailureCountInNegative();

	public double getRatioOfFailuresInPositive();

	public double getRatioOfFailuresInNegative();

	public boolean isExceptional();

	public Set<String> getFailedTests();
}
