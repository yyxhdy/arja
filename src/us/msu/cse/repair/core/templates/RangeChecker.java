package us.msu.cse.repair.core.templates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;

public class RangeChecker {	
	List<Expression> indexes;
	List<String> types;
	List<Expression> expressions;
	
	public RangeChecker() {
		indexes = new ArrayList<Expression>();
		types = new ArrayList<String>();
		expressions = new ArrayList<Expression>();
	}
	
	public void add(Expression node, Expression index, String type) {
		expressions.add(node);
		indexes.add(index);
		types.add(type);
	}
	
	public Expression getIndex(int i) {
		return indexes.get(i);
	}
	
	public String getType(int i) {
		return types.get(i);
	}
	
	public Expression getExpression(int i) {
		return expressions.get(i);
	}
	
	public int getSize() {
		return expressions.size();
	}
}
