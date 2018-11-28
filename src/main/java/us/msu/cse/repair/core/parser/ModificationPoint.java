package us.msu.cse.repair.core.parser;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Statement;

public class ModificationPoint {
	Statement statement;

	String sourceFilePath;

	double suspValue;
	LCNode lcNode;

	List<Statement> ingredients;

	Map<String, VarInfo> declaredFields;
	Map<String, VarInfo> inheritedFields;
	Map<String, VarInfo> outerFields;

	Map<String, VarInfo> localVars;

	Map<String, MethodInfo> delcaredMethods;
	Map<String, MethodInfo> inheritedMethods;
	Map<String, MethodInfo> outerMethods;

	boolean isInStaticMethod;

	public void setSuspValue(double suspValue) {
		this.suspValue = suspValue;
	}

	public double getSuspValue() {
		return this.suspValue;
	}

	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	public String getSourceFilePath() {
		return this.sourceFilePath;
	}

	public void setStatement(Statement statement) {
		this.statement = statement;
	}

	public Statement getStatement() {
		return this.statement;
	}

	public void setLCNode(LCNode lcNode) {
		this.lcNode = lcNode;
	}

	public LCNode getLCNode() {
		return this.lcNode;
	}

	public void setIngredients(List<Statement> ingredients) {
		this.ingredients = ingredients;
	}

	public List<Statement> getIngredients() {
		return this.ingredients;
	}

	public void setDeclaredFields(Map<String, VarInfo> declaredFields) {
		this.declaredFields = declaredFields;
	}

	public Map<String, VarInfo> getDeclaredFields() {
		return this.declaredFields;
	}

	public void setInheritedFields(Map<String, VarInfo> inheritedFields) {
		this.inheritedFields = inheritedFields;
	}

	public Map<String, VarInfo> getInheritedFields() {
		return this.inheritedFields;
	}

	public void setOuterFields(Map<String, VarInfo> outerFields) {
		this.outerFields = outerFields;
	}

	public Map<String, VarInfo> getOuterFields() {
		return this.outerFields;
	}

	public void setDeclaredMethods(Map<String, MethodInfo> declaredMethods) {
		this.delcaredMethods = declaredMethods;
	}

	public Map<String, MethodInfo> getDeclaredMethods() {
		return this.delcaredMethods;
	}

	public void setInheritedMethods(Map<String, MethodInfo> inheritedMethods) {
		this.inheritedMethods = inheritedMethods;
	}

	public Map<String, MethodInfo> getInheritedMethods() {
		return this.inheritedMethods;
	}

	public void setOuterMethods(Map<String, MethodInfo> outerMethods) {
		this.outerMethods = outerMethods;
	}

	public Map<String, MethodInfo> getOuterMethods() {
		return this.outerMethods;
	}

	public void setLocalVars(Map<String, VarInfo> localVars) {
		this.localVars = localVars;
	}

	public Map<String, VarInfo> getLocalVars() {
		return this.localVars;
	}

	public void setInStaticMethod(boolean isInStaticMethod) {
		this.isInStaticMethod = isInStaticMethod;
	}

	public boolean isInStaticMethod() {
		return this.isInStaticMethod;
	}

}
