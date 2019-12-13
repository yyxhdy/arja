package differentiation;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;


import serialization.Kryox;

public class DiffCore {
	static Map<String, IOPair> methodInfos;
	static String storeRoot1;
	static String storeRoot2;
	

	public static void main(String args[]) throws IOException {
		storeRoot1 = args[0].trim();
		storeRoot2 = args[1].trim();
		methodInfos = new HashMap<>();
		
		for (int i = 2; i < args.length; i++) {
			String strs[] = args[i].split(":");
			String path = strs[0].trim();
			int ni = Integer.parseInt(strs[1].trim());
			int no = Integer.parseInt(strs[2].trim());
			
			methodInfos.put(path, new IOPair(ni, no));
		}
		
		double diff = 1.0;
		try {
			diff = compute();
		}
		catch (RuntimeException e) {
			
		}
		
		System.out.println("diff: " + diff);
		System.exit(0);
	}

	
	static double compute() throws IOException {
		DiffPair dpair = new DiffPair(0, 0);
		for (String method : methodInfos.keySet()) {
			DiffPair d = compute(method);
			dpair.num += d.num;
			dpair.diff += d.diff;
		}
			
		return (dpair.num == 0 ? 0 : dpair.diff / dpair.num);
	}
	
	static DiffPair compute(String method) throws IOException {
		DiffPair dpair = new DiffPair(0, 0);
		int c = 0;
		for (int i = 1; ; i++) {
			File dir = getDir(storeRoot1, method, i);
			if (!dir.exists())
				break;

			int j = findCorrRun(method, i);
			
			if (j == -1)
				continue;
			
			String outputs1[] = getOutputs(storeRoot1, method, i);
			String outputs2[] = getOutputs(storeRoot2, method, j);
			
			if (isEqual(outputs1, outputs2))
				continue;
			
			DiffPair d = getMethodDiff(method, i, j); 
			dpair.num += d.num;
			dpair.diff += d.diff;
			c++;
		}
		
		dpair.num = (c == 0 ? 0 : dpair.num / c);
		dpair.diff = (c == 0 ? 0 : dpair.diff / c);
		
		return dpair;
	}
	
	
	static int findCorrRun(String method, int runID) throws IOException {
		String[] inputs1 = getInputs(storeRoot1, method, runID);

		while (true) {
			File dir = getDir(storeRoot2, method, runID);
			if (dir.exists())
				break;
			else
				runID--;
		}

		int k = 0;
		while (true) {
			File dir1 = getDir(storeRoot2, method, runID - k);
			File dir2 = getDir(storeRoot2, method, runID + k);

			if (dir1.exists()) {
				String[] inputs2 = getInputs(storeRoot2, method, runID - k);
				if (isEqual(inputs1, inputs2))
					return runID - k;
			}
			if (dir2.exists() && k != 0) {
				String[] inputs2 = getInputs(storeRoot2, method, runID + k);
				if (isEqual(inputs1, inputs2))
					return runID + k;
			}
			if (!dir1.exists() && !dir2.exists())
				break;

			k++;
		}
		return -1;
	}
	
	static String[] getOutputs(String storeRoot, String method, int runID) throws IOException {
		int n = methodInfos.get(method).no;
		String outputs[] = new String[n];
		
		File dir = getDir(storeRoot, method, runID);
		for (int i = 0; i < n; i++) {
			File file = new File(dir,"out_" + i);
			if (file.exists())
				outputs[i] = FileUtils.readFileToString(file, "UTF-8");
			else
				outputs[i] = "";
		}
		return outputs;
	}
	
	static String[] getInputs(String storeRoot, String method, int runID) throws IOException {
		int n = methodInfos.get(method).ni;
		
		String inputs[] = new String[n];
		
		File dir = getDir(storeRoot, method, runID);
		for (int i = 0; i < n; i++) {
			File file = new File(dir,"in_" + i);
			if (file.exists())
				inputs[i] = FileUtils.readFileToString(file, "UTF-8");
			else
				inputs[i] = "";
		}
		return inputs;
	}
	
	static File getDir(String storeRoot, String method, int runID) {
		String child = method + "/" + runID;
		File dir = new File(new File(storeRoot), child);
		return dir;
	}
	
	
	
	static boolean isEqual(String[] array1, String[] array2) {
		if (array1.length != array2.length)
			return false;
		
		for (int i = 0; i < array1.length; i++) {
			if (!array1[i].equals(array2[i]))
				return false;
		}
		return true;
	}
	
	static DiffPair getMethodDiff(String method,
			int runID1, int runID2) throws FileNotFoundException {
		File dir1 = getDir(storeRoot1, method, runID1);
		File dir2 = getDir(storeRoot2, method, runID2);
		
		int numberOfOutputs = methodInfos.get(method).no;
				
		double totalDiff = 0;
		int totalData = 0;
		for (int i = 0; i < numberOfOutputs; i++) {
			File out1 = new File(dir1, "out_" + i);
			File out2 = new File(dir2, "out_" + i);
			
			Object o1 = null;
			Object o2 = null;
			
			if (out1.exists()) 
				o1 = getObjectFromFile(out1);
			if (out2.exists()) 
				o2 = getObjectFromFile(out2);

			
			ObjectUnpacker ou = new ObjectUnpacker(o1, o2);
			totalDiff += ou.getDiff();
			totalData += ou.getNumberOfData();
		}
		
		return  new DiffPair(totalData, totalDiff);
	}
	
	static Object getObjectFromFile(File fl) throws FileNotFoundException {
		Kryo kryo = new Kryox();

		kryo.setRegistrationRequired(false);
		kryo.setReferences(true);

		FileInputStream fis = new FileInputStream(fl);
		Input input = new Input(fis);

		Object obj = kryo.readClassAndObject(input);
		return obj;
	}
}
