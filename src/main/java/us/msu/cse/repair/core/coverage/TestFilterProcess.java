package us.msu.cse.repair.core.coverage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import us.msu.cse.repair.core.parser.LCNode;
import us.msu.cse.repair.core.util.ProcessWithTimeout;
import us.msu.cse.repair.core.util.StreamReaderThread;

public class TestFilterProcess {
	Set<LCNode> faultyLines;
	String faultyLinesInfoPath;

	Set<String> orgPositiveTests;
	String orgPosTestsInfoPath;

	String binJavaDir;
	String binTestDir;
	Set<String> dependences;

	String externalProjRoot;

	String jvmPath;
	

	public TestFilterProcess(Set<LCNode> faultyLines, String faultyLinesInfoPath, Set<String> orgPositiveTests,
			String orgPosTestsInfoPath, String binJavaDir, String binTestDir, Set<String> dependences,
			String externalProjRoot, String jvmPath) {
		this.faultyLines = faultyLines;
		this.faultyLinesInfoPath = faultyLinesInfoPath;

		this.orgPositiveTests = orgPositiveTests;
		this.orgPosTestsInfoPath = orgPosTestsInfoPath;

		this.binJavaDir = binJavaDir;
		this.binTestDir = binTestDir;

		this.dependences = dependences;
		this.externalProjRoot = externalProjRoot;

		this.jvmPath = jvmPath;
	}

	public Set<String> getFilteredPositiveTests() throws IOException, InterruptedException {
		List<String> params = new ArrayList<String>();
		params.add(jvmPath);
		params.add("-cp");

		File jarDir = new File(externalProjRoot, "lib/*");
		File binExternalDir = new File(externalProjRoot, "bin");

		String cpStr = "";
		cpStr += jarDir.getCanonicalPath() + File.pathSeparator;
		cpStr += binExternalDir.getCanonicalPath();
		params.add(cpStr);

		params.add("us.msu.cse.repair.external.coverage.TestFilter");

		params.add(binJavaDir);
		params.add(binTestDir);

		if (dependences == null || dependences.isEmpty())
			params.add(File.pathSeparator);
		else {
			String dps = "";
			for (String cls : dependences)
				dps += (cls + File.pathSeparator);
			params.add(dps);
		}

		if (faultyLinesInfoPath != null)
			params.add("@" + faultyLinesInfoPath);
		else {
			String fls = "";
			for (LCNode node : faultyLines) {
				String className = node.getClassName();
				int lineNumber = node.getLineNumber();
				fls += (className + "#" + lineNumber + File.pathSeparator);
			}
			params.add(fls);
		}

		if (orgPosTestsInfoPath != null)
			params.add("@" + orgPosTestsInfoPath);
		else {
			String opts = "";
			for (String test : orgPositiveTests)
				opts += (test + File.pathSeparator);
			params.add(opts);
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
		processWithTimeout.waitForProcess(0);

		streamReaderThread.join();
		List<String> output = streamReaderThread.getOutput();

		Set<String> filteredPositiveTests = new HashSet<String>();
		for (String str : output) {
			if (str.startsWith("FilteredTest"))
				filteredPositiveTests.add(str.split(":")[1].trim());
		}

		return filteredPositiveTests;
	}

}
