package us.msu.cse.repair.core.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.Statement;

import jmetal.core.Solution;
import jmetal.encodings.variable.ArrayInt;
import us.msu.cse.repair.core.parser.ModificationPoint;

public class Patch {
	Set<PatchItem> itemSet;

	public Patch(List<Integer> opList, List<Integer> locList, List<Integer> ingredList,
			List<ModificationPoint> modificationPoints, List<List<String>> availableManipulations) {
		itemSet = new HashSet<PatchItem>();

		for (int i = 0; i < locList.size(); i++) {
			PatchItem item = new PatchItem();
			int loc = locList.get(i);
			int op = opList.get(i);
			int ingred = ingredList.get(i);

			ModificationPoint mp = modificationPoints.get(loc);

			item.manipulationName = availableManipulations.get(loc).get(op);
			item.sourceFilePath = mp.getSourceFilePath();
			item.lineNumber = mp.getLCNode().getLineNumber();

			item.faultyStatement = mp.getStatement();

			if (mp.getIngredients().size() > 0)
				item.ingredientStatement = mp.getIngredients().get(ingred);
			else
				item.ingredientStatement = null;

			itemSet.add(item);
		}
	}

	public Patch(Solution solution, List<ModificationPoint> modificationPoints,
			List<List<String>> availableManipulations, List<Map.Entry<Integer, Double>> list, int numberOfEdits) {
		int[] var0 = ((ArrayInt) solution.getDecisionVariables()[0]).array_;
		int size = var0.length / 2;
		itemSet = new HashSet<PatchItem>();

		for (int i = 0; i < numberOfEdits; i++) {
			PatchItem item = new PatchItem();
			int id = list.get(i).getKey();
			ModificationPoint mp = modificationPoints.get(id);
			item.manipulationName = availableManipulations.get(id).get(var0[id]);
			item.sourceFilePath = mp.getSourceFilePath();
			item.lineNumber = mp.getLCNode().getLineNumber();

			item.faultyStatement = mp.getStatement();
			if (mp.getIngredients().size() > 0)
				item.ingredientStatement = mp.getIngredients().get(var0[id + size]);
			else
				item.ingredientStatement = null;

			itemSet.add(item);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Patch))
			return false;
		Patch patch = (Patch) o;
		return this.itemSet.equals(patch.itemSet);
	}

	@Override
	public int hashCode() {
		return this.itemSet.hashCode();
	}

	@Override
	public String toString() {
		String str = "";
		int i = 0;
		for (PatchItem item : itemSet) {
			str += (++i) + ": ";
			str += item.manipulationName + " ";
			str += item.sourceFilePath + " ";
			str += item.lineNumber + "\n";
			str += "Faulty:\n";
			str += item.faultyStatement.toString();
			str += "Seed:\n";

			if (item.ingredientStatement != null)
				str += item.ingredientStatement.toString();
			else
				str += "null";

			str += "**************************************************\n";
		}

		return str;
	}

	private class PatchItem {
		String manipulationName;
		String sourceFilePath;
		int lineNumber;
		Statement faultyStatement;
		Statement ingredientStatement;

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof PatchItem))
				return false;
			PatchItem item = (PatchItem) o;

			String st1 = this.ingredientStatement.toString();
			String st2 = item.ingredientStatement.toString();

			if (this.manipulationName.equalsIgnoreCase("Delete"))
				st1 = "@";
			if (item.manipulationName.equalsIgnoreCase("Delete"))
				st2 = "@";

			String a = this.manipulationName + "#" + this.sourceFilePath + "#" + this.lineNumber + "#"
					+ this.faultyStatement.toString() + "#" + st1;
			String b = item.manipulationName + "#" + item.sourceFilePath + "#" + item.lineNumber + "#"
					+ item.faultyStatement.toString() + "#" + st2;
			return a.equals(b);
		}

		@Override
		public int hashCode() {
			String st = "@";
			if (ingredientStatement != null && !manipulationName.equalsIgnoreCase("Delete"))
				st = this.ingredientStatement.toString();

			String a = this.manipulationName + "#" + this.sourceFilePath + "#" + this.lineNumber + "#"
					+ this.faultyStatement.toString() + "#" + st;
			return a.hashCode();
		}
	}
}
