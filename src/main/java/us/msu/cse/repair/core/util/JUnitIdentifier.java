package us.msu.cse.repair.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Test;
import org.junit.runner.RunWith;

import junit.framework.TestCase;

public class JUnitIdentifier {
	public static boolean isJUnitTest(Class<?> target) {
		return isJUnit3TestCase(target) || isJUnit4TestCase(target) || isJUnit3TestSuite(target)
				|| isJUnit4Others(target);
	}

	private static boolean isJUnit3TestCase(Class<?> target) {
		if (TestCase.class.isAssignableFrom(target))
			return true;
		else
			return false;
	}

	private static boolean isJUnit4TestCase(Class<?> target) {
		Method[] methods = target.getMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(Test.class))
				return true;
		}

		return false;
	}

	private static boolean isJUnit3TestSuite(Class<?> target) {
		Method[] methods = target.getDeclaredMethods();

		for (Method method : methods) {
			String returnName = method.getReturnType().getName();
			String name = method.getName();
			int mod = method.getModifiers();
			int numberOfParameters = method.getGenericParameterTypes().length;
			if (name.equals("suite") && returnName.equals("junit.framework.Test") && numberOfParameters == 0
					&& Modifier.isPublic(mod) && Modifier.isStatic(mod))
				return true;
		}
		return false;
	}

	private static boolean isJUnit4Others(Class<?> target) {
		if (target.isAnnotationPresent(RunWith.class))
			return true;
		else
			return false;

	}
}
