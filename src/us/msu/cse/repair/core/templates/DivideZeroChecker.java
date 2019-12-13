package us.msu.cse.repair.core.templates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;

public class DivideZeroChecker {
	List<Expression> divisors;
	
	public DivideZeroChecker() {
		divisors = new ArrayList<Expression>();
	}
	
	public void add(Expression divisor) {
		this.divisors.add(divisor);
	}
	
	public List<Expression> getDivisors() {
		return this.divisors;
	}
	
	public int getSize() {
		return this.divisors.size();
	}
}
