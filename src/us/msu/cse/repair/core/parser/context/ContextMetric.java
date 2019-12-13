package us.msu.cse.repair.core.parser.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.util.Helper;
import us.msu.cse.repair.core.util.visitors.AllStatementASTVisitor;

public class ContextMetric {
	final int winSize = 5;
	
	ModificationPoint mp;
	Map<IMethodBinding, MethodDeclaration> methodDeclarations;
	Statement ingredient;
	
	UsedVarsExtractor uve;
	
	public Set<String> usedVars_mp;
	public Set<String> usedVars_ing;
	
	public Set<String> usedVars_before;
	public Set<String> usedVars_after;
	
	List<ASTNode> beforeNodes;
	List<ASTNode> afterNodes;
	

	public ContextMetric(Statement ingredient, ModificationPoint mp,
			Map<IMethodBinding, MethodDeclaration> methodDeclarations) {
		this.mp = mp;
		this.methodDeclarations = methodDeclarations;
		this.ingredient = ingredient;
		
		uve = new UsedVarsExtractor(mp, methodDeclarations);
		usedVars_mp = uve.extract(mp.getStatement());
		usedVars_ing = uve.extract(ingredient);
		
		
		getExps_Before_After();
		getUsedVars_Before();
		getUsedVars_After();
	}
	
	public double getReplacementSimilarity() {
		int count1 = 0;
		int count2 = 0;
		for (String st : usedVars_mp) {
			if (usedVars_ing.contains(st)) {
				count1++;
			}
			count2++;
		}
		for (String st : usedVars_ing) {
			if (!usedVars_mp.contains(st))
				count2++;
		}
		
		if (count2 == 0)
			return 1;

		return ((double) count1) / count2;
	}
	
	public double getInsertionRelevance() {
		Set<String> usedVars_after2 = new HashSet<String>();
		usedVars_after2.addAll(usedVars_after);
		usedVars_after2.addAll(usedVars_mp);

		int coveredByBefore = 0;
		int coveredByAfter = 0;

		for (String st : usedVars_ing) {
			if (usedVars_before.contains(st))
				coveredByBefore++;
			if (usedVars_after2.contains(st))
				coveredByAfter++;
		}
		
		if (usedVars_ing.size() == 0)
			return 0;
		
		return 0.5 * ((double) coveredByBefore / usedVars_ing.size() + 
				(double) coveredByAfter / usedVars_ing.size());
	}
	
	
	void getExps_Before_After() {
		beforeNodes = new ArrayList<ASTNode>();
		afterNodes = new ArrayList<ASTNode>();
		Statement statement = mp.getStatement();
		MethodDeclaration md = Helper.getMethodDeclaration(statement);
		
		if (md == null)
			return;
		
		AllStatementASTVisitor visitor = new AllStatementASTVisitor();
		md.accept(visitor);
		
		List<Statement> stList = visitor.getStatements();
		int index = stList.indexOf(statement);
		
		int cb = 0;
		int cf = 0;
		for (int i = index - 1; i >= 0; i--) {
			if (cb >= winSize)
				break;
			
			Statement st = stList.get(i);
			if (Helper.isNoVarStatement(st))
				continue;
			else {
				ASTNode exp = Helper.getExpression(st);
				if (exp == null) {
					cb++;
					beforeNodes.add(st);
				}
				else
					beforeNodes.add(exp);
			}
		}
		
		for (int i = index + 1; i < stList.size(); i++) {
			if (cf >= winSize)
				break;
			
			Statement st = stList.get(i);
			if (Helper.isNoVarStatement(st))
				continue;
			else {
				ASTNode exp = Helper.getExpression(st);
				if (exp == null) {
					cf++;
					afterNodes.add(st);
				}
				else
					afterNodes.add(exp);
			}
		}
	}
	
	void getUsedVars_Before() {
		usedVars_before = new HashSet<String>();
		
		for (ASTNode node : beforeNodes) {
			Set<String> uvs = uve.extract(node);
			usedVars_before.addAll(uvs);
		}
	}
	
	void getUsedVars_After() {
		usedVars_after = new HashSet<String>();
		for (ASTNode node : afterNodes) {
			Set<String> uvs = uve.extract(node);
			usedVars_after.addAll(uvs);
		}
	}
}
