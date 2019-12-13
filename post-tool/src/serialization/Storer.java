package serialization;

import java.io.File;

import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;


public class Storer {
	final static int MAX = 500;
	
	public static void storeObject(Object o, String path) {
		if (path == null)
			return;
		
		Kryo kryo = new Kryox();
		kryo.setRegistrationRequired(false);
		kryo.setReferences(true);
		
		Output output;
		try {
			output = new Output(FileUtils.openOutputStream(new File(path)));
			kryo.writeClassAndObject(output, o);
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static String generateStorePath(String storeRoot, String className, String methodName, String desc,
			int runID, String inout, int n) {
		if (runID > MAX)
			return null;
		
		String methodID = methodName + "#" + Math.abs(desc.hashCode());
		String path = className + "/" + methodID  + "/" + runID + "/" + inout
				+ "_" + n;
		File file = new File(storeRoot, path);
		return file.getAbsolutePath();
	}
}
