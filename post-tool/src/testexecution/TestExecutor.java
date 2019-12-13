package testexecution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TestExecutor {
	String binJavaDir;
	String binTestDir;
	Set<String> dependencies;
	
	String modifiedBinRoot;
	
	Set<String> tests;
	
	String curProjRoot;
	
	String jvmPath;
	
	Set<String> succeedTests;
	Set<String> failedTests;
	
	public TestExecutor(Set<String> tests, String modifiedBinRoot, String binJavaDir, String binTestDir,
			Set<String> dependencies, String curProjRoot, String jvmPath) {
		this.tests = tests;
		this.modifiedBinRoot = modifiedBinRoot;
		this.binJavaDir = binJavaDir;
		this.binTestDir = binTestDir;

		this.dependencies = dependencies;
		this.curProjRoot = curProjRoot;
		this.jvmPath = jvmPath;
		
		succeedTests = new HashSet<>();
		failedTests = new HashSet<>();
	}
	
	public void execute() throws IOException {
		List<String> params = new ArrayList<String>();
		params.add(jvmPath);
		params.add("-cp");

		String cpStr = "";
		if (modifiedBinRoot != null)
			cpStr += (modifiedBinRoot + File.pathSeparator);
		
		cpStr += (binJavaDir + File.pathSeparator);
		cpStr += (binTestDir + File.pathSeparator);
		cpStr += (new File(curProjRoot, "bin").getCanonicalPath() + File.pathSeparator);
		cpStr += new File(curProjRoot, "lib/*").getCanonicalPath();
		
		if (dependencies != null) {
			for (String dp : dependencies)
				cpStr += (File.pathSeparator + dp);
		}
		params.add(cpStr);

		params.add("testexecution.JUnitTestRunner");

		String testStrs = "";
		for (String test : tests)
			testStrs += (test + File.pathSeparator);
		params.add(testStrs);

		ProcessBuilder builder = new ProcessBuilder(params);
		builder.redirectOutput();
		builder.redirectErrorStream(true);
		builder.directory();
		builder.environment().put("TZ", "America/Los_Angeles");

		Process process = builder.start();
		
		
		StreamReaderThread streamReaderThread = new StreamReaderThread(process.getInputStream());
		streamReaderThread.start();

		ProcessWithTimeout processWithTimeout = new ProcessWithTimeout(process);
		processWithTimeout.waitForProcess(0);

		try {
			streamReaderThread.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			List<String> output = streamReaderThread.getOutput();
			for (String st : output) {
				if (st.startsWith("FailedTest")) {
					String failed = st.split(":")[1].trim();
					failedTests.add(failed);
				}
				else if (st.startsWith("SucceedTest")) {
					String succeed = st.split(":")[1].trim();
					succeedTests.add(succeed);
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public Set<String> getFailedTests() {
		return this.failedTests;
	}
	
	public Set<String> getSucceedTests() {
		return this.succeedTests;
	}
	
}
