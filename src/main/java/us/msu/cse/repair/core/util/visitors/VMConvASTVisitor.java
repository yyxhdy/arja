package us.msu.cse.repair.core.util.visitors;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;

public class VMConvASTVisitor extends ASTVisitor {
	Map<String, String> varMatchMap;
	Map<String, String> thisVarMatchMap;
	Map<String, String> superVarMatchMap;

	Map<String, String> methodMatchMap;
	Map<String, String> thisMethodMatchMap;
	Map<String, String> superMethodMatchMap;

	List<Integer> varIDs;

	Map<String, List<Integer>> methodIDs;
	Map<String, List<Integer>> superMethodIDs;

	Map<List<Integer>, String> idMethodMapping;

	int varCount;
	int methodCount;

	public VMConvASTVisitor(Map<String, String> varMatchMap, Map<String, String> thisVarMatchMap,
			Map<String, String> superVarMatchMap, Map<String, String> methodMatchMap,
			Map<String, String> thisMethodMatchMap, Map<String, String> superMethodMatchMap, List<Integer> varIDs,
			Map<String, List<Integer>> methodIDs, Map<String, List<Integer>> superMethodIDs) {
		this.varMatchMap = varMatchMap;
		this.thisVarMatchMap = thisVarMatchMap;
		this.superVarMatchMap = superVarMatchMap;

		this.methodMatchMap = methodMatchMap;
		this.thisMethodMatchMap = thisMethodMatchMap;
		this.superMethodMatchMap = superMethodMatchMap;

		this.varIDs = varIDs;

		this.methodIDs = methodIDs;
		this.superMethodIDs = superMethodIDs;

		this.varCount = 0;
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
	public boolean visit(FieldAccess fa) {
		Expression expression = fa.getExpression();

		if (expression instanceof ThisExpression) {
			String identifier = fa.getName().getIdentifier();

			if (thisVarMatchMap.containsKey(identifier)) {
				String newIdentifier = thisVarMatchMap.get(identifier);
				fa.getName().setIdentifier(newIdentifier);
			}

		}
		varCount++;
		return true;
	}

	@Override
	public boolean visit(SuperFieldAccess sfa) {
		String identifier = sfa.getName().getIdentifier();
		if (superVarMatchMap.containsKey(identifier)) {
			String newIdentifier = superVarMatchMap.get(identifier);
			sfa.getName().setIdentifier(newIdentifier);
		}

		varCount++;
		return true;
	}

	@Override
	public boolean visit(QualifiedName qn) {
		if (qn.getQualifier() instanceof SimpleName) {
			String identifier = ((SimpleName) qn.getQualifier()).getIdentifier();
			if (varMatchMap.containsKey(identifier)) {
				String newIdentifier = varMatchMap.get(identifier);
				((SimpleName) qn.getQualifier()).setIdentifier(newIdentifier);
			}
		}
		varCount++;
		return true;
	}

	@Override
	public boolean visit(SimpleName sn) {
		String identifier = sn.getIdentifier();
		if (varMatchMap.containsKey(identifier)) {
			if (Collections.binarySearch(varIDs, varCount) >= 0) {
				String newIdentifier = varMatchMap.get(identifier);
				sn.setIdentifier(newIdentifier);
			}
		}

		varCount++;
		return true;
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