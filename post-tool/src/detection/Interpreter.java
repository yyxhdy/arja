package detection;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Interpreter {
	
	public static HashMap<String, Object> getParameterSetting(String args[]) throws IOException {
		Map<String, String> parameterStrs = getParameterStrings(args);
		HashMap<String, Object> parameters = new HashMap<String, Object>();
	
		String binJavaDir = parameterStrs.get("binJavaDir");
		parameters.put("binJavaDir", binJavaDir);
		
		String binTestDir = parameterStrs.get("binTestDir");
		parameters.put("binTestDir", binTestDir);
		
		String srcJavaDir = parameterStrs.get("srcJavaDir");
		parameters.put("srcJavaDir", srcJavaDir);
			
		String dependencesS = parameterStrs.get("dependences");	
		if (dependencesS != null) {
			Set<String> dependences = new HashSet<String>();
			String strs[] = dependencesS.split(":");
			for (String st : strs)
				dependences.add(st.trim());
			parameters.put("dependences", dependences);	
		}	
		
		String patchOutputRootS = parameterStrs.get("patchPath");
		if (patchOutputRootS != null)
			parameters.put("patchPath", patchOutputRootS);
		
		
		String jvmPathS = parameterStrs.get("jvmPath");
		if (jvmPathS != null)
			parameters.put("jvmPath", jvmPathS);
		
		
		String binWorkingRootS = parameterStrs.get("workingRoot");
		if (binWorkingRootS != null)
			parameters.put("workingRoot", binWorkingRootS);
		
		return parameters;
	}
	
	static HashMap<String, String> getParameterStrings(String args[]) throws IOException {
		HashMap<String, String> parameters = new HashMap<String, String>();
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-D")) {
				if (i + 1 >= args.length)
					throw new IOException("The command is invalid!");
				parameters.put(args[i].trim().substring(2), args[i + 1].trim());
			}
		}
		
		return parameters;
	}
}
