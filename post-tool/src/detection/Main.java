package detection;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class Main {
	@SuppressWarnings("unchecked")
	public static void main(String args[]) throws Exception {
		String srcJavaDir;
		String binJavaDir;
		String binTestDir;
		String patchPath;
		String jvmPath;
		String workingRoot;
		
		Set<String> dependences;
		
		 if (args[0].equalsIgnoreCase("-listParameters"))
				ParameterInfoMain.main(args);
		 else {
			 Map<String, Object> params = Interpreter.getParameterSetting(args);
			 
			 if (params.get("srcJavaDir") != null) 
				 srcJavaDir = (String) params.get("srcJavaDir");
			 else 
				 throw new IOException("Please specify the root directory of the source code!");
			 
			 if (params.get("binJavaDir") != null) 
				 binJavaDir = (String) params.get("binJavaDir");
			 else 
				 throw new IOException("Please specify the root directory of the compiled classes of source code");
			 
			 if (params.get("binTestDir") != null) 
				 binTestDir = (String) params.get("binTestDir");
			 else
				 throw new IOException("Please specify the root directory of the compiled classes of test code");
			 
			 
			 if (params.get("dependences") != null) 
				 dependences = (Set<String>) params.get("dependences");
			 else
				 throw new IOException("Please specify the root directory of the dependencies");
			 
			 
			if (params.get("patchPath") != null)
				patchPath = (String) params.get("patchPath");
			else
				throw new IOException("Please specify the path of the patch file!");

			 if (params.get("jvmPath") != null) 
				 jvmPath = (String) params.get("jvmPath");
			 else
				 jvmPath = System.getProperty("java.home") + "/bin/java";
			 
			 if (params.get("workingRoot") != null)
				 workingRoot = (String) params.get("workingRoot");
			 else
				 workingRoot = "/tmp";
		
			Detector detector = new Detector(srcJavaDir, binJavaDir, binTestDir, dependences, patchPath, jvmPath,
					workingRoot);
			
			boolean isCorrect = detector.execute();
			
			System.out.println("*********************************************************");
			if (isCorrect) {
				System.out.println("Correct");
			}
			else {
				System.out.println("Incorrect");
				System.out.println("Distance: " + detector.getDistance());
			}
			System.out.println("*********************************************************");
		 }
		
	}
}
