package us.msu.cse.repair.core.manipulation;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.util.Helper;

public class InsertReturnManipulation extends AbstractManipulation {

	int returnInteger = 0;
	boolean returnBoolean = true;

	public InsertReturnManipulation(ModificationPoint mp, Statement ingredStatement, ASTRewrite rewriter) {
		super(mp, ingredStatement, rewriter);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean manipulate() {
		// TODO Auto-generated method stub
		Statement statement = mp.getStatement();
		MethodDeclaration md = Helper.getMethodDeclaration(statement);
		if (md == null || md.isConstructor())
			return false;
		Type type = md.getReturnType2();

		ReturnStatement rs = statement.getAST().newReturnStatement();
		Expression expression = null;
		if (type.isPrimitiveType()) {
			PrimitiveType pt = (PrimitiveType) type;
			String code = pt.getPrimitiveTypeCode().toString();
			if (code.equals("void"))
				expression = null;
			else if (code.equals("boolean"))
				expression = statement.getAST().newBooleanLiteral(returnBoolean);
			else
				expression = statement.getAST().newNumberLiteral(returnInteger + "");
		} else
			expression = statement.getAST().newNullLiteral();

		rs.setExpression(expression);

		IfStatement ifs = statement.getAST().newIfStatement();
		ifs.setThenStatement(rs);
		expression = statement.getAST().newBooleanLiteral(true);
		ifs.setExpression(expression);

		return new InsertBeforeManipulation(mp, ifs, rewriter).manipulate();
	}

	public void setReturnStatus(boolean status) {
		if (status) {
			returnInteger = 0;
			returnBoolean = true;
		} else {
			returnInteger = -1;
			returnBoolean = false;
		}
	}

	public int getReturnInteger() {
		return this.returnInteger;
	}

	public boolean getReturnBoolean() {
		return this.returnBoolean;
	}

}
