package us.msu.cse.repair.core.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.Statement;

import us.msu.cse.repair.core.util.Helper;
import us.msu.cse.repair.core.util.visitors.NumberLiteralVisitor;

public class NumberLiteralDetector {
	List<ExtendedModificationPoint> modificationPoints;
	
	Map<IMethodBinding, List<NumberLiteral>> nearbyNumberLiteralMap;
	
	public NumberLiteralDetector(List<ExtendedModificationPoint> modificationPoints) {
		this.modificationPoints = modificationPoints;
		nearbyNumberLiteralMap = new HashMap<IMethodBinding, List<NumberLiteral>>();
	}
	
	public void detect() {
		for (ExtendedModificationPoint mp : modificationPoints)
			detectNumberLiterals(mp);
	}
	
	void detectNumberLiterals(ExtendedModificationPoint mp) {
		Statement statement = mp.getStatement();
		MethodDeclaration md = Helper.getMethodDeclaration(statement);
		
		if (md == null)
			return;
		
		IMethodBinding mb = md.resolveBinding();
		if (mb == null)
			return;
		
		if (nearbyNumberLiteralMap.containsKey(mb)) {
			mp.setNearbyNumberLiterals(nearbyNumberLiteralMap.get(mb));
			return;
		}
				
		NumberLiteralVisitor visitor = new NumberLiteralVisitor();
		md.accept(visitor);
		
		List<NumberLiteral> nearbyNumberLiterals = visitor.getNumberLiterals();
		nearbyNumberLiteralMap.put(mb, nearbyNumberLiterals);
		mp.setNearbyNumberLiterals(nearbyNumberLiterals);
	}
}
