package us.msu.cse.repair.external.instrumentation;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.objectweb.asm.Opcodes;


public class AssertTracer {
	public static int checkCount;
	public static int errorCount;
	public static List<Double> errors;
	public static double dist = Integer.MAX_VALUE;
	
	final static double MIN = 1e-20;
	
	final static double ir = 1 + 1.0 / ((double) Integer.MAX_VALUE - (double) Integer.MIN_VALUE);
	final static double lr = 1 + 1.0 / ((double) Long.MAX_VALUE - (double) Long.MIN_VALUE);
	final static double fr = 1 + 1.0 / ((double) Float.MAX_VALUE - (double) Float.MIN_VALUE);
	final static double sr = 1 + 1.0 / ((double) Short.MAX_VALUE - (double) Short.MIN_VALUE);
	final static double br = 1 + 1.0 / ((double) Byte.MAX_VALUE - (double) Byte.MIN_VALUE);
	final static double cr = 1 + 1.0 / 65535.0;
	final static double strr = 1 + 1.0 / ((double) Integer.MAX_VALUE);
	
	
	public static void clear() {
		checkCount = 0;
		errorCount = 0;
		errors = new ArrayList<Double>();
	}
	
	public static void assertTrue(boolean condition) {
		assertTrue(null, condition);
	}
	public static void assertTrue(String message, boolean condition) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertTrue(message, condition);
		}
		catch (Throwable t) {
			errorCount++;
			if (dist != Integer.MAX_VALUE)
				error = normalize(dist) * ir;
			else
				error = 1;
		}
		errors.add(error);
		dist = Integer.MAX_VALUE;
	}

	
	
	public static void assertFalse(boolean condition) {
		assertFalse(null, condition);
	}
	public static void assertFalse(String message, boolean condition) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertFalse(message, condition);
		} catch (Throwable t) {
			errorCount++;
			if (dist != Integer.MAX_VALUE)
				error = normalize(dist) * ir;
			else
				error = 1;
		}
		errors.add(error);
		dist = Integer.MAX_VALUE;
	}

	
	public static void assertEquals(Object expected, Object actual) {
		assertEquals(null, expected, actual);
	}
	public static void assertEquals(String message, Object expected, Object actual) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {
			errorCount++;
			
			if (isStringComparison(expected, actual)) {
				error = getLevenshteinDistance((String) expected, (String) actual);
				error = normalize(error) * strr;
			}
			else if (isDoubleComparison(expected, actual)) {
				Double n1 = (Double) expected;
				Double n2 = (Double) actual;
				error = Math.abs(n1.doubleValue() - n2.doubleValue());
				error = normalize(error);
			}
			else if (isFloatComparison(expected, actual)) {
				Float n1 = (Float) expected;
				Float n2 = (Float) actual;
				error = Math.abs(n1.doubleValue() - n2.doubleValue());
				error = normalize(error) * fr;
			}
			else if (isLongComparison(expected, actual)) {
				Long n1 = (Long) expected;
				Long n2 = (Long) actual;
				error = Math.abs(n1.doubleValue() - n2.doubleValue());
				error = normalize(error) * lr;
			}
			else if (isIntegerComparison(expected, actual)) {
				Integer n1 = (Integer) expected;
				Integer n2 = (Integer) actual;
				error = Math.abs(n1.doubleValue() - n2.doubleValue());
				error = normalize(error) * ir;
			}
			else if (isByteComparison(expected, actual)) {
				Byte n1 = (Byte) expected;
				Byte n2 = (Byte) actual;
				error = Math.abs(n1.doubleValue() - n2.doubleValue());
				error = normalize(error) * br;
			}
			else if (isShortComparison(expected, actual)) {
				Short n1 = (Short) expected;
				Short n2 = (Short) actual;
				error = Math.abs(n1.doubleValue() - n2.doubleValue());
				error = normalize(error) * sr;
			}
			else if (isCharComparison(expected, actual)) {
				Character c1 = (Character) expected;
				Character c2 = (Character) actual;
				error = Math.abs(c1 - c2);
				error = normalize(error) * cr;
			}
			else
				error = 1;
		}
		errors.add(error);
	}

	
	
	public static void assertNotEquals(Object unexpected, Object actual) {
		assertNotEquals(null, unexpected, actual);
	}
	public static void assertNotEquals(String message, Object unexpected, Object actual) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertNotEquals(message, unexpected, actual);
		} catch (Throwable t) {
			errorCount++;
			error = 1;
		}
		errors.add(error);
	}

	
	public static void assertNotEquals(long unexpected, long actual) {
		assertNotEquals(null, unexpected, actual);
	}
	public static void assertNotEquals(String message, long unexpected, long actual) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertNotEquals(message, unexpected, actual);
		} catch (Throwable t) {
			errorCount++;
			error = 1;
		}
		errors.add(error);
	}


	public static void assertNotEquals(double unexpected, double actual, double delta) {
		assertNotEquals(null, unexpected, actual, delta);
	}
	public static void assertNotEquals(String message, double unexpected, double actual, double delta) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertNotEquals(message, unexpected, actual, delta);
		} catch (Throwable t) {
			errorCount++;
			error = delta - Math.abs(unexpected - actual);
			error = normalize(error);
		}
		errors.add(error);
	}

	
	
	public static void assertNotEquals(float unexpected, float actual, float delta) {
		assertNotEquals(null, unexpected, actual, delta);
	}
	public static void assertNotEquals(String message, float unexpected, float actual, float delta) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertNotEquals(message, unexpected, actual, delta);
		} catch (Throwable t) {
			errorCount++;
			error = delta - Math.abs(unexpected - actual);
			error = normalize(error);
		}
		errors.add(error);
	}
	
	
	public static void assertArrayEquals(Object[] expecteds, Object[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}
	public static void assertArrayEquals(String message, Object[] expecteds, Object[] actuals) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
			errorCount++;
			
			if (expecteds == null || actuals == null)
				error = 1;
			else {
				int min = Math.min(expecteds.length, actuals.length);
				int max = Math.max(expecteds.length, actuals.length);
				for (int i = 0; i < min; i++) {
					if (expecteds[i] != null && actuals[i] != null && expecteds[i] instanceof String 
							&& actuals[i] instanceof String) {
						double dist = getLevenshteinDistance((String) expecteds[i], (String) actuals[i]);
						error += normalize(dist) * strr;
					}
					else if (isDoubleComparison(expecteds[i], actuals[i])) {
						Double n1 = (Double) expecteds[i];
						Double n2 = (Double) actuals[i];
						error = Math.abs(n1.doubleValue() - n2.doubleValue());
						error = normalize(error);
					}
					else if (isFloatComparison(expecteds[i], actuals[i])) {
						Float n1 = (Float) expecteds[i];
						Float n2 = (Float) actuals[i];
						error = Math.abs(n1.doubleValue() - n2.doubleValue());
						error = normalize(error) * fr;
					}
					else if (isLongComparison(expecteds[i], actuals[i])) {
						Long n1 = (Long) expecteds[i];
						Long n2 = (Long) actuals[i];
						error = Math.abs(n1.doubleValue() - n2.doubleValue());
						error = normalize(error) * lr;
					}
					else if (isIntegerComparison(expecteds[i], actuals[i])) {
						Integer n1 = (Integer) expecteds[i];
						Integer n2 = (Integer) actuals[i];
						error = Math.abs(n1.doubleValue() - n2.doubleValue());
						error = normalize(error) * ir;
					}
					else if (isByteComparison(expecteds[i], actuals[i])) {
						Byte n1 = (Byte) expecteds[i];
						Byte n2 = (Byte) actuals[i];
						error = Math.abs(n1.doubleValue() - n2.doubleValue());
						error = normalize(error) * br;
					}
					else if (isShortComparison(expecteds[i], actuals[i])) {
						Short n1 = (Short) expecteds[i];
						Short n2 = (Short) actuals[i];
						error = Math.abs(n1.doubleValue() - n2.doubleValue());
						error = normalize(error) * sr;
					}		
					else if (isCharComparison(expecteds[i], actuals[i])) {
						Character c1 = (Character) expecteds[i];
						Character c2 = (Character) actuals[i];
						error = Math.abs(c1 - c2);
						error += normalize(error) * cr;
					}
					else 
						error += 1;
				}
				error += (max - min);
				error = (max == 0) ? 1 : (error / max);
			}
		}
		errors.add(error);
	}


	public static void assertArrayEquals(boolean[] expecteds, boolean[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}
	public static void assertArrayEquals(String message, boolean[] expecteds, boolean[] actuals) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
			errorCount++;
			if (expecteds == null || actuals == null)
				error = 1;
			else {
				int min = Math.min(expecteds.length, actuals.length);
				int max = Math.max(expecteds.length, actuals.length);
				for (int i = 0; i < min; i++) {
					if (expecteds[i] != actuals[i])
						error += 1;
				}
				error += (max - min);
				error = (max == 0) ? 1 : (error / max);
			}
			
		}
		errors.add(error);
	}

	
	
	public static void assertArrayEquals(byte[] expecteds, byte[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}
	public static void assertArrayEquals(String message, byte[] expecteds, byte[] actuals) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
			errorCount++;

			if (expecteds == null || actuals == null)
				error = 1;
			else {
				int min = Math.min(expecteds.length, actuals.length);
				int max = Math.max(expecteds.length, actuals.length);
				for (int i = 0; i < min; i++) {
					if (expecteds[i] != actuals[i]) {
						double dist = Math.abs(expecteds[i] - actuals[i]);
						error += normalize(dist) * br;
					}
				}
				error += (max - min);
				error = (max == 0) ? 1 : (error / max);
			}
		}
		errors.add(error);
	}

	

	public static void assertArrayEquals(char[] expecteds, char[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}
	public static void assertArrayEquals(String message, char[] expecteds, char[] actuals) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
			errorCount++;
			
			if (expecteds == null || actuals == null)
				error = 1;
			else {
				int min = Math.min(expecteds.length, actuals.length);
				int max = Math.max(expecteds.length, actuals.length);
				for (int i = 0; i < min; i++) {
					if (expecteds[i] != actuals[i]) {
						double dist = Math.abs(expecteds[i] - actuals[i]);
						error += normalize(dist) * cr;
					}
				}
				error += (max - min);
				error = (max == 0) ? 1 : (error / max);
			}
		}
		errors.add(error);
	}


	public static void assertArrayEquals(short[] expecteds, short[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}
	public static void assertArrayEquals(String message, short[] expecteds, short[] actuals) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
			errorCount++;
			
			if (expecteds == null || actuals == null)
				error = 1;
			else {
				int min = Math.min(expecteds.length, actuals.length);
				int max = Math.max(expecteds.length, actuals.length);
				for (int i = 0; i < min; i++) {
					if (expecteds[i] != actuals[i]) {
						double dist = Math.abs(expecteds[i] - actuals[i]);
						error += normalize(dist) * sr;
					}
				}
				error += (max - min);
				error = (max == 0) ? 1 : (error / max);
			}
		}
		errors.add(error);
	}

	
	
	public static void assertArrayEquals(int[] expecteds, int[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}
	public static void assertArrayEquals(String message, int[] expecteds, int[] actuals) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
			errorCount++;
			if (expecteds == null || actuals == null)
				error = 1;
			else {
				int min = Math.min(expecteds.length, actuals.length);
				int max = Math.max(expecteds.length, actuals.length);
				for (int i = 0; i < min; i++) {
					if (expecteds[i] != actuals[i]) {
						double dist = Math.abs(expecteds[i] - actuals[i]);
						error += normalize(dist) * ir;
					}
				}
				error += (max - min);
				error = (max == 0) ? 1 : (error / max);
			}
		}
		errors.add(error);
	}

	
	
	public static void assertArrayEquals(long[] expecteds, long[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}
	public static void assertArrayEquals(String message, long[] expecteds, long[] actuals) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
			errorCount++;
			if (expecteds == null || actuals == null)
				error = 1;
			else {
				int min = Math.min(expecteds.length, actuals.length);
				int max = Math.max(expecteds.length, actuals.length);
				for (int i = 0; i < min; i++) {
					if (expecteds[i] != actuals[i]) {
						double dist = Math.abs(expecteds[i] - actuals[i]);
						error += normalize(dist) * lr;
					}
				}
				error += (max - min);
				error = (max == 0) ? 1 : (error / max);
			}
		}
		errors.add(error);
	}


	public static void assertArrayEquals(double[] expecteds, double[] actuals, double delta) {
		assertArrayEquals(null, expecteds, actuals, delta);
	}
	public static void assertArrayEquals(String message, double[] expecteds, double[] actuals, double delta) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals, delta);
		} catch (Throwable t) {
			errorCount++;
			
			if (expecteds == null || actuals == null)
				error = 1;
			else {
				int min = Math.min(expecteds.length, actuals.length);
				int max = Math.max(expecteds.length, actuals.length);
				for (int i = 0; i < min; i++) {
					double dist = Math.abs(expecteds[i] - actuals[i]);
					if (dist > delta) 
						error += normalize(dist - delta);
				}
				error += (max - min);
				error = (max == 0) ? 1 : (error / max);
			}
		}
		errors.add(error);
	}


	public static void assertArrayEquals(float[] expecteds, float[] actuals, float delta) {
		assertArrayEquals(null, expecteds, actuals, delta);
	}
	public static void assertArrayEquals(String message, float[] expecteds, float[] actuals, float delta) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals, delta);
		} catch (Throwable t) {
			errorCount++;
			
			if (expecteds == null || actuals == null)
				error = 1;
			else {
				int min = Math.min(expecteds.length, actuals.length);
				int max = Math.max(expecteds.length, actuals.length);
				for (int i = 0; i < min; i++) {
					double dist = Math.abs(expecteds[i] - actuals[i]);
					if (dist > delta) 
						error += normalize(dist - delta) * fr;
				}
				error += (max - min);
				error = (max == 0) ? 1 : (error / max);
			}
		}
		
		errors.add(error);
	}


	
	public static void assertEquals(double expected, double actual, double delta) {
		assertEquals(null, expected, actual, delta);
	}
	public static void assertEquals(String message, double expected, double actual, double delta) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertEquals(message, expected, actual, delta);
		} catch (Throwable t) {
			errorCount++;
			error = Math.abs(expected - actual) - delta;
			error = normalize(error);
		}
		errors.add(error);
	}




	public static void assertEquals(long expected, long actual) {
		assertEquals(null, expected, actual);
	}
	public static void assertEquals(String message, long expected, long actual) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {
			errorCount++;
			error = Math.abs(expected - actual);
			error = normalize(error) * lr;
		}
		errors.add(error);
	}

	

	public static void assertEquals(double expected, double actual) {
		assertEquals(null, expected, actual);
	}
	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, double expected, double actual) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {
			errorCount++;
			error = Math.abs(expected - actual);
			error = normalize(error);
		}
		errors.add(error);
	}

	
	public static void assertEquals(float expected, float actual, float delta) {
		assertEquals(null, expected, actual, delta);
	}
	public static void assertEquals(String message, float expected, float actual, float delta) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertEquals(message, expected, actual, delta);
		} catch (Throwable t) {
			errorCount++;
			error = Math.abs(expected - actual) - delta;
			error = normalize(error) * fr;
		}
		errors.add(error);
	}


	public static void assertNotNull(Object object) {
		assertNotNull(null, object);
	}
	public static void assertNotNull(String message, Object object) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertNotNull(message, object);
		} catch (Throwable t) {
			errorCount++;
			error = 1;
		}
		errors.add(error);
	}


	public static void assertNull(Object object) {
		assertNull(null, object);
	}
	public static void assertNull(String message, Object object) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertNull(message, object);
		} catch (Throwable t) {
			errorCount++;
			error = 1;
		}
		errors.add(error);
	}
	
	
	
	public static void assertSame(Object expected, Object actual) {
		assertSame(null, expected, actual);
	}
	public static void assertSame(String message, Object expected, Object actual) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertSame( message, expected, actual);
		} catch (Throwable t) {
			errorCount++;
			error = 1;
		}
		errors.add(error);
	}
	

	public static void assertNotSame(Object unexpected, Object actual) {
		assertNotSame(null, unexpected, actual);
	}
	public static void assertNotSame(String message, Object unexpected, Object actual) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertNotSame(message, unexpected, actual);
		} catch (Throwable t) {
			errorCount++;
			error = 1;
		}
		errors.add(error);
	}

	

	public static void assertEquals(Object[] expecteds, Object[] actuals) {
		assertEquals(null, expecteds, actuals);
	}
	@Deprecated
	public static void assertEquals(String message, Object[] expecteds, Object[] actuals) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertEquals(message, expecteds, actuals);
		} catch (Throwable t) {
			errorCount++;
			
			if (expecteds == null || actuals == null)
				error = 1;
			else {
				int min = Math.min(expecteds.length, actuals.length);
				int max = Math.max(expecteds.length, actuals.length);
				for (int i = 0; i < min; i++) {
					if (isStringComparison(expecteds[i], actuals[i])) {
						double dist = getLevenshteinDistance((String) expecteds[i], (String) actuals[i]);
						error += normalize(dist) * strr;
					}
					else if (isDoubleComparison(expecteds[i], actuals[i])) {
						Double n1 = (Double) expecteds[i];
						Double n2 = (Double) actuals[i];
						error = Math.abs(n1.doubleValue() - n2.doubleValue());
						error = normalize(error);
					}
					else if (isFloatComparison(expecteds[i], actuals[i])) {
						Float n1 = (Float) expecteds[i];
						Float n2 = (Float) actuals[i];
						error = Math.abs(n1.doubleValue() - n2.doubleValue());
						error = normalize(error) * fr;
					}
					else if (isLongComparison(expecteds[i], actuals[i])) {
						Long n1 = (Long) expecteds[i];
						Long n2 = (Long) actuals[i];
						error = Math.abs(n1.doubleValue() - n2.doubleValue());
						error = normalize(error) * lr;
					}
					else if (isIntegerComparison(expecteds[i], actuals[i])) {
						Integer n1 = (Integer) expecteds[i];
						Integer n2 = (Integer) actuals[i];
						error = Math.abs(n1.doubleValue() - n2.doubleValue());
						error = normalize(error) * ir;
					}
					else if (isByteComparison(expecteds[i], actuals[i])) {
						Byte n1 = (Byte) expecteds[i];
						Byte n2 = (Byte) actuals[i];
						error = Math.abs(n1.doubleValue() - n2.doubleValue());
						error = normalize(error) * br;
					}
					else if (isShortComparison(expecteds[i], actuals[i])) {
						Short n1 = (Short) expecteds[i];
						Short n2 = (Short) actuals[i];
						error = Math.abs(n1.doubleValue() - n2.doubleValue());
						error = normalize(error) * sr;
					}		
					else if (isCharComparison(expecteds[i], actuals[i])) {
						Character c1 = (Character) expecteds[i];
						Character c2 = (Character) actuals[i];
						error = Math.abs(c1 - c2);
						error += normalize(error) * cr;
					}
					else 
						error += 1;
				}
				error += (max - min);
				error = (max == 0) ? 1 : (error / max);
			}
		}
		errors.add(error);
	}


	
	public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
		assertThat(null, actual, matcher);
	}
	public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.assertThat(reason, actual, matcher);
		} catch (Throwable t) {
			errorCount++;
			error = 1;
		}
		errors.add(error);
	}
	
	
	public static void fail() {
		fail(null);
	}
	public static void fail(String message) {
		checkCount++;
		double error = 0;
		try {
			org.junit.Assert.fail(message);
		} catch (Throwable t) {
			errorCount++;
			error = 1;
		}
		errors.add(error);
	}
	
	
	/* 
	 * the follows are for Junit 3 
	 * 
	 */
	public static void assertEquals(boolean expected, boolean actual) {
		assertEquals(null, expected, actual);
	}
	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, boolean expected, boolean actual) {
		checkCount++;
		double error = 0;
		try {
			junit.framework.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {
			errorCount++;
			error = 1;
		}
		errors.add(error);
	}
	
	
	
	public static void assertEquals(byte expected, byte actual) {
		assertEquals(null, expected, actual);
	}
	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, byte expected, byte actual) {
		checkCount++;
		double error = 0;
		try {
			junit.framework.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {
			errorCount++;
			error = Math.abs(expected - actual);
			error = normalize(error) * br;
		}
		errors.add(error);
	}
	
	
	public static void assertEquals(char expected, char actual) {
		assertEquals(null, expected, actual);
	}
	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, char expected, char actual) {
		checkCount++;
		double error = 0;
		try {
			junit.framework.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {
			errorCount++;
			error = Math.abs(expected - actual);
			error = normalize(error) * cr;
		}
		errors.add(error);
	}
	

	public static void assertEquals(int expected, int actual) {
		assertEquals(null, expected, actual);
	}
	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, int expected, int actual) {
		checkCount++;
		double error = 0;
		try {
			junit.framework.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {
			errorCount++;
			error = Math.abs(expected - actual);
			error = normalize(error) * ir;
		}
		errors.add(error);
	}
	
	
	public static void assertEquals(short expected, short actual) {
		assertEquals(null, expected, actual);
	}
	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, short expected, short actual) {
		checkCount++;
		double error = 0;
		try {
			junit.framework.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {
			errorCount++;
			error = Math.abs(expected - actual);
			error = normalize(error) * sr;
		}
		errors.add(error);
	}
	
	
	public static void assertEquals(String expected, String actual) {
		assertEquals(null, expected, actual);
	}
	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, String expected, String actual) {
		checkCount++;
		double error = 0;
		try {
			junit.framework.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {
			errorCount++;
			
			if (expected == null || actual == null)
				error = 1;
			else 
				error = normalize(getLevenshteinDistance(expected, actual)) * strr;
		}
		errors.add(error);
	}

	
	@SuppressWarnings("deprecation")
	public static void failSame(String message) {
		checkCount++;
		double error = 0;
		try {
			junit.framework.Assert.failSame(message);
		} catch (Throwable t) {
			errorCount++;
		}
		errors.add(error);
	}
	
	@SuppressWarnings("deprecation")
	public static void failNotEquals(String message, Object expected, Object actual) {
		checkCount++;
		double error = 0;
		try {
			junit.framework.Assert.failNotEquals(message, expected, actual);
		} catch (Throwable t) {
			errorCount++;
		}
		errors.add(error);
	}
	
	@SuppressWarnings("deprecation")
	public static void failNotSame(String message, Object expected, Object actual) {
		checkCount++;
		double error = 0;
		try {
			junit.framework.Assert.failNotSame(message, expected, actual);
		} catch (Throwable t) {
			errorCount++;
		}
		errors.add(error);
	}
	
	

	@SuppressWarnings("deprecation")
	public static void format(String message, Object expected, Object actual) {
		junit.framework.Assert.format(message, expected, actual);
	}
	
	
	public static void distance(int val, int opcode, boolean flag) {
		double distance = Integer.MAX_VALUE;
		if (opcode == Opcodes.IFEQ) {
			if (flag)
				distance = Math.abs(val);
		} else if (opcode == Opcodes.IFNE) {
			if (!flag)
				distance = Math.abs(val);
		} else if (opcode == Opcodes.IFLE || opcode == Opcodes.IFLT) {
			if (flag)
				distance = val;
			else
				distance = -val;
		} else if (opcode == Opcodes.IFGE || opcode == Opcodes.IFGT) {
			if (flag)
				distance = -val;
			else
				distance = val;
		}

		if (distance >= 0 && distance < dist)
			dist = distance;
	}
	
	public static void distance(int val1, int val2, int opcode, boolean flag) {
		double distance = Integer.MAX_VALUE;
		
		if (opcode == Opcodes.IF_ICMPEQ) {
			if (flag) 
				distance = Math.abs(val1 - val2);
		}
		else if (opcode == Opcodes.IF_ICMPNE) {
			if (!flag) 
				distance = Math.abs(val1 - val2);
		}
		else if (opcode == Opcodes.IF_ICMPGE || opcode == Opcodes.IF_ICMPGT) {
			if (flag) 
				distance = val2 - val1;
			else
				distance = val1 - val2;
		}
		else if (opcode == Opcodes.IF_ICMPLE || opcode == Opcodes.IF_ICMPLT) {
			if (flag) 
				distance = val1 - val2;
			else
				distance = val2 - val1;
		}
		
		if (distance >= 0 && distance < dist)
			dist = distance;
	}
	
	
/*	static double normalize(double error) {
		double val = error / (error + 1);
		return Double.isNaN(val) ? 1 : val;
	}*/
	
	
	static double normalize(double error) {
		if (error == 0)
			return MIN;
		double val = error / (error + 1);
		return Double.isNaN(val) ? 1 : val;
	}
	
	static int getLevenshteinDistance(CharSequence left, CharSequence right) {
		if (left == null || right == null) {
			throw new IllegalArgumentException("CharSequences must not be null");
		}

		int n = left.length(); // length of left
		int m = right.length(); // length of right

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		if (n > m) {
			// swap the input strings to consume less memory
			final CharSequence tmp = left;
			left = right;
			right = tmp;
			n = m;
			m = right.length();
		}

		final int[] p = new int[n + 1];

		// indexes into strings left and right
		int i; // iterates through left
		int j; // iterates through right
		int upperLeft;
		int upper;

		char rightJ; // jth character of right
		int cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			upperLeft = p[0];
			rightJ = right.charAt(j - 1);
			p[0] = j;

			for (i = 1; i <= n; i++) {
				upper = p[i];
				cost = left.charAt(i - 1) == rightJ ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left
				// and up +cost
				p[i] = Math.min(Math.min(p[i - 1] + 1, p[i] + 1), upperLeft + cost);
				upperLeft = upper;
			}
		}

		return p[n];
	}
	
	static boolean isStringComparison(Object expected, Object actual) {
		return expected != null && actual != null && expected instanceof String && actual instanceof String;
	}
	
	static boolean isNumberComparison(Object expected, Object actual) {
		return isByteComparison(expected, actual) ||
				isShortComparison(expected, actual) ||
				isIntegerComparison(expected, actual) ||
				isLongComparison(expected, actual) ||
				isFloatComparison(expected, actual) ||
				isDoubleComparison(expected, actual);
				
	}
	
	static boolean isByteComparison(Object expected, Object actual) {
		return expected != null && actual != null && expected instanceof Byte && actual instanceof Byte;
	}
	
	static boolean isCharComparison(Object expected, Object actual) {
		return expected != null && actual != null && expected instanceof Character && actual instanceof Character;
	}
	
	static boolean isShortComparison(Object expected, Object actual) {
		return expected != null && actual != null && expected instanceof Short && actual instanceof Short;
	}
	
	static boolean isIntegerComparison(Object expected, Object actual) {
		return expected != null && actual != null && expected instanceof Integer && actual instanceof Integer;
	}
	
	static boolean isLongComparison(Object expected, Object actual) {
		return expected != null && actual != null && expected instanceof Long && actual instanceof Long;
	}
	
	static boolean isFloatComparison(Object expected, Object actual) {
		return expected != null && actual != null && expected instanceof Float && actual instanceof Float;
	}
	
	static boolean isDoubleComparison(Object expected, Object actual) {
		return expected != null && actual != null && expected instanceof Double && actual instanceof Double;
	}
}
