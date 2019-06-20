package us.msu.cse.repair.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.tools.JavaFileObject;

import jmetal.core.Solution;
import jmetal.encodings.variable.ArrayInt;
import jmetal.encodings.variable.Binary;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.Statement;

import us.msu.cse.repair.core.compiler.JavaFileObjectImpl;
import us.msu.cse.repair.core.parser.MethodInfo;
import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.parser.SeedStatement;
import us.msu.cse.repair.core.parser.SeedStatementInfo;

public class IO {
	public static void printSeedStatements(Map<SeedStatement, SeedStatementInfo> seedStatements, String filePath)
			throws IOException {
		File file = new File(filePath);
		for (Map.Entry<SeedStatement, SeedStatementInfo> entry : seedStatements.entrySet()) {
			String infoStr = entry.getValue().getSourceFilePaths().get(0) + "\t"
					+ entry.getValue().getLCNodes().get(0).getLineNumber() + "\n";
			infoStr += entry.getKey().getStatement().toString();
			infoStr += "********************************************\n";
			FileUtils.writeByteArrayToFile(file, infoStr.getBytes(), true);
		}
	}

	public static void printModificationPoint(ModificationPoint mp, String filePath) throws IOException {
		String infoStr = mp.getSourceFilePath() + "\t" + mp.getLCNode().getLineNumber() + "\t" + mp.getSuspValue()
				+ "\n";
		infoStr += mp.getStatement().toString();
		infoStr += "********************************************\n\n";

		infoStr += mp.getIngredients().size() + "\n";
		for (Statement statement : mp.getIngredients()) {
			infoStr += statement.toString();
			infoStr += "********************************************\n";
		}

		FileUtils.writeByteArrayToFile(new File(filePath), infoStr.getBytes());
	}

	public static void printMethodsInEachModificationPoint(ModificationPoint mp) {
		Map<String, MethodInfo> declaredMethods = mp.getDeclaredMethods();
		System.out.println("*******************\n");
		for (String key : declaredMethods.keySet())
			System.out.println(key);
		System.out.println("*******************\n");
	}

	public static void saveProgramVariant(Map<String, String> modifiedJavaSources,
			Map<String, JavaFileObject> compiledClasses, String srcJavaDir, String binJavaDir,
			String programVariantOutputDir, int globalID) throws IOException {
		File srcFile = new File(srcJavaDir);
		File destSrcFile = new File(programVariantOutputDir, "Variant_" + (globalID) + "/src");

		File binFile = new File(binJavaDir);
		File destBinFile = new File(programVariantOutputDir, "Variant_" + (globalID) + "/bin");

		FileUtils.copyDirectory(srcFile, destSrcFile);
		FileUtils.copyDirectory(binFile, destBinFile);

		for (Map.Entry<String, String> entry : modifiedJavaSources.entrySet()) {
			String sourceFilePath = entry.getKey();
			URI uri1 = new File(sourceFilePath).toURI();
			URI uri2 = srcFile.toURI();
			URI uri3 = uri2.relativize(uri1);
			File file = new File(destSrcFile, uri3.getPath());
			FileUtils.writeByteArrayToFile(file, entry.getValue().getBytes());
		}

		for (Map.Entry<String, JavaFileObject> entry : compiledClasses.entrySet()) {
			String fullClassName = entry.getKey();
			String child = fullClassName.replace(".", File.separator) + ".class";

			File file = new File(destBinFile, child);
			byte[] bytes = ((JavaFileObjectImpl) entry.getValue()).getByteCode();
			FileUtils.writeByteArrayToFile(file, bytes);
		}
	}

	public static void savePatch(List<Integer> opList, List<Integer> locList, List<Integer> ingredList,
			List<ModificationPoint> modificationPoints, List<List<String>> availableManipulations, String patchDir,
			int globalID) throws IOException {
		File file = new File(patchDir, "Patch_" + globalID + ".txt");
		if (file.exists())
			file.delete();

		int opId = 0;
		for (int i = 0; i < locList.size(); i++) {
			int loc = locList.get(i);
			int op = opList.get(i);
			int ingred = ingredList.get(i);

			ModificationPoint mp = modificationPoints.get(loc);
			int lineNumber = mp.getLCNode().getLineNumber();
			String sourceFilePath = mp.getSourceFilePath();

			String manipulationName = availableManipulations.get(loc).get(op);

			Statement faulty = mp.getStatement();

			Statement seed = null;
			if (mp.getIngredients() != null && !mp.getIngredients().isEmpty())
				seed = mp.getIngredients().get(ingred);

			String data = (++opId) + " " + manipulationName + " " + sourceFilePath + " " + lineNumber + "\n";

			data += "Faulty:\n";
			data += faulty.toString();
			data += "Seed:\n";
			data += (seed == null ? "NULL\n" : seed.toString());
			data += "**************************************************\n";

			FileUtils.writeByteArrayToFile(file, data.getBytes(), true);
		}
	}

