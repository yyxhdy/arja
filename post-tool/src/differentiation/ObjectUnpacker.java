package differentiation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ObjectUnpacker {

	double diff;
	int numberOfData;
	
	public ObjectUnpacker(Object obj1, Object obj2) {
		try {
			computeDifference(obj1, obj2);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double getDiff() {
		return diff;
	}
	
	public int getNumberOfData() {
		return numberOfData;
	}
	
	void computeDifference(Object obj1, Object obj2)
			throws IllegalArgumentException, IllegalAccessException {
		Map<String, Object> values1 = new HashMap<>();
		Map<String, Object> values2 = new HashMap<>();
	
		
		Set<Object> unpackedObjs = new HashSet<>();

		if (obj1 != null) {
			unpack(obj1, "", values1, unpackedObjs);
		}
		if (obj2 != null) {
			unpackedObjs.clear();
			unpack(obj2, "", values2, unpackedObjs);
		}
	
		
		if (obj1 == null || obj2 == null) {
			diff = Math.max(values1.size(), values2.size());
			numberOfData = values1.size() + values2.size();
	
			return;
		}

		Set<String> keys = new HashSet<>();
		keys.addAll(values1.keySet());
		keys.addAll(values2.keySet());

		numberOfData = keys.size();
		
		diff = 0;
		for (String key : keys) {
			Object o1 = values1.get(key);
			Object o2 = values2.get(key);
			if (o1 == null || o2 == null) {
				diff += 1;
	
			}
			else {
				if (o1 instanceof CharSequence && o2 instanceof CharSequence) {
					CharSequence cs1 = (CharSequence) o1;
					CharSequence cs2 = (CharSequence) o2;
					diff += normalize(getLevenshteinDistance(cs1, cs2));
		
				} else if (o1 instanceof Boolean && o2 instanceof Boolean) {
					Boolean b1 = (Boolean) o1;
					Boolean b2 = (Boolean) o2;

					if (b1 != b2)
						diff += 1;
			
				} 
				else if (o1 instanceof Character && o2 instanceof Character) {
					Character c1 = (Character) o1;
					Character c2 = (Character) o2;
					diff += normalize(Math.abs(c1.charValue() - c2.charValue()));
		
				}
				else {
					Number n1 = (Number) o1;
					Number n2 = (Number) o2;

					diff += normalize(Math.abs(n1.doubleValue() - n2.doubleValue()));
		
				}
			}
		}
	}

	double normalize(double x) {
		return x / (x + 1);
	}

	void unpack(Object obj, String curName, Map<String, Object> values, Set<Object> unpackedObjs)
			throws IllegalArgumentException, IllegalAccessException {

		unpackedObjs.add(obj);
		
		
		if (obj == null)
			return;
		

		if (isBasicType(obj)) {
			values.put(curName, obj);
			return;
		}

		if (isBasicTypeArray(obj)) {
			handleBasicTypeArray(obj, curName, values);
			return;
		}

		if (obj.getClass().isArray()) {
			Object[] array = (Object[]) obj;
			for (int i = 0; i < array.length; i++) {
				String name = curName + "[" + i + "]";
				if (!unpackedObjs.contains(array[i]))
					unpack(array[i], name, values, unpackedObjs);
			}
		} else {
			Map<String, Field> fields = new HashMap<>();
			
			collectDeclaredFields(obj.getClass(), fields);
	
			for (Field f : fields.values()) {
				f.setAccessible(true);
				Object o = f.get(obj);
				String name = curName + "#" + f.getName();
				if (!unpackedObjs.contains(o))
					unpack(o, name, values, unpackedObjs);
			}
		}
	}

	void collectDeclaredFields(Class<?> clz, Map<String, Field> fields) {
		if (clz == Object.class)
			return;

		collectDeclaredFields(clz.getSuperclass(), fields);
	
		for (Field f : clz.getDeclaredFields()) {

			int mod = f.getModifiers();
			if (Modifier.isStatic(mod))
				continue;
			fields.put(f.getName(), f);
		}
	}

	void handleBasicTypeArray(Object obj, String curName, Map<String, Object> values) {
		String name = obj.getClass().getName();

		if (name.equals("[Z")) {
			boolean[] array = (boolean[]) obj;
			for (int i = 0; i < array.length; i++) {
				String cn = curName + "[" + i + "]";
				values.put(cn, array[i]);
			}
		} else if (name.equals("[C")) {
			char[] array = (char[]) obj;
			for (int i = 0; i < array.length; i++) {
				String cn = curName + "[" + i + "]";
				values.put(cn, array[i]);
			}
		} else if (name.equals("[B")) {
			byte[] array = (byte[]) obj;
			for (int i = 0; i < array.length; i++) {
				String cn = curName + "[" + i + "]";
				values.put(cn, array[i]);
			}
		} else if (name.equals("[S")) {
			short[] array = (short[]) obj;
			for (int i = 0; i < array.length; i++) {
				String cn = curName + "[" + i + "]";
				values.put(cn, array[i]);
			}
		} else if (name.equals("[I")) {
			int[] array = (int[]) obj;
			for (int i = 0; i < array.length; i++) {
				String cn = curName + "[" + i + "]";
				values.put(cn, array[i]);
			}
		} else if (name.equals("[F")) {
			float[] array = (float[]) obj;
			for (int i = 0; i < array.length; i++) {
				String cn = curName + "[" + i + "]";
				values.put(cn, array[i]);
			}
		} else if (name.equals("[J")) {
			long[] array = (long[]) obj;
			for (int i = 0; i < array.length; i++) {
				String cn = curName + "[" + i + "]";
				values.put(cn, array[i]);
			}
		} else if (name.equals("[D")) {
			double[] array = (double[]) obj;
			for (int i = 0; i < array.length; i++) {
				String cn = curName + "[" + i + "]";
				values.put(cn, array[i]);
			}
		}
	}

	boolean isBasicTypeArray(Object obj) {
		String name = obj.getClass().getName();

		boolean flag1 = name.equals("[Z");
		boolean flag2 = name.equals("[C");
		boolean flag3 = name.equals("[B");
		boolean flag4 = name.equals("[S");
		boolean flag5 = name.equals("[I");
		boolean flag6 = name.equals("[F");
		boolean flag7 = name.equals("[J");
		boolean flag8 = name.equals("[D");
		
		
		if (flag1 || flag2 || flag3 || flag4 || flag5 || flag6 || flag7 || flag8)
			return true;

		return false;
	}

	boolean isBasicType(Object obj) {
		boolean flag1 = obj instanceof Byte;
		boolean flag2 = obj instanceof Short;
		boolean flag3 = obj instanceof Integer;
		boolean flag4 = obj instanceof Long;
		boolean flag5 = obj instanceof Float;
		boolean flag6 = obj instanceof Double;
		boolean flag7 = obj instanceof Boolean;
		boolean flag8 = obj instanceof Character;
		boolean flag9 = obj instanceof CharSequence;

		if (flag1 || flag2 || flag3 || flag4 || flag5 || flag6 || flag7 || flag8 || flag9)
			return true;

		return false;
	}

	static int getLevenshteinDistance(CharSequence left, CharSequence right) {
		if (left == null || right == null) {
			throw new IllegalArgumentException("String must not be null");
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

}
