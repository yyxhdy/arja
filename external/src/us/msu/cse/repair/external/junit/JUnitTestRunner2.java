package us.msu.cse.repair.external.junit;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

import us.msu.cse.repair.external.instrumentation.AssertTracer;
import us.msu.cse.repair.external.util.Util;

public class JUnitTestRunner2 {
	public static void main(String args[]) throws Exception {
		List<String> tests;
		if (args[0].startsWith("@")) {
			String path = args[0].trim().substring(1);
			tests = Util.readLines(new File(path));
		} else {
			String testStrs[] = args[0].trim().split(File.pathSeparator);
			tests = Arrays.asList(testStrs);
		}

		runTests(tests);
		System.exit(0);
	}

	private static void runTests(List<String> tests) throws ClassNotFoundException {
		Map<String, Double> failedTests = new HashMap<>();
		for (String test : tests) {
			AssertTracer.clear();
			
			String strs[] = test.split("#");
			String className = strs[0];
			String methodName = strs[1];
			
			Request request = Request.method(Class.forName(className), methodName);
			Result res = new JUnitCore().run(request);
			
			if (res.getFailureCount() != 0) {
				AssertTracer.errors.add(1.0);
				AssertTracer.errorCount++;
			}

			if (AssertTracer.errorCount != 0 ) {
/*				for (double err : AssertTracer.errors)
					System.out.print(err + "\t");
				System.out.println();*/
				double ave = 0;
				for (double err : AssertTracer.errors) 
					ave += err;
				ave /= AssertTracer.errors.size();
				
				failedTests.put(test, ave);
			}	
		}
		
		printFailedTests(failedTests);
	}
	
	private static void printFailedTests(Map<String, Double> failedTests) {
		System.out.println("FailureCount: " + failedTests.size());
		for (Entry<String, Double> entry : failedTests.entrySet()) 
			System.out.println("FailedTest: " + entry.getKey() + " " + entry.getValue());
	}

}
