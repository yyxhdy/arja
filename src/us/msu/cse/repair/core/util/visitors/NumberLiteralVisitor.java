package us.msu.cse.repair.core.util.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.NumberLiteral;

public class NumberLiteralVisitor extends ASTVisitor {
	List<NumberLiteral> numberLiterals;
	public NumberLiteralVisitor() {
		numberLiterals = new ArrayList<NumberLiteral>();
	}
	
	@Override
	public boolean visit(NumberLiteral nl) {
		numberLiterals.add(nl);
		return true;
	}
	
	public List<NumberLiteral> getNumberLiterals() {
		return this.numberLiterals;
	}
}
