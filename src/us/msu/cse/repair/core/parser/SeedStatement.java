package us.msu.cse.repair.core.parser;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Statement;

public class SeedStatement {
	Statement statement;

	public SeedStatement(Statement statement) {
		this.statement = statement;
	}

	public Statement getStatement() {
		return statement;
	}

	public void setStatement(Statement statement) {
		this.statement = statement;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SeedStatement))
			return false;
		SeedStatement ss = (SeedStatement) o;
		return statement.subtreeMatch(new ASTMatcher(true), ss.getStatement());

	}

	@Override
	public int hashCode() {
		return statement.toString().hashCode();
	}

}