	public static void savePatch(List<Integer> opList, List<Integer> locList, List<Integer> ingredList,
			List<ModificationPoint> modificationPoints, List<List<String>> availableManipulations, String patchDir,
			int globalID, int evaluations, long estimatedTime) throws IOException {
		File file = new File(patchDir, "Patch_" + globalID + ".txt");
		if (file.exists())
			file.delete();

		int opId = 0;
		for (int i = 0; i < locList.size(); i++) {
			int loc = locList.get(i);
			int op = opList.get(i);
			int ingred = ingredList.get(i);

			ModificationPoint mp = modificationPoints.get(loc);
			int lineNumber = mp.getLCNode().getLineNumber();
			String sourceFilePath = mp.getSourceFilePath();

			String manipulationName = availableManipulations.get(loc).get(op);

			Statement faulty = mp.getStatement();

			Statement seed = null;
			if (mp.getIngredients() != null && !mp.getIngredients().isEmpty())
				seed = mp.getIngredients().get(ingred);

			String data = (++opId) + " " + manipulationName + " " + sourceFilePath + " " + lineNumber + "\n";

			data += "Faulty:\n";
			data += faulty.toString();
			data += "Seed:\n";
			data += (seed == null ? "NULL\n" : seed.toString());
			data += "**************************************************\n";

			FileUtils.writeByteArrayToFile(file, data.getBytes(), true);
		}
		String evalInfo = "Evaluations: " + evaluations;
		String timeInfo = "EstimatedTime: " + estimatedTime;
		String info = evalInfo + "\n" + timeInfo + "\n";
		FileUtils.writeByteArrayToFile(file, info.getBytes(), true);
	}

	public static void savePatch(Solution solution, List<ModificationPoint> modificationPoints,
			List<List<String>> availableManipulations, List<Map.Entry<Integer, Double>> list, int numberOfEdits,
			String patchDir, int globalID) throws IOException {
		int[] var0 = ((ArrayInt) solution.getDecisionVariables()[0]).array_;

		File file = new File(patchDir, "Patch_" + globalID + ".txt");
		int opId = 0, size = var0.length / 2;

		if (file.exists())
			file.delete();

		for (int i = 0; i < numberOfEdits; i++) {
			int id = list.get(i).getKey();
			ModificationPoint mp = modificationPoints.get(id);
			int lineNumber = mp.getLCNode().getLineNumber();
			String sourceFilePath = mp.getSourceFilePath();

			String manipulationName = availableManipulations.get(id).get(var0[id]);

			Statement faulty = mp.getStatement();

			Statement seed = null;
			if (mp.getIngredients() != null && !mp.getIngredients().isEmpty())
				seed = mp.getIngredients().get(var0[id + size]);

			String data = (++opId) + " " + manipulationName + " " + sourceFilePath + " " + lineNumber + "\n";

			data += "Faulty:\n";
			data += faulty.toString();
			data += "Seed:\n";
			data += (seed == null ? "NULL\n" : seed.toString());
			data += "**************************************************\n";

			FileUtils.writeByteArrayToFile(file, data.getBytes(), true);
		}
	}

	public static void savePatch(Solution solution, List<ModificationPoint> modificationPoints,
			String[] manipulationNames, String patchDir, int globalID) throws IOException {
		int[] var0 = ((ArrayInt) solution.getDecisionVariables()[0]).array_;
		Binary var1 = (Binary) solution.getDecisionVariables()[1];

		File file = new File(patchDir, "Patch_" + globalID + ".txt");
		int opId = 0, size = var1.getNumberOfBits();

		if (file.exists())
			file.delete();

		for (int i = 0; i < size; i++) {
			if (!var1.getIth(i))
				continue;

			ModificationPoint mp = modificationPoints.get(i);
			int lineNumber = mp.getLCNode().getLineNumber();
			String sourceFilePath = mp.getSourceFilePath();

			String manipulationName = manipulationNames[var0[i]];

			Statement faulty = mp.getStatement();

			Statement seed = mp.getIngredients().get(var0[i + size]);

			String data = (++opId) + " " + manipulationName + " " + sourceFilePath + " " + lineNumber + "\n";

			data += "Faulty:\n";
			data += faulty.toString();
			data += "Seed:\n";
			data += seed.toString();
			data += "**************************************************\n";

			FileUtils.writeByteArrayToFile(file, data.getBytes(), true);
		}
	}

	public static void saveCompiledClasses(Map<String, JavaFileObject> compiledClasses, File binWorkingDirFile)
			throws IOException {
		for (Map.Entry<String, JavaFileObject> entry : compiledClasses.entrySet()) {
			String fullClassName = entry.getKey();
			String child = fullClassName.replace(".", File.separator) + ".class";

			File file = new File(binWorkingDirFile, child);
			byte[] bytes = ((JavaFileObjectImpl) entry.getValue()).getByteCode();
			FileUtils.writeByteArrayToFile(file, bytes);
		}
	}
	
	
	public static void savePatch(Map<String, String> modifiedJavaSources, String srcJavaDir,
			String patchDir, int globalID) throws IOException, InterruptedException {
		File root = new File(patchDir, "Patch_" + globalID);
		List<String> diffs = new ArrayList<>();
		
		for (Map.Entry<String, String> entry : modifiedJavaSources.entrySet()) {
			String orgFilePath = entry.getKey();
			File patched = new File(root, "patched");
			
			String relative = new File(srcJavaDir).toURI().relativize(new File(orgFilePath).toURI()).getPath();
			File revisedFile = new File(patched, relative);
			FileUtils.writeByteArrayToFile(revisedFile, entry.getValue().getBytes());
			
			List<String> diff = getDiff(orgFilePath, revisedFile.getAbsolutePath());
			diffs.addAll(diff);
			diffs.add("\n");
		}
		
		FileUtils.writeLines(new File(root, "diff"), diffs);
	}
	static List<String> getDiff(String orgFilePath, String revisedFilePath) throws IOException, InterruptedException {
		List<String> params = new ArrayList<String>();
		params.add("diff");
		params.add("-u");
		params.add(orgFilePath);
		params.add(revisedFilePath);
		
		ProcessBuilder builder = new ProcessBuilder(params);
		builder.redirectOutput();
		builder.redirectErrorStream(true);
		builder.directory();

		Process process = builder.start();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		List<String> lines = new ArrayList<>();
		while ((line = in.readLine()) != null) {
		   lines.add(line);
		}
		process.waitFor();

		in.close();
		
		return lines;
	}

}
