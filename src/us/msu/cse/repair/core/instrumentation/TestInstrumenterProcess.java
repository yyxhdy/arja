package us.msu.cse.repair.core.instrumentation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import us.msu.cse.repair.core.util.ProcessWithTimeout;
import us.msu.cse.repair.core.util.StreamReaderThread;

public class TestInstrumenterProcess {
	String binJavaDir;
	String binTestDir;
	Set<String> dependences;

	String externalProjRoot;

	String jvmPath;
	
	String binInsTestDir;
	
	
	public TestInstrumenterProcess(String binInsTestDir, 
			String binJavaDir, String binTestDir, Set<String> dependences,
			String externalProjRoot, String jvmPath) {
		this.binInsTestDir = binInsTestDir;
		
		this.binJavaDir = binJavaDir;
		this.binTestDir = binTestDir;

		this.dependences = dependences;
		this.externalProjRoot = externalProjRoot;

		this.jvmPath = jvmPath;
	}
	
	
	public void instrumentTestClasses() throws IOException, InterruptedException {
		List<String> params = new ArrayList<String>();
		params.add(jvmPath);
		params.add("-cp");

		String cpStr = "";
		cpStr += (binJavaDir + File.pathSeparator);
		cpStr += (binTestDir + File.pathSeparator);
		cpStr += (new File(externalProjRoot, "bin").getCanonicalPath()  + File.pathSeparator);
		
		File jarDir = new File(externalProjRoot, "lib/*");
		cpStr += jarDir.getCanonicalPath();
		
		if (dependences != null) {
			for (String dp : dependences)
				cpStr += (File.pathSeparator + dp);
		}
		params.add(cpStr);
		
		params.add("us.msu.cse.repair.external.instrumentation.TestInstrumenter");
		params.add(binTestDir);
		params.add(binInsTestDir);
		
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
		
		for (String st: output)
			System.out.println(st);
	}
	
	
}
