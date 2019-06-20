package us.msu.cse.repair.core.faultlocalizer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import us.msu.cse.repair.core.parser.LCNode;

public class GZoltarFaultLocalizer2 implements IFaultLocalizer {
	Set<String> positiveTestMethods;
	Set<String> negativeTestMethods;

	Map<LCNode, Double> faultyLines;

	public GZoltarFaultLocalizer2(String gzoltarDataDir) throws IOException {
		positiveTestMethods = new HashSet<String>();
		negativeTestMethods = new HashSet<String>();
		File testFile = new File(gzoltarDataDir, "tests");
		List<String> allTestMethods = FileUtils.readLines(testFile, "UTF-8");
		for (int i = 1; i < allTestMethods.size(); i++) {
			String info[] = allTestMethods.get(i).trim().split(",");
			if (info[1].trim().equals("PASS"))
				positiveTestMethods.add(info[0].trim());
			else
				negativeTestMethods.add(info[0].trim());
		}

		faultyLines = new HashMap<LCNode, Double>();
		File spectraFile = new File(gzoltarDataDir, "spectra");
		List<String> fLines = FileUtils.readLines(spectraFile, "UTF-8");
		for (int i = 1; i < fLines.size(); i++) {
			String line = fLines.get(i).trim();

			int startIndex = line.indexOf('<');
			int endIndex = line.indexOf('{');
			String className = line.substring(startIndex + 1, endIndex);

			String[] info = line.split("#")[1].split(",");
			int lineNumber = Integer.parseInt(info[0].trim());
			double suspValue = Double.parseDouble(info[1].trim());

			LCNode lcNode = new LCNode(className, lineNumber);
			faultyLines.put(lcNode, suspValue);
		}
	}

	@Override
	public Map<LCNode, Double> searchSuspicious(double thr) {
		// TODO Auto-generated method stub
		Map<LCNode, Double> partFaultyLines = new HashMap<LCNode, Double>();
		for (Map.Entry<LCNode, Double> entry : faultyLines.entrySet()) {
			if (entry.getValue() >= thr)
				partFaultyLines.put(entry.getKey(), entry.getValue());
		}
		return partFaultyLines;
	}

	@Override
	public Set<String> getPositiveTests() {
		// TODO Auto-generated method stub
		return this.positiveTestMethods;
	}

	@Override
	public Set<String> getNegativeTests() {
		// TODO Auto-generated method stub
		return this.negativeTestMethods;
	}
}
