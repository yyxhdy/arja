package us.msu.cse.repair.external.instrumentation;

public class ComparisonHelper {

	/**
	 * Replacement function for double comparison
	 * 
	 * @param d1
	 *            a double.
	 * @param d2
	 *            a double.
	 * @return a int.
	 */
	
	final static double ir = 1 + 1.0 / ((double) Integer.MAX_VALUE - (double) Integer.MIN_VALUE);
	final static double lr = 1 + 1.0 / ((double) Long.MAX_VALUE - (double) Long.MIN_VALUE);
	final static double fr = 1 + 1.0 / ((double) Float.MAX_VALUE - (double) Float.MIN_VALUE);
	
	public static int doubleSubG(double d1, double d2) {
		if (d1 == d2) {
			return 0;
		} else {
			// Bytecode spec: If either number is NaN, the integer 1 is pushed onto the stack
			if(Double.isNaN(d1) || Double.isNaN(d2)) {
				return 1;
			}

			return doubleSubHelper(d1, d2);
		}
	}

	public static int doubleSubL(double d1, double d2) {
		if (d1 == d2) {
			return 0;
		} else {
			// Bytecode spec: If either number is NaN, the integer -1 is pushed onto the stack
			if(Double.isNaN(d1) || Double.isNaN(d2)) {
				return -1;
			}

			return doubleSubHelper(d1, d2);
		}
	}


	private static int doubleSubHelper(double d1, double d2) {
		if(Double.isInfinite(d1) || Double.isInfinite(d2)) {
			return Double.compare(d1, d2);
		}

		double diff = d1 - d2;
		double diff2 = Math.abs(diff) / (1.0 + Math.abs(diff));

		
		double t =  diff2 / (ir - diff2);
		
		int d3;
		if (t >= Integer.MAX_VALUE) 
			d3 = Integer.MAX_VALUE;
		else
			d3 = (int)Math.ceil(t);
		
		d3 = (int) Math.signum(diff) * d3;
		
		if(d3 == 0)
			d3 = (int)Math.signum(diff);

		return d3;
	}
	/**
	 * Replacement function for float comparison
	 * 
	 * @param f1
	 *            a float.
	 * @param f2
	 *            a float.
	 * @return a int.
	 */
	public static int floatSubG(float f1, float f2) {
		if (f1 == f2) {
			return 0;
		} else {
			// Bytecode spec: If either number is NaN, the integer 1 is pushed onto the stack
			if(Float.isNaN(f1) || Float.isNaN(f2)) {
				return 1;
			}

			return floatSubHelper(f1, f2);
		}
	}

	public static int floatSubL(float f1, float f2) {
		if (f1 == f2) {
			return 0;
		} else {
			// Bytecode spec: If either number is NaN, the integer -1 is pushed onto the stack
			if(Float.isNaN(f1) || Float.isNaN(f2)) {
				return -1;
			}
			return floatSubHelper(f1, f2);
		}
	}

	private static int floatSubHelper(float f1, float f2) {
		if(Float.isInfinite(f1) || Float.isInfinite(f2)) {
			return Float.compare(f1, f2);
		}
		double diff = (double)f1 - (double)f2;
		double diff2 = Math.abs(diff) / (1.0 + Math.abs(diff));
		
		double t = (diff2 * fr) / (ir - diff2 * fr);
		
		int d3;
		if (t >= Integer.MAX_VALUE) 
			d3 = Integer.MAX_VALUE;
		else
			d3 = (int)Math.ceil(t);
		
		d3 = (int) Math.signum(diff) * d3;
		
		if(d3 == 0)
			d3 = (int)Math.signum(diff);
		return d3;
	}



	/**
	 * Replacement function for long comparison
	 * 
	 * @param l1
	 *            a long.
	 * @param l2
	 *            a long.
	 * @return a int.
	 */
	public static int longSub(long l1, long l2) {
		if (l1 == l2) {
			return 0;
		} else {
			double diff = (double)l1 - (double)l2;
			double diff2 = Math.abs(diff) / (1.0 + Math.abs(diff));
			
			
			double t = (diff2 * lr) / (ir - diff2 * lr);
			
			int d3;
			if (t >= Integer.MAX_VALUE) 
				d3 = Integer.MAX_VALUE;
			else
				d3 = (int)Math.ceil(t);
			
			d3 = (int) Math.signum(diff) * d3;
			
			if(d3 == 0)
				d3 = (int)Math.signum(diff);
			return d3;
		}
	}
}
