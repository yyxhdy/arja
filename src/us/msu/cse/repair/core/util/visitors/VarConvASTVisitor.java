package us.msu.cse.repair.core.util.visitors;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.ThisExpression;

public class VarConvASTVisitor extends ASTVisitor {
	Map<String, String> varMatchMap;
	Map<String, String> thisVarMatchMap;
	Map<String, String> superVarMatchMap;

	List<Integer> varIDs;
	int varCount;

	public VarConvASTVisitor(Map<String, String> varMatchMap, Map<String, String> thisVarMatchMap,
			Map<String, String> superVarMatchMap, List<Integer> varIDs) {
		this.varMatchMap = varMatchMap;
		this.thisVarMatchMap = thisVarMatchMap;
		this.superVarMatchMap = superVarMatchMap;

		this.varIDs = varIDs;
		this.varCount = 0;
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

}