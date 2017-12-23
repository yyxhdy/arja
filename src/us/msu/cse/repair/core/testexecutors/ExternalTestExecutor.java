package us.msu.cse.repair.core.testexecutors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import us.msu.cse.repair.core.util.ProcessWithTimeout;
import us.msu.cse.repair.core.util.StreamReaderThread;

public class ExternalTestExecutor implements ITestExecutor {
	String binJavaDir;
	String binTestDir;
	Set<String> dependences;

	String binWorkingDir;

	String externalProjRoot;

	Set<String> positiveTests;
	Set<String> negativeTests;
	String finalTestsInfoPath;

	String jvmPath;

	int waitTime;
	boolean isExceptional;

	int failuresInPositive;
	int failuresInNegative;

	Set<String> failedTests;

	final int MAX = 300;

	public ExternalTestExecutor(Set<String> positiveTests, Set<String> negativeTests, String finalTestsInfoPath,
			String binJavaDir, String binTestDir, Set<String> dependences, String binWorkingDir,
			String externalProjRoot, String jvmPath, int waitTime) {
		this.positiveTests = positiveTests;
		this.negativeTests = negativeTests;
		this.finalTestsInfoPath = finalTestsInfoPath;

		this.binJavaDir = binJavaDir;
		this.binTestDir = binTestDir;
		this.dependences = dependences;
		this.externalProjRoot = externalProjRoot;

		this.binWorkingDir = binWorkingDir;

		this.jvmPath = jvmPath;

		this.waitTime = waitTime;

		this.failuresInPositive = 0;
		this.failuresInNegative = 0;
		this.isExceptional = false;
	}

	@Override
	public boolean runTests() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		List<String> params = new ArrayList<String>();
		params.add(jvmPath);
		params.add("-cp");

		String cpStr = "";
		cpStr += (binWorkingDir + File.pathSeparator);
		cpStr += (binJavaDir + File.pathSeparator);
		cpStr += (binTestDir + File.pathSeparator);
		cpStr += new File(externalProjRoot, "bin").getCanonicalPath();
		if (dependences != null) {
			for (String dp : dependences)
				cpStr += (File.pathSeparator + dp);
		}
		params.add(cpStr);

		params.add("us.msu.cse.repair.external.junit.JUnitTestRunner");

		if (finalTestsInfoPath != null && positiveTests.size() > MAX)
			params.add("@" + finalTestsInfoPath);
		else {
			String testStrs = "";
			for (String test : positiveTests)
				testStrs += (test + File.pathSeparator);
			for (String test : negativeTests)
				testStrs += (test + File.pathSeparator);
			params.add(testStrs);
		}

		ProcessBuilder builder = new ProcessBuilder(params);
		builder.redirectOutput();
		builder.redirectErrorStream(true);
		builder.directory();
		builder.environment().put("TZ", "America/Los_Angeles");

		Process process = builder.start();

		StreamReaderThread streamReaderThread = new StreamReaderThread(process.getInputStream());
		streamReaderThread.start();

		ProcessWithTimeout processWithTimeout = new ProcessWithTimeout(process);
		int exitCode = processWithTimeout.waitForProcess(waitTime);

		streamReaderThread.join();

		if (exitCode != 0 || streamReaderThread.isStreamExceptional()) {
			isExceptional = true;
			return false;
		}

		List<String> output = streamReaderThread.getOutput();

		failedTests = new HashSet<String>();
		for (String str : output) {
			if (str.startsWith("FailedTest"))
				failedTests.add(str.split(":")[1].trim());
		}

		for (String test : failedTests) {
			if (negativeTests.contains(test))
				failuresInNegative++;
			else
				failuresInPositive++;
		}
		return failedTests.isEmpty();
	}

	@Override
	public int getFailureCountInPositive() {
		// TODO Auto-generated method stub
		return this.failuresInPositive;
	}

	@Override
	public int getFailureCountInNegative() {
		// TODO Auto-generated method stub
		return this.failuresInNegative;
	}

	@Override
	public double getRatioOfFailuresInPositive() {
		// TODO Auto-generated method stub
		if (!positiveTests.isEmpty())
			return (double) failuresInPositive / positiveTests.size();
		else
			return 0;
	}

	@Override
	public double getRatioOfFailuresInNegative() {
		// TODO Auto-generated method stub
		if (!negativeTests.isEmpty())
			return (double) failuresInNegative / negativeTests.size();
		else
			return 0;
	}

	@Override
	public boolean isExceptional() {
		// TODO Auto-generated method stub
		return this.isExceptional;
	}

	@Override
	public Set<String> getFailedTests() {
		return this.failedTests;
	}

}
