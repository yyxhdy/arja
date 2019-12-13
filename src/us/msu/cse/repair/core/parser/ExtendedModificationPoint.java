package us.msu.cse.repair.core.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.Statement;

public class ExtendedModificationPoint extends ModificationPoint {

	List<Statement> replaceIngredients;
	List<Statement> insertIngredients;	
	
	Set<IMethodBinding> availableStaticMethods;
	Set<IVariableBinding> availableStaticFields;
	List<NumberLiteral> nearbyNumberLiterals;
	
	List<Expression> availableBooleanExpressions;
	
	public ExtendedModificationPoint(ModificationPoint mp) {
		this.declaredFields = mp.declaredFields;
		this.delcaredMethods = mp.delcaredMethods;
		this.ingredients = mp.ingredients;
		this.inheritedFields = mp.inheritedFields;
		this.inheritedMethods = mp.inheritedMethods;
		this.isInStaticMethod = mp.isInStaticMethod;
		this.lcNode = mp.lcNode;
		this.localVars = mp.localVars;
		this.outerFields = mp.outerFields;
		this.outerMethods = mp.outerMethods;
		this.sourceFilePath = mp.sourceFilePath;
		this.statement = mp.statement;
		this.suspValue = mp.suspValue;
		
		this.replaceIngredients = new ArrayList<Statement>();
		this.insertIngredients = new ArrayList<Statement>();
	}
	
	public List<Statement> getReplaceIngredients() {
		return replaceIngredients;
	}
	
	public List<Statement> getInsertIngredients() {
		return insertIngredients;
	}
	
	public Set<IMethodBinding> getAvailableStaticMethods() {
		return availableStaticMethods;
	}
	
	public Set<IVariableBinding> getAvailableStaticFields() {
		return availableStaticFields;
	}
	
	public void setReplaceIngredients(List<Statement> replaceIngredients) {
		this.replaceIngredients = replaceIngredients;
	}
	
	public void setInsertIngredients(List<Statement> insertIngredients) {
		this.insertIngredients = insertIngredients;
	}
	
	public void setAvailableStaticMethods(Set<IMethodBinding> availableStaticMethods) {
		this.availableStaticMethods = availableStaticMethods;
	}
	
	public void setAvailableStaticFields(Set<IVariableBinding> availableStaticFields) {
		this.availableStaticFields = availableStaticFields;
	}
	
	public void setNearbyNumberLiterals(List<NumberLiteral> nearbyNumberLiterals) {
		this.nearbyNumberLiterals = nearbyNumberLiterals;
	}
	
	public List<NumberLiteral> getNearbyNumberLiterals() {
		return this.nearbyNumberLiterals;
	}
	
	public void setAvailableBooleanExpressions(List<Expression> availableBooleanExpressions) {
		this.availableBooleanExpressions = availableBooleanExpressions;
	}
	
	public List<Expression> getAvailableBooleanExpressions() {
		return this.availableBooleanExpressions;
	}
	
	public void addReplaceIngredient(Statement statement) {
		this.replaceIngredients.add(statement);
	}
	
	public void addInsertIngredient(Statement statement) {
		this.insertIngredients.add(statement);
	}
}
