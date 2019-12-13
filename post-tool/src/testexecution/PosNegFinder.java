package testexecution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashSet;

import java.util.Set;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.instr.testing.TestResult;


public class PosNegFinder  {
	Set<String> positiveTestMethods;
	Set<String> negativeTestMethods;


	public PosNegFinder(Set<String> binJavaClasses, Set<String> binExecuteTestClasses, String binJavaDir,
			String binTestDir, Set<String> dependences) throws FileNotFoundException, IOException {
		String projLoc = new File("").getAbsolutePath();
		GZoltar gz = new GZoltar(projLoc);

		gz.getClasspaths().add(binJavaDir);
		gz.getClasspaths().add(binTestDir);

		if (dependences != null)
			gz.getClasspaths().addAll(dependences);

		for (String testClass : binExecuteTestClasses)
			gz.addTestToExecute(testClass);

		for (String javaClass : binJavaClasses)
			gz.addClassToInstrument(javaClass);

		gz.run();

		positiveTestMethods = new HashSet<String>();
		negativeTestMethods = new HashSet<String>();

		for (TestResult tr : gz.getTestResults()) {
			String testName = tr.getName();
			if (tr.wasSuccessful())
				positiveTestMethods.add(testName);
			else {
				if (!tr.getName().startsWith("junit.framework"))
					negativeTestMethods.add(testName);
			}
		}
	}



	public Set<String> getPositiveTests() {
		// TODO Auto-generated method stub
		return this.positiveTestMethods;
	}

	public Set<String> getNegativeTests() {
		// TODO Auto-generated method stub
		return this.negativeTestMethods;
	}
}
