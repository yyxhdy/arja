package us.msu.cse.repair.core.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import us.msu.cse.repair.core.util.Helper;

public class LocalVarDetector {
	List<ModificationPoint> modificationPoints;

	public LocalVarDetector(List<ModificationPoint> modificationPoints) {
		this.modificationPoints = modificationPoints;
	}

	public void detect() {
		for (ModificationPoint mp : modificationPoints)
			detectLocalVars(mp);
	}

	void detectLocalVars(ModificationPoint mp) {

		ASTNode curNode = mp.getStatement();

		Map<String, VarInfo> localVars = new HashMap<String, VarInfo>();

		while (!(curNode.getParent() instanceof MethodDeclaration || curNode.getParent() instanceof Initializer)) {
			if (curNode.getParent() instanceof Block || curNode.getParent() instanceof SwitchStatement) {
				StructuralPropertyDescriptor property = curNode.getLocationInParent();
				List<?> statements = (List<?>) (curNode.getParent().getStructuralProperty(property));

				int index = statements.indexOf(curNode);
				// i<index or i<=index
				for (int i = 0; i < index; i++) {
					Statement st = (Statement) statements.get(i);
					if (st instanceof VariableDeclarationStatement)
						extractVarDecl((VariableDeclarationStatement) st, localVars);
				}
			} else if (curNode.getParent() instanceof ForStatement)
				extractVarDecl((ForStatement) curNode.getParent(), localVars);
			else if (curNode.getParent() instanceof EnhancedForStatement)
				extractVarDecl((EnhancedForStatement) curNode.getParent(), localVars);
			else if (curNode.getParent() instanceof CatchClause)
				extractVarDecl((CatchClause) curNode.getParent(), localVars);

			curNode = curNode.getParent();

			/*
			 * System.out.println(curNode== null);
			 * System.out.println(curNode.toString());
			 */
		}

		if (curNode.getParent() instanceof MethodDeclaration) {
			MethodDeclaration md = (MethodDeclaration) curNode.getParent();
			extractVarDecl(md, localVars);
		}

		mp.setLocalVars(localVars);
	}

	void extractVarDecl(VariableDeclarationStatement vs, Map<String, VarInfo> localVars) {
		List<VariableDeclarationFragment> fragments = Helper.getVariableDeclarationFragments(vs);

		for (VariableDeclarationFragment fragment : fragments) {
			IVariableBinding vb = fragment.resolveBinding();

			if (vb != null) {
				String name = vb.getName();
				VarInfo vi = Helper.getVarInfo(vb);
				localVars.put(name, vi);
			} else {
				System.out.println("The binding of var declaration is null!");
			}
		}
	}

	void extractVarDecl(MethodDeclaration md, Map<String, VarInfo> localVars) {
		for (Object obj : md.parameters()) {
			SingleVariableDeclaration vd = (SingleVariableDeclaration) obj;
			IVariableBinding vb = vd.resolveBinding();
			if (vb != null) {
				String name = vb.getName();
				VarInfo vi = Helper.getVarInfo(vb);
				localVars.put(name, vi);
			} else {
				System.out.println("The binding of single var declaration is null!");
			}
		}
	}

	@SuppressWarnings("rawtypes")
	void extractVarDecl(ForStatement fs, Map<String, VarInfo> localVars) {
		List ins = fs.initializers();
		if (ins.size() > 0 && ins.get(0) instanceof VariableDeclarationExpression) {
			VariableDeclarationExpression vde = (VariableDeclarationExpression) ins.get(0);

			List<VariableDeclarationFragment> fragments = Helper.getVariableDeclarationFragments(vde);

			for (VariableDeclarationFragment fragment : fragments) {
				IVariableBinding vb = fragment.resolveBinding();
				if (vb != null) {
					String name = vb.getName();
					VarInfo vi = Helper.getVarInfo(vb);
					localVars.put(name, vi);
				} else {
					System.out.println("The binding of var declaration of for is null!");
				}
			}
		}
	}

	void extractVarDecl(EnhancedForStatement efs, Map<String, VarInfo> localVars) {
		SingleVariableDeclaration svd = efs.getParameter();
		IVariableBinding vb = svd.resolveBinding();

		if (vb != null) {
			String name = vb.getName();
			VarInfo vi = Helper.getVarInfo(vb);

			localVars.put(name, vi);
		} else {
			System.out.println("The binding of var declaration of enchance for is null!");
		}
	}

	void extractVarDecl(CatchClause cc, Map<String, VarInfo> localVars) {
		SingleVariableDeclaration svd = cc.getException();
		IVariableBinding vb = svd.resolveBinding();

		if (vb != null) {
			String name = vb.getName();
			VarInfo vi = Helper.getVarInfo(vb);

			localVars.put(name, vi);
		} else {
			System.out.println("The binding of var declaration of catch is null!");
		}
	}
}
