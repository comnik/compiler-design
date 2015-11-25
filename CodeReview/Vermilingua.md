# HW3 Code Review

+ Documentation of implicit assumptions in SemanticAnalyzer, regarding
  the order of checks.

+ Clean separation of concerns (Unknown type checking, symtab generation, etc...)

+ Nice abstraction of symbol table and scoping concerns into the SymbolTable interface.

+ Good use of nested classes for things that are of no concern to other classes.
  (CheckReturnVisitor in CheckTypeVisitor)

~ Not sure about the usefulness of the CheckTypeVistor.Scope abstraction,
  because one could just pass the respective symbol table down directly.

~ Inheritance check kinda drops out of the visitor pattern for no real reason,
  it's probably easier for other people to understand the code, the more uniform it is.
  (This is very debatable and not a real concern.)

~ Inheritance check is more complicated than needed, it suffices to recursively
  check each classes inheritance chain for itself, in isolation.

~ Not quite following common code style guidelines (argument spacing, curly brackets, ...)

- Not particularly generous with brackets in general. This can cause problems
  when reordering code later.

- The INVALID_START_POINT check will fail if a superclass of "Main"
  implements "main".
