package localization;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;

public class LineExtractor {
	String patchPath;
	String srcJavaDir;
	
	int pLevel;
	
	Map<String, Set<Integer>> modifiedLines;
	Set<String> relativePathsForModifiedFiles;
	
	
	public LineExtractor(String patchPath, String srcJavaDir) throws IOException {
		this.patchPath = patchPath;
		this.srcJavaDir = srcJavaDir;
		execute();
	}
	
	void execute() throws IOException {
		modifiedLines = new HashMap<>();
		relativePathsForModifiedFiles = new HashSet<>();
		List<String> content = FileUtils.readLines(new File(patchPath), "UTF-8");
		String curJavaClass = null;
		for (int i = 0; i < content.size(); i++) {
			String line = content.get(i).trim();
			
			if (line.startsWith("---")) {
				String temp = line.split("\\s+")[1];
		
				String jc = processPath(temp);
				relativePathsForModifiedFiles.add(jc);
				
				String javaClass = new File(srcJavaDir, jc).getAbsolutePath();
				curJavaClass = javaClass;
				
				Set<Integer> set = new HashSet<>();
				modifiedLines.put(javaClass, set);
			}
			else if (line.startsWith("@@")) {
				Set<Integer> values = modifiedLines.get(curJavaClass);
				String lns = line.split("@@")[1].split(",")[0].trim();
				
				int ln = Math.abs(Integer.parseInt(lns));
				values.add(ln);
			}
		}

	}
	
	
	String processPath(String path) {
		String sts[] = srcJavaDir.split(File.separator);
		String curSt = "";
		int beginIndex = 0;
		
		for (int i = sts.length - 1; i >= 0; i--) {
			curSt = sts[i] + File.separator + curSt;
			int index = path.indexOf(curSt);
			if (index == -1)
				break;
			else {
				beginIndex = index + curSt.length();
			}
		}

		pLevel = 0;
		for (int i = 0; i < beginIndex; i++) {
			if (path.charAt(i) == File.separatorChar) {
				pLevel++;
			}
		}
		
		return path.substring(beginIndex);
	}
	
	
	
	public Map<String, Set<Integer>> getModifiedLines() {
		return this.modifiedLines;
	}
	
	public Set<String> getRelativePathsForModifiedFiles() {
		return this.relativePathsForModifiedFiles;
	}
	
	public int getPLevel() {
		return this.pLevel;
	}
	
	public static void main(String args[]) throws IOException {
		
/*		String patchPath = "/Users/yuanyuan/Desktop/diffpatch/patch.txt";
		String srcJavaDir = "/Users/yuanyuan/Desktop/diffpatch/x";*/
		
		String srcJavaDir = "/Users/yuanyuan/Documents/Defects4J/Math/math_98_buggy/src/main/java";
		String patchPath = "/Users/yuanyuan/Documents/workspace/Arja-e/patches_ixnd/Patch_605/diff";
		
		LineExtractor line = new LineExtractor(patchPath, srcJavaDir);
		line.execute();
		
		System.out.println(line.getModifiedLines());
		System.out.println(line.getRelativePathsForModifiedFiles());
		System.out.println(line.getPLevel());
/*		String st = "abacde";
		
		int k = st.indexOf("cd");
		
		System.out.println(k);
		
		
		String sts = "/abc/def/";
		
		String strs[] = sts.split(File.separator);
		
		System.out.println(strs.length);
		int i = 0;
		for (String s : strs) {
			System.out.println((i++) + "\t" + s);
		}*/
		
		
	}
}
