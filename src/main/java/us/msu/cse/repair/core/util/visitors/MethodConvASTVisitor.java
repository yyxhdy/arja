package us.msu.cse.repair.core.util.visitors;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

public class MethodConvASTVisitor extends ASTVisitor {
	Map<String, String> methodMatchMap;
	Map<String, String> thisMethodMatchMap;
	Map<String, String> superMethodMatchMap;

	Map<String, List<Integer>> methodIDs;
	Map<String, List<Integer>> superMethodIDs;

	Map<List<Integer>, String> idMethodMapping;

	int methodCount;

	public MethodConvASTVisitor(Map<String, String> methodMatchMap, Map<String, String> thisMethodMatchMap,
			Map<String, String> superMethodMatchMap, Map<String, List<Integer>> methodIDs,
			Map<String, List<Integer>> superMethodIDs) {
		this.methodMatchMap = methodMatchMap;
		this.thisMethodMatchMap = thisMethodMatchMap;
		this.superMethodMatchMap = superMethodMatchMap;
		this.methodIDs = methodIDs;
		this.superMethodIDs = superMethodIDs;

		this.methodCount = 0;
		constructIDMethodMapping();
	}

	void constructIDMethodMapping() {
		idMethodMapping = new HashMap<List<Integer>, String>();
		for (Map.Entry<String, String> entry : methodMatchMap.entrySet()) {
			String key = entry.getKey();
			List<Integer> idList = methodIDs.get(key);
			idMethodMapping.put(idList, entry.getValue());
		}
		for (Map.Entry<String, String> entry : thisMethodMatchMap.entrySet()) {
			String key = entry.getKey();
			List<Integer> idList = methodIDs.get(key);
			idMethodMapping.put(idList, entry.getValue());
		}
		for (Map.Entry<String, String> entry : superMethodMatchMap.entrySet()) {
			String key = entry.getKey();
			List<Integer> idList = superMethodIDs.get(key);
			idMethodMapping.put(idList, entry.getValue());
		}
	}

	@Override
	public boolean visit(MethodInvocation mi) {
		for (Map.Entry<List<Integer>, String> entry : idMethodMapping.entrySet()) {
			List<Integer> idList = entry.getKey();
			String s = entry.getValue();

			if (Collections.binarySearch(idList, methodCount) >= 0) {
				mi.getName().setIdentifier(s);
				break;
			}
		}

		methodCount++;
		return true;
	}

	@Override
	public boolean visit(SuperMethodInvocation smi) {
		for (Map.Entry<List<Integer>, String> entry : idMethodMapping.entrySet()) {
			List<Integer> idList = entry.getKey();
			String s = entry.getValue();

			if (Collections.binarySearch(idList, methodCount) >= 0) {
				smi.getName().setIdentifier(s);
				break;
			}
		}

		methodCount++;
		return true;
	}
}