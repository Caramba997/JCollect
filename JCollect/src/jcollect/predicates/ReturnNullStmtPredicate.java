package jcollect.predicates;

import java.util.Optional;
import java.util.function.Predicate;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ReturnStmt;

public class ReturnNullStmtPredicate<E> implements Predicate<ReturnStmt> {
	
	@Override
	public boolean test(ReturnStmt stmt) {
		Optional<Expression> exprOpt = stmt.getExpression();
		if (exprOpt.isPresent()) {
			Expression expr = exprOpt.get();
			if (expr.isNullLiteralExpr()) {
				return true;
			}
		}
		return false;
	}

}
