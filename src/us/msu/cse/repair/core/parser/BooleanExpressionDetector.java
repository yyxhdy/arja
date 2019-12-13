package us.msu.cse.repair.core.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;


import us.msu.cse.repair.core.parser.ingredient.IngredientMode;
import us.msu.cse.repair.core.parser.ingredient.IngredientUtil;
import us.msu.cse.repair.core.util.Helper;
import us.msu.cse.repair.core.util.visitors.BooleanExpressionVisitor;


public class BooleanExpressionDetector {
	
	List<ExtendedModificationPoint> modificationPoints;
	Map<String, CompilationUnit> sourceASTs;
	
	IngredientMode mode;
	
	public  BooleanExpressionDetector(List<ExtendedModificationPoint> modificationPoints,
			Map<String, CompilationUnit> sourceASTs, IngredientMode mode) {
		this.modificationPoints = modificationPoints;
		this.sourceASTs = sourceASTs;
		this.mode = mode;
	}
	
	public  BooleanExpressionDetector(List<ExtendedModificationPoint> modificationPoints,
			Map<String, CompilationUnit> sourceASTs) {
		this(modificationPoints, sourceASTs, IngredientMode.File);
	}
	
	
	
	public void detect() {
		for (ExtendedModificationPoint mp : modificationPoints)
			detectAvailableBooleanExpressions(mp);
	}
	
	void detectAvailableBooleanExpressions(ExtendedModificationPoint mp) {
		if (mode == IngredientMode.File)
			detectAvailableBooleanExpressions_File(mp);
		else if (mode == IngredientMode.Package)
			detectAvailableBooleanExpressions_Package(mp);
		else if (mode == IngredientMode.Application)
			detectAvailableBooleanExpressions_Application(mp);
	}
	
	void detectAvailableBooleanExpressions_File(ExtendedModificationPoint mp) {
		String sourceFilePath = mp.getSourceFilePath();

		CompilationUnit cu = sourceASTs.get(sourceFilePath);

		BooleanExpressionVisitor bev = new BooleanExpressionVisitor();
		cu.accept(bev);

		List<Expression> availableBooleanExpressions = getAvailableBooleanExpressions(bev.getExpressions(), mp);
		mp.setAvailableBooleanExpressions(availableBooleanExpressions);
	}

	void detectAvailableBooleanExpressions_Package(ExtendedModificationPoint mp) {
		String sourceFilePath = mp.getSourceFilePath();
		List<Expression> availableBooleanExpressions = new ArrayList<Expression>();
		for (Map.Entry<String, CompilationUnit> entry : sourceASTs.entrySet()) {
			String srcFilePath = entry.getKey();
			boolean isSamePackage = Helper.isInSamePackage2(sourceFilePath, srcFilePath);
			
			if (!isSamePackage)
				continue;
			
			CompilationUnit cu = entry.getValue();
			BooleanExpressionVisitor bev = new BooleanExpressionVisitor();
			cu.accept(bev);
			availableBooleanExpressions.addAll(bev.getExpressions());
		}
		
		availableBooleanExpressions = getAvailableBooleanExpressions(availableBooleanExpressions, mp);
		mp.setAvailableBooleanExpressions(availableBooleanExpressions);
	}
	
	void detectAvailableBooleanExpressions_Application(ExtendedModificationPoint mp) {
		List<Expression> availableBooleanExpressions = new ArrayList<Expression>();
		for (CompilationUnit cu : sourceASTs.values()) {
			BooleanExpressionVisitor bev = new BooleanExpressionVisitor();
			cu.accept(bev);
			availableBooleanExpressions.addAll(bev.getExpressions());
		}
		availableBooleanExpressions = getAvailableBooleanExpressions(availableBooleanExpressions, mp);
		mp.setAvailableBooleanExpressions(availableBooleanExpressions);
	}
	
	List<Expression> getAvailableBooleanExpressions(List<Expression> booleanExpressions, 
			ExtendedModificationPoint mp) {
		List<Expression> resBooleanExpressions = new ArrayList<Expression>();
		
	/*	System.out.println("st: " + mp.getStatement().toString());
		if (!mp.getStatement().toString().startsWith("if (emptyRange)"))
			return new ArrayList<Expression>();
		
		for (String name : mp.getLocalVars().keySet()) {
			System.out.println("local: " + name);
		}
		
		for (String name : mp.getDeclaredFields().keySet()) {
			System.out.println("d fields: " + name);
		}
		
		for (String name : mp.getInheritedFields().keySet()) {
			System.out.println("i fields: " + name);
		}
		
		for (String name : mp.getOuterFields().keySet()) {
			System.out.println("o fields: " + name);
		}*/
		
		for (Expression exp : booleanExpressions) {
			VarMethodInfoExtractor sie = new VarMethodInfoExtractor(exp);
			sie.extract();
	//		System.out.println("exp: " + exp.toString());

			if (IngredientUtil.isInMethodScope(mp, sie) && IngredientUtil.isInVarScope(mp, sie)) {
				resBooleanExpressions.add(exp);
		//		System.out.println("true");
			}
		/*	else 
				System.out.println("false");
			*/
		
		}
		
		return resBooleanExpressions;
	}
	
}
