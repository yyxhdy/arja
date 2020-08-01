package us.msu.cse.repair;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

public class Interpreter {
	
	public static HashMap<String, Object> getBasicParameterSetting(Map<String, String> parameterStrs) {
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
		
		
		String testsS = parameterStrs.get("tests");	
		if (testsS != null) {
			Set<String> tests = new HashSet<String>();
			String strs[] = testsS.split(":");
			for (String st : strs)
				tests.add(st.trim());
			parameters.put("tests", tests);	
		}	
		
			
		String thrS = parameterStrs.get("thr");
		if (thrS != null) {
			double thr = Double.parseDouble(thrS);
			parameters.put("thr", thr);
		}

		String external = parameterStrs.get("externalProjRoot");
		if (external != null) {
			parameters.put("externalProjRoot", external);
		}
		
		String maxNumberOfModificationPointsS = parameterStrs.get("maxNumberOfModificationPoints");
		if (maxNumberOfModificationPointsS != null) {
			int maxNumberOfModificationPoints = Integer.parseInt(maxNumberOfModificationPointsS);
			parameters.put("maxNumberOfModificationPoints", maxNumberOfModificationPoints);	
		}
		
		String jvmPathS = parameterStrs.get("jvmPath");
		if (jvmPathS != null)
			parameters.put("jvmPath", jvmPathS);
		
		String testExecutorNameS = parameterStrs.get("testExecutorName");
		if (testExecutorNameS != null)
			parameters.put("testExecutorName", testExecutorNameS);
		
		String waitTimeS = parameterStrs.get("waitTime");
		if (waitTimeS != null) {
			int waitTime = Integer.parseInt(waitTimeS);
			parameters.put("waitTime", waitTime);
		}   
		
		String patchOutputRootS = parameterStrs.get("patchOutputRoot");
		if (patchOutputRootS != null)
			parameters.put("patchOutputRoot", patchOutputRootS);
		
		String gzoltarDataDirS = (String) parameterStrs.get("gzoltarDataDir");
		if (gzoltarDataDirS != null)
			parameters.put("gzoltarDataDir", gzoltarDataDirS);
			
		String ingredientModeS = parameterStrs.get("ingredientMode");
		if (ingredientModeS != null)
			parameters.put("ingredientMode", ingredientModeS);
		
		String diffFormatS = parameterStrs.get("diffFormat");
		if (diffFormatS != null) {
			boolean diffFormat = Boolean.parseBoolean(diffFormatS);
			parameters.put("diffFormat", diffFormat);
		}

		String seed_str = parameterStrs.get("seed");
		double seed = 0.0;
		if (seed_str != null) {
			seed = Double.parseDouble(seed_str);
		}
		try {
			PseudoRandom.randDouble();

			Field random_1 = PseudoRandom.class.getDeclaredField("random_");
			random_1.setAccessible(true);
			Object random_ = random_1.get(null);
			Field seed_field = random_.getClass().getDeclaredField("seed");
			seed_field.setAccessible(true);
			seed_field.set(random_, seed);
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		return parameters;
	}
	
	public static HashMap<String, String> getParameterStrings(String args[]) throws JMException {
		HashMap<String, String> parameters = new HashMap<String, String>();
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-D")) {
				if (i + 1 >= args.length)
					throw new JMException("The command is invalid!");
				parameters.put(args[i].trim().substring(2), args[i + 1].trim());
			}
		}
		
		return parameters;
	}
}
