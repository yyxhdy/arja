package detection;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import differentiation.DiffExecutor;
import instrument.OrgCodeInjecter;

import localization.LineExtractor;
import localization.MethodInfo;
import localization.ModifiedMethodFinder;
import patching.Patcher;
import preprocessing.AssertTransformer;
import preprocessing.TransientRemover;
import testexecution.ClassFinder;
import testexecution.PosNegFinder;
import testexecution.TestExecutor;

public class Detector {
	// buggy program
	String srcJavaDir;
	String binJavaDir;
	String binTestDir;
	Set<String> dependencies;

	// patch
	String patchPath;

	String curProjRoot;

	String jvmPath;

	Set<String> positiveTests;
	Map<String, Set<MethodInfo>> modifiedMethods;
	Map<String, Set<Integer>> modifiedLines;
	
	Set<String> relativePathsForModifiedFiles;
	int pLevel;

	String patchedBinRoot;

	String insBuggyBinRoot;
	String insPatchedBinRoot;

	String storeRoot1;
	String storeRoot2;

	String newBinJavaDir;
	String newBinTestDir;

	String workingRoot;

	File workingDir;
	
	double distance;

	private final String characterTable[] = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
			"p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };

	public Detector(String srcJavaDir, String binJavaDir, String binTestDir, Set<String> dependencies, String patchPath,
			String jvmPath, String workingRoot) {
		this.srcJavaDir = srcJavaDir;
		this.binJavaDir = binJavaDir;
		this.binTestDir = binTestDir;

		this.dependencies = dependencies;

		this.patchPath = patchPath;
		this.jvmPath = jvmPath;
		
		this.workingRoot = workingRoot;

		String path = this.getClass().getClassLoader().getResource("").getPath();
		curProjRoot = path.substring(0, path.length() - 5);
		distance = 0;
	}

	public boolean execute() throws Exception {
		setPaths();
		findModifiedMethods();
		findPositiveTests();
		preprocessing();
		applyPatch();

		instrumentBuggy();
		instrumentPatched();
		executePosTestsForBuggy();
		executePosTestsForPatched();

		if (new File(storeRoot1).exists() && new File(storeRoot2).exists())
			computeDiff();

		deleteWorkingFile();
		
		return distance == 0 ? true : false;
	}
	
	void computeDiff() throws IOException, InterruptedException {
		System.out.println("Compute the difference...");
		DiffExecutor cd = new DiffExecutor(storeRoot1, storeRoot2, modifiedMethods,
			 binJavaDir, binTestDir, dependencies,  curProjRoot, jvmPath);
		distance = cd.execute();
	}
	
	void applyPatch() throws Exception {
		System.out.println("Apply the patch...");
		Patcher patcher = new Patcher(patchPath, relativePathsForModifiedFiles, pLevel,
				srcJavaDir, binJavaDir, dependencies,
				patchedBinRoot, workingDir);
		patcher.execute();
	}

	void findModifiedMethods() throws IOException {
		System.out.println("Localize the modified methods...");
		LineExtractor le = new LineExtractor(patchPath, srcJavaDir);
		modifiedLines = le.getModifiedLines();
		ModifiedMethodFinder finder = new ModifiedMethodFinder(modifiedLines, srcJavaDir, dependencies);
		modifiedMethods = finder.findModifiedMethods();

		pLevel = le.getPLevel();
		relativePathsForModifiedFiles = le.getRelativePathsForModifiedFiles();
	}

	void findPositiveTests() throws ClassNotFoundException, IOException {
		System.out.println("Look for the positive tests...");
		ClassFinder cf = new ClassFinder(binJavaDir, binTestDir, dependencies);
		PosNegFinder pnf = new PosNegFinder(cf.findBinJavaClasses(), cf.findBinExecuteTestClasses(), binJavaDir,
				binTestDir, dependencies);
		positiveTests = pnf.getPositiveTests();
	}

	void instrumentBuggy() throws IOException {
		System.out.println("Instrument the buggy program...");
		OrgCodeInjecter oci = new OrgCodeInjecter(binJavaDir, modifiedMethods, insBuggyBinRoot, storeRoot1);
		oci.inject();
	}

	void instrumentPatched() throws IOException {
		System.out.println("Instrument the patched program...");
		OrgCodeInjecter oci = new OrgCodeInjecter(patchedBinRoot, modifiedMethods, insPatchedBinRoot, storeRoot2);
		oci.inject();
	}

	void executePosTestsForBuggy() throws IOException {
		System.out.println("Execute the positive tests on the bugyy program...");
		TestExecutor pte = new TestExecutor(positiveTests, insBuggyBinRoot, binJavaDir, binTestDir, dependencies,
				curProjRoot, jvmPath);
		pte.execute();
	}

	void executePosTestsForPatched() throws IOException {
		System.out.println("Execute the positive tests on the patched program...");
		TestExecutor pte = new TestExecutor(positiveTests, insPatchedBinRoot, binJavaDir, binTestDir, dependencies,
				curProjRoot, jvmPath);
		pte.execute();
	}

	void preprocessing() throws IOException {
		System.out.println("Preprocess the binary code...");
		AssertTransformer ti = new AssertTransformer(newBinTestDir, binJavaDir, binTestDir, dependencies, curProjRoot,
				jvmPath);
		ti.transform();

		TransientRemover tr = new TransientRemover(binJavaDir, newBinJavaDir);
		tr.remove();
		binJavaDir = newBinJavaDir;
	}

	boolean isDifferent() {
		return true;
	}

	void setPaths() {
		String rndID = getRandomID();

		workingDir = new File(workingRoot, rndID);

		patchedBinRoot = new File(workingDir, "patchedBin").getAbsolutePath();

		insPatchedBinRoot = new File(workingDir, "insPatchedBin").getAbsolutePath();
		insBuggyBinRoot = new File(workingDir, "insBuggyBin").getAbsolutePath();

		storeRoot1 = new File(workingDir, "store1").getAbsolutePath();
		storeRoot2 = new File(workingDir, "store2").getAbsolutePath();

		newBinJavaDir = new File(workingDir, "binJava").getAbsolutePath();
		newBinTestDir = new File(workingDir, "binTest").getAbsolutePath();
	}

	String getRandomID() {
		Random rnd = new Random();
		int count = 4;
		String id = "";
		for (int i = 0; i < count; i++) {
			int index = rnd.nextInt(characterTable.length);
			id = (id + characterTable[index]);
		}
		return id;
	}
	
	void deleteWorkingFile() throws IOException {
		FileUtils.deleteDirectory(workingDir);
	}
	
	public double getDistance() {
		return distance;
	}
}
