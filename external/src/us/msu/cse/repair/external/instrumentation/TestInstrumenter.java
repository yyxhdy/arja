package us.msu.cse.repair.external.instrumentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class TestInstrumenter {
	public static void main(String args[]) throws IOException {
		String binTestDir = args[0].trim();
		String binInsTestDir = args[1].trim();
		
		File dir = new File(binTestDir);
		Collection<File> files = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE,
				TrueFileFilter.INSTANCE);
		
		for (File file : files) {
			String relative = dir.toURI().relativize(file.toURI()).getPath();
			File destFile = new File(binInsTestDir, relative);
			if (relative.endsWith(".class")) {
				 byte[] bytes = getTransformedBytes(file); 
				 FileUtils.writeByteArrayToFile(destFile, bytes);
			}
			else {
				FileUtils.copyFile(file, destFile);
			}
		}
		System.exit(0);
	}
	
	
	static byte[] getTransformedBytes(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		ClassReader cr = new ClassReader(is);
		ClassWriter cw = new ClassWriter(0);
		ClassVisitor cv = new AssertClassAdapter(cw);
		cr.accept(cv, 0);

		return cw.toByteArray();
	}
}
