package testexecution;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;



public class JUnitTestRunner {
	public static void main(String args[]) throws Exception {
		List<String> tests;
		if (args[0].startsWith("@")) {
			String path = args[0].trim().substring(1);
			tests = FileUtils.readLines(new File(path), "UTF-8");
		} else {
			String testStrs[] = args[0].trim().split(File.pathSeparator);
			tests = Arrays.asList(testStrs);
		}

		runTests(tests);
		System.exit(0);
	}

	private static void runTests(List<String> tests) throws ClassNotFoundException {
		List<String> failedTests = new ArrayList<String>();
		List<String> succeedTests = new ArrayList<String>();
		for (String test : tests) {
			String strs[] = test.split("#");
			String className = strs[0];
			String methodName = strs[1];
			
			Request request = Request.method(Class.forName(className), methodName);
			Result res = new JUnitCore().run(request);
			
			if (!res.wasSuccessful())
				failedTests.add(test);
			else
				succeedTests.add(test);
		}

		printTests(failedTests, succeedTests);
	}

		
	private static void printTests(List<String> failedTests, List<String> succeedTests) {
		System.out.println("FailureCount: " + failedTests.size());
		for (String test : failedTests) 
			System.out.println("FailedTest: " + test);
		
		
		System.out.println("SucceedCount: " + succeedTests.size());
		for (String test : succeedTests) 
			System.out.println("SucceedTest: " + test);
	}
}
