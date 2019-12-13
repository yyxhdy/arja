package differentiation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import localization.MethodInfo;

public class DiffExecutor {
	String storeRoot1;
	String storeRoot2;
	Map<String, Set<MethodInfo>> modifiedMethods;
	
	String binJavaDir;
	String binTestDir;
	Set<String> dependencies;
	
	String curProjRoot;
	
	String jvmPath;
	
	
	public DiffExecutor(String storeRoot1, String storeRoot2, Map<String, Set<MethodInfo>> modifiedMethods,
			String binJavaDir, String binTestDir, Set<String> dependencies, String curProjRoot, String jvmPath) {
		this.storeRoot1 = storeRoot1;
		this.storeRoot2 = storeRoot2;
		this.modifiedMethods = modifiedMethods;
		this.binJavaDir = binJavaDir;
		this.binTestDir = binTestDir;
		this.dependencies = dependencies;
		this.curProjRoot = curProjRoot;
		this.jvmPath = jvmPath;
	}
	
	
	
	public double execute() throws IOException, InterruptedException {
		List<String> params = new ArrayList<String>();
		params.add(jvmPath);
		params.add("-cp");

		String cpStr = "";
		cpStr += (binJavaDir + File.pathSeparator);
		cpStr += (binTestDir + File.pathSeparator);
		cpStr += (new File(curProjRoot, "bin").getCanonicalPath()  + File.pathSeparator);
		File jarDir = new File(curProjRoot, "lib/*");
		cpStr += jarDir.getCanonicalPath();
		
		if (dependencies != null) {
			for (String dp : dependencies)
				cpStr += (File.pathSeparator + dp);
		}
		params.add(cpStr);
		
		params.add("differentiation.DiffCore");
		params.add(storeRoot1);
		params.add(storeRoot2);
		
		for (Map.Entry<String, Set<MethodInfo>> entry : modifiedMethods.entrySet()) {
			String className = entry.getKey();
			
			for (MethodInfo mi : entry.getValue()) {
				String arg = className + "/" + mi.getName() + "#" + Math.abs(mi.getDesc().hashCode());
				arg += ":" + mi.getNumberOfInputs();
				arg += ":" + mi.getNumberOfOutputs();
				params.add(arg);
			}
		}
		
		ProcessBuilder builder = new ProcessBuilder(params);
		builder.redirectOutput();
		builder.redirectErrorStream(true);
		builder.directory();
		builder.environment().put("TZ", "America/Los_Angeles");

		Process process = builder.start();
		InputStream fis = process.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		
		String line = null; 
		while ((line = br.readLine()) != null) { 
			if (line.startsWith("diff")) {
				double diff = Double.parseDouble(line.split(":")[1].trim());
				return diff;
			}
		}
		process.waitFor();
		
		return 0;
	}
}
