grammar Javali; // parser grammar, parses streams of tokens

@header {
	// Java header
	package cd.frontend.parser;
}


// Start node
start
	: unit EOF
	;


// PARSER RULES

// types
primitiveType
	: 'int'
	| 'boolean'
	;

type
	: primitiveType
	| referenceType
	;

referenceType
	: Identifier
	| arrayType
	;

arrayType
	: Identifier '[' ']'
	| primitiveType '[' ']'
	;

// program structure
 unit
 	: classDecl+
 	;

classDecl
	: 'class' Identifier ('extends' Identifier)? '{' memberList '}'
	;

memberList
	: (varDecl | methodDecl)*
	;

varDecl
	: type Identifier (',' Identifier)* ';'
    ;

methodDecl
	: (type | 'void') Identifier '(' formalParamList? ')' '{' varDecl* stmt* '}'
	;

formalParamList
	: type Identifier (',' type Identifier)*
	;

// statements
stmt
	: assignmentStmt
	| methodCallStmt
	| ifStmt
	| whileStmt
	| returnStmt
	| writeStmt
	;

stmtBlock
	: '{' stmt* '}'
	;

methodCallStmt
	: methodCallExpr ';'
	;

assignmentStmt
	: identAccess '=' (expr | newExpr | readExpr) ';'
	;

writeStmt
	: ( 'write' '(' expr ')' | 'writeln' '(' ')' ) ';'
	;

ifStmt
	: 'if' '(' expr ')' stmtBlock ('else' stmtBlock)?
	;

whileStmt
	: 'while' '(' expr ')' stmtBlock
	;

returnStmt
	: 'return' expr? ';'
	;

// expressions
newExpr
	: 'new' (Identifier '(' ')' | Identifier '[' expr ']' | primitiveType '[' expr  ']')
	;

readExpr
	: 'read' '(' ')'
	;

methodCallExpr
	: Identifier '(' actualParamList? ')'
	| identAccess '.' Identifier '(' actualParamList? ')'
	;

actualParamList
	: expr (',' expr)*
	;

identAccess
	: Identifier # Identifier
	| 'this' # This
	| identAccess '[' expr ']' # ArrayAccess
	| identAccess '.' Identifier # FieldAccess
	// Antlr doesn't do indirect recursion, so the code from methodCallExpr is copied here.
	| Identifier '(' actualParamList? ')' # Unqualified
	| identAccess '.' Identifier '(' actualParamList? ')' # Qualified
	;

expr
    : literal # LitExpr
    | identAccess # Var
	| '(' expr ')' # Brackets
	// Operators in order of precedence.
	| ('+' | '-' | '!') expr # UnaryOp
	| '(' referenceType ')' expr # Cast
    | expr ('*' | '/' | '%') expr # BinaryMul
	| expr ('+' | '-') expr # BinaryAdd
	| expr ('<' | '<=' | '>' | '>=') expr # Compare
	| expr ('==' | '!=') expr # Equality
	| expr '&&' expr # LogicalAnd
	| expr '||' expr # LogicalOr
	;

literal
    : Integer # Integer
    | Boolean # Boolean
    | 'null' # Null
    ;

// LEXER RULES

// Java(li) identifiers:
Identifier
	: Letter (Letter|JavaIDDigit)*
	;

Integer
	: Decimal
	| Hex
	;

fragment
Decimal
	: '0'
	| '1'..'9' Digit*
	;

fragment
Digit
	: '0'..'9'
	;

fragment
Hex
	: ('0x' | '0X') HexDigit HexDigit*
	;

fragment
HexDigit
	: Letter
	| JavaIDDigit
	;

Boolean
	: 'true'
	| 'false'
	;

fragment
Letter
	:	'\u0024'
	|	'\u0041'..'\u005a'
	|	'\u005f'
	|	'\u0061'..'\u007a'
	|	'\u00c0'..'\u00d6'
	|	'\u00d8'..'\u00f6'
	|	'\u00f8'..'\u00ff'
	|	'\u0100'..'\u1fff'
	|	'\u3040'..'\u318f'
	|	'\u3300'..'\u337f'
	|	'\u3400'..'\u3d2d'
	|	'\u4e00'..'\u9fff'
	|	'\uf900'..'\ufaff'
	;

fragment
JavaIDDigit
	:	'\u0030'..'\u0039'
	|	'\u0660'..'\u0669'
	|	'\u06f0'..'\u06f9'
	|	'\u0966'..'\u096f'
	|	'\u09e6'..'\u09ef'
	|	'\u0a66'..'\u0a6f'
	|	'\u0ae6'..'\u0aef'
	|	'\u0b66'..'\u0b6f'
	|	'\u0be7'..'\u0bef'
	|	'\u0c66'..'\u0c6f'
	|	'\u0ce6'..'\u0cef'
	|	'\u0d66'..'\u0d6f'
	|	'\u0e50'..'\u0e59'
	|	'\u0ed0'..'\u0ed9'
	|	'\u1040'..'\u1049'
	;

// comments and white space does not produce tokens:
COMMENT
	:	'/*' .*? '*/' -> skip
	;

LINE_COMMENT
	:	'//' ~('\n'|'\r')* -> skip
	;

WS
	:	(' '|'\r'|'\t'|'\u000C'|'\n') -> skip
	;