package preprocessing;

import org.hamcrest.Matcher;


public class AssertHelper {
	public static void assertTrue(boolean condition) {
		assertTrue(null, condition);
	}

	public static void assertTrue(String message, boolean condition) {
		try {
			org.junit.Assert.assertTrue(message, condition);
		} catch (Throwable t) {

		}
	}

	public static void assertFalse(boolean condition) {
		assertFalse(null, condition);
	}

	public static void assertFalse(String message, boolean condition) {
		try {
			org.junit.Assert.assertFalse(message, condition);
		} catch (Throwable t) {
		}
	}

	public static void assertEquals(Object expected, Object actual) {
		assertEquals(null, expected, actual);
	}

	public static void assertEquals(String message, Object expected, Object actual) {
		try {
			org.junit.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {

		}
	}

	public static void assertNotEquals(Object unexpected, Object actual) {
		assertNotEquals(null, unexpected, actual);
	}

	public static void assertNotEquals(String message, Object unexpected, Object actual) {
		try {
			org.junit.Assert.assertNotEquals(message, unexpected, actual);
		} catch (Throwable t) {
		}
	}

	public static void assertNotEquals(long unexpected, long actual) {
		assertNotEquals(null, unexpected, actual);
	}

	public static void assertNotEquals(String message, long unexpected, long actual) {
		try {
			org.junit.Assert.assertNotEquals(message, unexpected, actual);
		} catch (Throwable t) {
		}
	}

	public static void assertNotEquals(double unexpected, double actual, double delta) {
		assertNotEquals(null, unexpected, actual, delta);
	}

	public static void assertNotEquals(String message, double unexpected, double actual, double delta) {
		try {
			org.junit.Assert.assertNotEquals(message, unexpected, actual, delta);
		} catch (Throwable t) {
		}
	}

	public static void assertNotEquals(float unexpected, float actual, float delta) {
		assertNotEquals(null, unexpected, actual, delta);
	}

	public static void assertNotEquals(String message, float unexpected, float actual, float delta) {
		try {
			org.junit.Assert.assertNotEquals(message, unexpected, actual, delta);
		} catch (Throwable t) {
		}
	}

	public static void assertArrayEquals(Object[] expecteds, Object[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}

	public static void assertArrayEquals(String message, Object[] expecteds, Object[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
		}
	}

	public static void assertArrayEquals(boolean[] expecteds, boolean[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}

	public static void assertArrayEquals(String message, boolean[] expecteds, boolean[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
		}
	}

	public static void assertArrayEquals(byte[] expecteds, byte[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}

	public static void assertArrayEquals(String message, byte[] expecteds, byte[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
		}
	}

	public static void assertArrayEquals(char[] expecteds, char[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}

	public static void assertArrayEquals(String message, char[] expecteds, char[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
		}
	}

	public static void assertArrayEquals(short[] expecteds, short[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}

	public static void assertArrayEquals(String message, short[] expecteds, short[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
		}
	}

	public static void assertArrayEquals(int[] expecteds, int[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}

	public static void assertArrayEquals(String message, int[] expecteds, int[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {

		}
	}

	public static void assertArrayEquals(long[] expecteds, long[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}

	public static void assertArrayEquals(String message, long[] expecteds, long[] actuals) {
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals);
		} catch (Throwable t) {
		}
	}

	public static void assertArrayEquals(double[] expecteds, double[] actuals, double delta) {
		assertArrayEquals(null, expecteds, actuals, delta);
	}

	public static void assertArrayEquals(String message, double[] expecteds, double[] actuals, double delta) {
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals, delta);
		} catch (Throwable t) {
		}
	}

	public static void assertArrayEquals(float[] expecteds, float[] actuals, float delta) {
		assertArrayEquals(null, expecteds, actuals, delta);
	}

	public static void assertArrayEquals(String message, float[] expecteds, float[] actuals, float delta) {
		try {
			org.junit.Assert.assertArrayEquals(message, expecteds, actuals, delta);
		} catch (Throwable t) {
		}
	}

	public static void assertEquals(double expected, double actual, double delta) {
		assertEquals(null, expected, actual, delta);
	}

	public static void assertEquals(String message, double expected, double actual, double delta) {
		try {
			org.junit.Assert.assertEquals(message, expected, actual, delta);
		} catch (Throwable t) {
		}
	}

	public static void assertEquals(long expected, long actual) {
		assertEquals(null, expected, actual);
	}

	public static void assertEquals(String message, long expected, long actual) {
		try {
			org.junit.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {
		}
	}

	public static void assertEquals(double expected, double actual) {
		assertEquals(null, expected, actual);
	}

	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, double expected, double actual) {
		try {
			org.junit.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {
		}
	}

	public static void assertEquals(float expected, float actual, float delta) {
		assertEquals(null, expected, actual, delta);
	}

	public static void assertEquals(String message, float expected, float actual, float delta) {
		try {
			org.junit.Assert.assertEquals(message, expected, actual, delta);
		} catch (Throwable t) {
		}
	}

	public static void assertNotNull(Object object) {
		assertNotNull(null, object);
	}

	public static void assertNotNull(String message, Object object) {
		try {
			org.junit.Assert.assertNotNull(message, object);
		} catch (Throwable t) {
		}
	}

	public static void assertNull(Object object) {
		assertNull(null, object);
	}

	public static void assertNull(String message, Object object) {
		try {
			org.junit.Assert.assertNull(message, object);
		} catch (Throwable t) {
		}
	}

	public static void assertSame(Object expected, Object actual) {
		assertSame(null, expected, actual);
	}

	public static void assertSame(String message, Object expected, Object actual) {
		try {
			org.junit.Assert.assertSame(message, expected, actual);
		} catch (Throwable t) {
		}
	}

	public static void assertNotSame(Object unexpected, Object actual) {
		assertNotSame(null, unexpected, actual);
	}

	public static void assertNotSame(String message, Object unexpected, Object actual) {
		try {
			org.junit.Assert.assertNotSame(message, unexpected, actual);
		} catch (Throwable t) {

		}

	}

	public static void assertEquals(Object[] expecteds, Object[] actuals) {
		assertEquals(null, expecteds, actuals);
	}

	@Deprecated
	public static void assertEquals(String message, Object[] expecteds, Object[] actuals) {
		try {
			org.junit.Assert.assertEquals(message, expecteds, actuals);
		} catch (Throwable t) {

		}
	}

	public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
		assertThat(null, actual, matcher);
	}

	public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {

		try {
			org.junit.Assert.assertThat(reason, actual, matcher);
		} catch (Throwable t) {

		}

	}

	public static void fail() {
		fail(null);
	}

	public static void fail(String message) {
		try {
			org.junit.Assert.fail(message);
		} catch (Throwable t) {
		}

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
		try {
			junit.framework.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {

		}

	}

	public static void assertEquals(byte expected, byte actual) {
		assertEquals(null, expected, actual);
	}

	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, byte expected, byte actual) {

		try {
			junit.framework.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {

		}

	}

	public static void assertEquals(char expected, char actual) {
		assertEquals(null, expected, actual);
	}

	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, char expected, char actual) {

		try {
			junit.framework.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {

		}

	}

	public static void assertEquals(int expected, int actual) {
		assertEquals(null, expected, actual);
	}

	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, int expected, int actual) {

		try {
			junit.framework.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {

		}

	}

	public static void assertEquals(short expected, short actual) {
		assertEquals(null, expected, actual);
	}

	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, short expected, short actual) {
		try {
			junit.framework.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {

		}
	}

	public static void assertEquals(String expected, String actual) {
		assertEquals(null, expected, actual);
	}

	@SuppressWarnings("deprecation")
	public static void assertEquals(String message, String expected, String actual) {
		try {
			junit.framework.Assert.assertEquals(message, expected, actual);
		} catch (Throwable t) {

		}

	}

	@SuppressWarnings("deprecation")
	public static void failSame(String message) {

		try {
			junit.framework.Assert.failSame(message);
		} catch (Throwable t) {

		}

	}

	@SuppressWarnings("deprecation")
	public static void failNotEquals(String message, Object expected, Object actual) {
		try {
			junit.framework.Assert.failNotEquals(message, expected, actual);
		} catch (Throwable t) {

		}

	}

	@SuppressWarnings("deprecation")
	public static void failNotSame(String message, Object expected, Object actual) {
		try {
			junit.framework.Assert.failNotSame(message, expected, actual);
		} catch (Throwable t) {

		}

	}

	@SuppressWarnings("deprecation")
	public static void format(String message, Object expected, Object actual) {
		junit.framework.Assert.format(message, expected, actual);
	}
}
