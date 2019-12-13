package detection;

public class ParameterInfoMain {
	public static void main(String args[]) {
		System.out.println("                         Parameters for this tool   ");
		System.out.println("*************************************************************************************");
		System.out.println("-DsrcJavaDir : the root directory of source code");
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.println("-DbinJavaDir : the root directory of all the compiled classes of source code");
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.println("-DbinTestDir : the root directory of all the compiled classes of test code");
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.println("-Ddependences : the dependences (jar files) of the buggy program, \n"
				+ "	        separated by \":\", and at least including a junit jar");
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.println("-DpatchPath : the absolute path of the patch file");
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.println("-DjvmPath : the path of the executable JVM, e.g., /usr/bin/java");
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.println("-DworkingRoot : the working root directory of the tool, default /tmp");
		System.out.println("*************************************************************************************");
	
		
	}
}
