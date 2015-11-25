# HW3 Code-Review

+ Good and generous use of comments!

+ Nice separation with Expr and Stmt visitors, but...
- ...unfortunately this introduces hidden coupling between StmtInformationVisitor and ExprInformationVisitor,
  specifically through the "currentClassSymbol" and "currentMethodSymbol" fields.
  These should probably be private, as they concern state about the inner workings of the visitor.
  As a result, StmtInformationVisitor and ExprInformationVisitor will have to be used according
  to a hidden "protocol". If ExprInformationVisitor were to be used by any other class, it could not
  know about the hidden dependency on "currentClassSymbol". An easy fix would be to pass the current
  enclosing Symbol (method or class) as the second argument to visit, this way the dependency is at least visible.

  The same thing ofcourse holds for ExprCheckVisitor and StmtCheckVisitor.

- The enriching process in StmtInformationVisitor is kinda working around
  the visitor pattern.

- Variable names border on cryptic at times. "sIV", "eIV", "sCV", "eCV"
