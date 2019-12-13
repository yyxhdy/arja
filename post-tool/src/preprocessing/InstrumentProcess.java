package preprocessing;

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

public class InstrumentProcess {
	public static void main(String args[]) throws IOException {
		String orgBinTestDir = args[0].trim();
		String newBinTestDir = args[1].trim();
		instrument(orgBinTestDir, newBinTestDir);
		System.exit(0);
	}
	
	static void instrument(String orgBinTestDir, String newBinTestDir) throws IOException {
		Collection<File> files = FileUtils.listFiles(new File(orgBinTestDir), TrueFileFilter.INSTANCE,
				TrueFileFilter.INSTANCE);

		for (File fl : files) {
			File nfl = getNewFile(fl, orgBinTestDir, newBinTestDir);
			if (fl.getName().endsWith(".class")) {
				byte[] bytes = getTransformedBytes(fl);
				FileUtils.writeByteArrayToFile(nfl, bytes);
			} else
				FileUtils.copyFile(fl, nfl);
		}
	}

	static File getNewFile(File fl, String orgBinTestDir, String newBinTestDir) {
		String relative = new File(orgBinTestDir).toURI().relativize(fl.toURI()).getPath();
		return new File(newBinTestDir, relative);
	}

	static byte[] getTransformedBytes(File fl) throws IOException {
		InputStream is = new FileInputStream(fl);
		ClassReader cr = new ClassReader(is);
		ClassWriter cw = new ClassWriter(0);

		ClassVisitor cv = new ChangeAssertAdapter(cw);
		cr.accept(cv, 0);

		return cw.toByteArray();
	}
}
