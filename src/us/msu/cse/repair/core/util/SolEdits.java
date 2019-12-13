package us.msu.cse.repair.core.util;

import java.util.List;

import jmetal.core.Solution;
import jmetal.encodings.variable.ArrayInt;
import jmetal.encodings.variable.Binary;
import jmetal.util.JMException;

public class SolEdits {
	String editsStr;
	Solution solution;
	List<List<String>> availableManipulations;
	
	public SolEdits(Solution solution, List<List<String>> availableManipulations) throws JMException {
		this.solution = solution;
		this.availableManipulations = availableManipulations;
		
		ArrayInt array = (ArrayInt) solution.getDecisionVariables()[0];
		Binary binary = (Binary) solution.getDecisionVariables()[1];
		int size = binary.getNumberOfBits();
		editsStr = "";
		for (int i = 0; i < size; i++) {
			if (!binary.getIth(i))
				continue;
			
			editsStr += (i + ".");
			
			int index = array.getValue(i);
			String op = this.availableManipulations.get(i).get(index);
			op = op.substring(0, 1);
			editsStr += op;
			if (op.equals("D")) {
				editsStr += ".-1";
			}
			else if (op.equals("R")) {
				int k = array.getValue(i + size);
				editsStr += ("." + k);
			}
			else if (op.equals("I")) {
				int k = array.getValue(i + 2 * size);
				editsStr += ("." + k);
			}
			editsStr += ";";
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SolEdits))
			return false;
		
		String str = ((SolEdits) o).editsStr;
		return this.editsStr.equals(str);
	}
	
	
	@Override
	public int hashCode() {
		return editsStr.hashCode();
	}
	
}
