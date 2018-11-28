package us.msu.cse.repair.core.testexecutors;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

import us.msu.cse.repair.core.util.CustomURLClassLoader;

public class InternalTestExecutor implements ITestExecutor {
	Set<String> positiveTests;
	Set<String> negativeTests;

	CustomURLClassLoader urlClassLoader;

	int waitTime;
	boolean isTimeout;

	int failuresInPositive;
	int failuresInNegative;

	Set<String> failedTests;

	public InternalTestExecutor(Set<String> positiveTests, Set<String> negativeTests,
			CustomURLClassLoader urlClassLoader, int waitTime) throws MalformedURLException {
		this.positiveTests = positiveTests;
		this.negativeTests = negativeTests;

		this.urlClassLoader = urlClassLoader;

		this.waitTime = waitTime;
		this.isTimeout = false;

		this.failuresInPositive = 0;
		this.failuresInNegative = 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean runTests() throws IOException {
		// TODO Auto-generated method stub
		failedTests = new HashSet<String>();

		TestRunThread thread = new TestRunThread();
		thread.start();
		try {
			thread.join(waitTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean flag;

		if (thread.isAlive()) {
			isTimeout = true;
			thread.stop();
			flag = false;
		} else
			flag = thread.isSuccess;

		urlClassLoader.close();
		return flag;
	}

	private class TestRunThread extends Thread {
		boolean isSuccess;

		@Override
		public void run() {
			try {
				boolean posSuccess = runPositiveTests();
				boolean negSuccess = runNegativeTests();
				isSuccess = posSuccess & negSuccess;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	boolean runPositiveTests() throws ClassNotFoundException {
		for (String test : positiveTests) {
			String[] temp = test.split("#");

			Class<?> targetClass = urlClassLoader.loadClass(temp[0]);
			Request request = Request.method(targetClass, temp[1]);

			Result result = new JUnitCore().run(request);

			if (!result.wasSuccessful()) {
				failuresInPositive++;
				failedTests.add(test);
			}
		}

		return failuresInPositive == 0;
	}

	boolean runNegativeTests() throws ClassNotFoundException {
		for (String test : negativeTests) {
			String[] temp = test.split("#");
			Class<?> targetClass = urlClassLoader.loadClass(temp[0]);
			Request request = Request.method(targetClass, temp[1]);
			Result result = new JUnitCore().run(request);

			if (!result.wasSuccessful()) {
				failuresInNegative++;
				failedTests.add(test);
			}
		}
		return failuresInNegative == 0;
	}

	@Override
	public int getFailureCountInPositive() {
		// TODO Auto-generated method stub
		return this.failuresInPositive;
	}

	@Override
	public int getFailureCountInNegative() {
		// TODO Auto-generated method stub
		return this.failuresInNegative;
	}

	@Override
	public double getRatioOfFailuresInPositive() {
		// TODO Auto-generated method stub
		if (positiveTests.size() != 0)
			return (double) failuresInPositive / positiveTests.size();
		else
			return 0;
	}

	@Override
	public double getRatioOfFailuresInNegative() {
		// TODO Auto-generated method stub
		if (negativeTests.size() != 0)
			return (double) failuresInNegative / negativeTests.size();
		else
			return 0;
	}

	@Override
	public boolean isExceptional() {
		// TODO Auto-generated method stub
		return this.isTimeout;
	}

	@Override
	public Set<String> getFailedTests() {
		// TODO Auto-generated method stub
		return this.failedTests;
	}

}
