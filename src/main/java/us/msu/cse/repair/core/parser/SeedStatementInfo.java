package us.msu.cse.repair.core.parser;

import java.util.List;

public class SeedStatementInfo {
	List<LCNode> lcNodes;
	List<String> sourceFilePaths;

	public SeedStatementInfo(List<LCNode> lcNodes, List<String> sourceFilePaths) {
		this.lcNodes = lcNodes;
		this.sourceFilePaths = sourceFilePaths;
	}

	public List<LCNode> getLCNodes() {
		return lcNodes;
	}

	public List<String> getSourceFilePaths() {
		return sourceFilePaths;
	}

	public void setLCNodes(List<LCNode> lcNodes) {
		this.lcNodes = lcNodes;
	}

	public void setSourceFilePaths(List<String> sourceFilePaths) {
		this.sourceFilePaths = sourceFilePaths;
	}

}
