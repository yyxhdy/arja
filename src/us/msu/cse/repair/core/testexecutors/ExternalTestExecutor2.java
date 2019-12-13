package us.msu.cse.repair.core.testexecutors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import us.msu.cse.repair.core.util.ProcessWithTimeout;
import us.msu.cse.repair.core.util.StreamReaderThread;

public class ExternalTestExecutor2 implements ITestExecutor {
	String binJavaDir;
	String binInsTestDir;
	Set<String> dependences;

	String binWorkingDir;

	String externalProjRoot;

	Set<String> positiveTests;
	Set<String> negativeTests;
	String finalTestsInfoPath;

	String jvmPath;

	int waitTime;
	boolean isIOExceptional;
	boolean isTimeout;

	int failuresInPositive;
	int failuresInNegative;
	
	double failErrorInPositive;
	double failErrorInNegative;

	Map<String, Double> failedTests;

	final int MAX = 300;

	public ExternalTestExecutor2(Set<String> positiveTests, Set<String> negativeTests, String finalTestsInfoPath,
			String binJavaDir, String binInsTestDir, Set<String> dependences, String binWorkingDir,
			String externalProjRoot, String jvmPath, int waitTime) {
		this.positiveTests = positiveTests;
		this.negativeTests = negativeTests;
		this.finalTestsInfoPath = finalTestsInfoPath;

		this.binJavaDir = binJavaDir;
		this.binInsTestDir = binInsTestDir;
		this.dependences = dependences;
		this.externalProjRoot = externalProjRoot;

		this.binWorkingDir = binWorkingDir;

		this.jvmPath = jvmPath;

		this.waitTime = waitTime;

		this.failuresInPositive = 0;
		this.failuresInNegative = 0;
		
		this.failErrorInPositive = 0;
		this.failErrorInNegative = 0;
		
		this.isTimeout = false;
		this.isIOExceptional = false;
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
		cpStr += (binInsTestDir + File.pathSeparator);
		cpStr += (new File(externalProjRoot, "bin").getCanonicalPath()  + File.pathSeparator);
		
		File jarDir = new File(externalProjRoot, "lib/*");
		cpStr += jarDir.getCanonicalPath();
		
		if (dependences != null) {
			for (String dp : dependences)
				cpStr += (File.pathSeparator + dp);
		}
		params.add(cpStr);

		params.add("us.msu.cse.repair.external.junit.JUnitTestRunner2");

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
		
/*		for (String st : streamReaderThread.getOutput())
			System.out.println(st);*/

		if (exitCode != 0) {
			isTimeout = true;
			return false;
		}
		
		if (streamReaderThread.isIOExceptional()) {
			isIOExceptional = true;
			return false;
		}

		List<String> output = streamReaderThread.getOutput();

		failedTests = new HashMap<>();
		for (String str : output) {
			if (str.startsWith("FailedTest")) {
				String info[] = str.split(":")[1].trim().split("\\s+");
				failedTests.put(info[0], Double.parseDouble(info[1]));
			}
		}

		for (Entry<String, Double> entry : failedTests.entrySet()) {
			String test = entry.getKey();
			double error = entry.getValue();
			if (negativeTests.contains(test)) {
				failuresInNegative++;
				failErrorInNegative += error;
			}
			else {
				failuresInPositive++;
				failErrorInPositive += error;
			}
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
			return failErrorInPositive / positiveTests.size();
		else
			return 0;
	}

	@Override
	public double getRatioOfFailuresInNegative() {
		// TODO Auto-generated method stub
		if (!negativeTests.isEmpty())
			return failErrorInNegative / negativeTests.size();
		else
			return 0;
	}

	@Override
	public boolean isIOExceptional() {
		// TODO Auto-generated method stub
		return this.isIOExceptional;
	}
	
	@Override
	public boolean isTimeout() {
		// TODO Auto-generated method stub
		return this.isTimeout;
	}


	@Override
	public Map<String, Double> getFailedTests() {
		return this.failedTests;
	}
}
