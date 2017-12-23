package us.msu.cse.repair.core.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DataSet {
	public static HashMap<String, Object> getDefects4JProgram(String defects4jRoot, String proj, int id)
			throws IOException {
		HashMap<String, Object> info = new HashMap<String, Object>();
		File pf1 = new File(defects4jRoot, proj);
		String name = proj.toLowerCase() + "_" + id + "_buggy";
		File pf2 = new File(pf1, name);

		String binJavaDir = new File(pf2, "target/classes").getCanonicalPath();
		String binTestDir = new File(pf2, "target/test-classes").getCanonicalPath();
		String srcJavaDir = new File(pf2, "src/main/java").getCanonicalPath();

		Set<String> dependences = new HashSet<String>();

		for (File file : new File(pf2, "lib").listFiles())
			dependences.add(file.getCanonicalPath());

		info.put("binJavaDir", binJavaDir);
		info.put("binTestDir", binTestDir);
		info.put("srcJavaDir", srcJavaDir);
		info.put("dependences", dependences);

		return info;
	}

	public static HashMap<String, Object> getSeedMath85Program(String seedMath85Root, int id) throws IOException {
		HashMap<String, Object> info = new HashMap<String, Object>();
		File pf = new File(seedMath85Root, "SM" + id);

		String binJavaDir = new File(pf, "target/classes").getCanonicalPath();
		String binTestDir = new File(pf, "target/test-classes").getCanonicalPath();
		String srcJavaDir = new File(pf, "src/main/java").getCanonicalPath();

		Set<String> dependences = new HashSet<String>();

		for (File file : new File(pf, "lib").listFiles())
			dependences.add(file.getCanonicalPath());

		info.put("binJavaDir", binJavaDir);
		info.put("binTestDir", binTestDir);
		info.put("srcJavaDir", srcJavaDir);
		info.put("dependences", dependences);

		return info;
	}

	public static HashMap<String, Object> getSeedMath85TMProgram(String seedMath85TMRoot, int id) throws IOException {
		HashMap<String, Object> info = new HashMap<String, Object>();
		File pf = new File(seedMath85TMRoot, "ST" + id);

		String binJavaDir = new File(pf, "target/classes").getCanonicalPath();
		String binTestDir = new File(pf, "target/test-classes").getCanonicalPath();
		String srcJavaDir = new File(pf, "src/main/java").getCanonicalPath();

		Set<String> dependences = new HashSet<String>();

		for (File file : new File(pf, "lib").listFiles())
			dependences.add(file.getCanonicalPath());

		info.put("binJavaDir", binJavaDir);
		info.put("binTestDir", binTestDir);
		info.put("srcJavaDir", srcJavaDir);
		info.put("dependences", dependences);

		return info;
	}
}
