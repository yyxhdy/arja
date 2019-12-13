package us.msu.cse.repair.core.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import us.msu.cse.repair.core.util.visitors.VarMethodInfoASTVisitor;

public class VarMethodInfoExtractor {
	ASTNode node;

	Map<String, VarInfo> vars;
	Map<String, VarInfo> thisVars;
	Map<String, VarInfo> superVars;

	Map<String, MethodInfo> methods;
	Map<String, MethodInfo> thisMethods;
	Map<String, MethodInfo> superMethods;

	List<Integer> varIDs;
	Map<String, List<Integer>> methodIDs;
	Map<String, List<Integer>> superMethodIDs;

	public VarMethodInfoExtractor(ASTNode node) {
		this.node = node;

		this.vars = new HashMap<String, VarInfo>();
		this.thisVars = new HashMap<String, VarInfo>();
		this.superVars = new HashMap<String, VarInfo>();

		this.methods = new HashMap<String, MethodInfo>();
		this.thisMethods = new HashMap<String, MethodInfo>();
		this.superMethods = new HashMap<String, MethodInfo>();

		this.varIDs = new ArrayList<Integer>();

		methodIDs = new HashMap<String, List<Integer>>();
		superMethodIDs = new HashMap<String, List<Integer>>();
	}

	public void extract() {
		VarMethodInfoASTVisitor visitor = new VarMethodInfoASTVisitor(vars, thisVars, superVars, methods, thisMethods,
				superMethods, varIDs, methodIDs, superMethodIDs);
		node.accept(visitor);
	}

	public List<Integer> getVarIDs() {
		return varIDs;
	}

	public Map<String, List<Integer>> getMethodIDs() {
		return methodIDs;
	}

	public Map<String, List<Integer>> getSuperMethodIDs() {
		return superMethodIDs;
	}

	public Map<String, VarInfo> getVars() {
		return vars;
	}

	public Map<String, VarInfo> getThisVars() {
		return thisVars;
	}

	public Map<String, VarInfo> getSuperVars() {
		return superVars;
	}

	public Map<String, MethodInfo> getMethods() {
		return methods;
	}

	public Map<String, MethodInfo> getThisMethods() {
		return thisMethods;
	}

	public Map<String, MethodInfo> getSuperMethods() {
		return superMethods;
	}

}
