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

public class SeedLineGeneratorProcess {
	Set<String> binJavaClasses;
	Set<String> binExecuteTestClasses;

	String javaClassesInfoPath;
	String testClassesInfoPath;

	String binJavaDir;
	String binTestDir;

	Set<String> dependences;

	String externalProjRoot;

	String jvmPath;

	public SeedLineGeneratorProcess(Set<String> binJavaClasses, String javaClassesInfoPath,
			Set<String> binExecuteTestClasses, String testClassesInfoPath, String binJavaDir, String binTestDir,
			Set<String> dependences, String externalProjRoot, String jvmPath) {
		this.binJavaClasses = binJavaClasses;
		this.javaClassesInfoPath = javaClassesInfoPath;

		this.binExecuteTestClasses = binExecuteTestClasses;
		this.testClassesInfoPath = testClassesInfoPath;

		this.binJavaDir = binJavaDir;
		this.binTestDir = binTestDir;

		this.dependences = dependences;
		this.externalProjRoot = externalProjRoot;
		this.jvmPath = jvmPath;
	}

	public Set<LCNode> getSeedLines() throws IOException, InterruptedException {
		List<String> params = new ArrayList<String>();
		params.add(jvmPath);
		params.add("-cp");

		File jarDir = new File(externalProjRoot, "lib/*");
		File binExternalDir = new File(externalProjRoot, "bin");

		String cpStr = "";
		cpStr += jarDir.getCanonicalPath() + File.pathSeparator;
		cpStr += binExternalDir.getCanonicalPath();
		params.add(cpStr);

		params.add("us.msu.cse.repair.external.coverage.SeedLineGenerator");

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

		if (javaClassesInfoPath != null)
			params.add("@" + javaClassesInfoPath);
		else {
			String bjs = "";
			for (String cls : binJavaClasses)
				bjs += (cls + File.pathSeparator);
			params.add(bjs);
		}

		if (testClassesInfoPath != null)
			params.add("@" + testClassesInfoPath);
		else {
			String bts = "";
			for (String cls : binExecuteTestClasses)
				bts += (cls + File.pathSeparator);
			params.add(bts);
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

		Set<LCNode> seedLines = new HashSet<LCNode>();

		for (String str : output) {
			if (str.startsWith("SeedLine")) {
				String slStr = str.split(":")[1].trim();
				String lineInfo[] = slStr.split("#");
				LCNode node = new LCNode(lineInfo[0], Integer.parseInt(lineInfo[1]));
				seedLines.add(node);
			}
		}

		return seedLines;
	}

}
