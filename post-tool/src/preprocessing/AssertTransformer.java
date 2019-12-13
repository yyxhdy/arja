package preprocessing;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class AssertTransformer {
	String binJavaDir;
	String binTestDir;
	Set<String> dependences;

	String curProjRoot;

	String jvmPath;
	
	String newBinTestDir;
	
	
	public AssertTransformer(String newBinTestDir, 
			String binJavaDir, String binTestDir, Set<String> dependences,
			String curProjRoot, String jvmPath) {
		this.newBinTestDir = newBinTestDir;
		
		this.binJavaDir = binJavaDir;
		this.binTestDir = binTestDir;

		this.dependences = dependences;
		this.curProjRoot = curProjRoot;

		this.jvmPath = jvmPath;
	}
	
	
	public void transform() throws IOException {
		List<String> params = new ArrayList<String>();
		params.add(jvmPath);
		params.add("-cp");

		String cpStr = "";
		cpStr += (binJavaDir + File.pathSeparator);
		cpStr += (binTestDir + File.pathSeparator);
		cpStr += (new File(curProjRoot, "bin").getCanonicalPath()  + File.pathSeparator);
		
		File jarDir = new File(curProjRoot, "lib/*");
		cpStr += jarDir.getCanonicalPath();
		
		if (dependences != null) {
			for (String dp : dependences)
				cpStr += (File.pathSeparator + dp);
		}
		params.add(cpStr);
		
		params.add("preprocessing.InstrumentProcess");
		params.add(binTestDir);
		params.add(newBinTestDir);
		
		ProcessBuilder builder = new ProcessBuilder(params);
		builder.redirectOutput();
		builder.redirectErrorStream(true);
		builder.directory();
		builder.environment().put("TZ", "America/Los_Angeles");
		
		builder.start();
	}
}
