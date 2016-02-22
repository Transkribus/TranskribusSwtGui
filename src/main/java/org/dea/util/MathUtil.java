package org.dea.util;

public class MathUtil {
	
	public final static double RAD_TO_DEG_FAC = 180.0d/Math.PI;
	public final static double DEG_TO_RAD_FAC = Math.PI/180d;
	
	public static boolean eqBounds(int value, int test, int tol) {
		return test > (value-tol) && test < (value+tol);
	}

	/**
	 * Signum functions with epsilon for floating point imprecision
	 */
	public static double signumEps(double val, double eps) {
		if (val < eps && val > -eps)
			return 0;
		else
			return Math.signum(val);
	}
	
	public static double radToDeg(double radVal) {
		return radVal * RAD_TO_DEG_FAC;
	}
	
	public static double degToRad(double degVal) {
		return degVal * DEG_TO_RAD_FAC;
	}

}
