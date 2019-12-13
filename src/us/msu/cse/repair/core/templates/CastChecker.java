package us.msu.cse.repair.core.templates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.CastExpression;

public class CastChecker {
	List<CastExpression> castExpressions;
	
	public CastChecker() {
		this.castExpressions = new ArrayList<CastExpression>();
	}
	
	public List<CastExpression> getCastExpressions() {
		return this.castExpressions;
	}
	
	public int getSize() {
		return this.castExpressions.size();
	}
	
	public void add(CastExpression ce) {
		this.castExpressions.add(ce);
	}
}
