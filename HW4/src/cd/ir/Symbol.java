package cd.ir;

import cd.Config;
import cd.util.Tuple;

import java.util.*;

public abstract class Symbol {
	
	public final String name;
	
	public static abstract class TypeSymbol extends Symbol {
		
		public TypeSymbol(String name) {
			super(name);
		}

		public abstract boolean isReferenceType();
		
		public String toString() {
			return name;
		}

        /** Returns the size of a reference to this type. */
        public int getRefSize() { return Config.SIZEOF_PTR; }

        /** Returns the size of all fields pertaining to this type. */
		public abstract int getFieldSize();

        /** Returns the label of the vtable associated with this type. */
        public abstract String getVtableLabel();

		public abstract TypeSymbol getSuperType();
		
		public boolean isSuperTypeOf(TypeSymbol sub) {
			if (sub == this)
				return true;
			
			if (this instanceof PrimitiveTypeSymbol || sub instanceof PrimitiveTypeSymbol)
				return false; // no hierarchy with primitive types
			
			if (sub == ClassSymbol.nullType && this.isReferenceType())
				return true;
			
			TypeSymbol curr = sub;
			while (curr != null) {
				if (curr == this)
					return true;
				curr = curr.getSuperType();
			}
			return false;
		}
		
	}
	
	public static class PrimitiveTypeSymbol extends TypeSymbol {
		
		/** Symbols for the built-in primitive types */
		public static final PrimitiveTypeSymbol intType = new PrimitiveTypeSymbol("int");
		public static final PrimitiveTypeSymbol voidType = new PrimitiveTypeSymbol("void");
		public static final PrimitiveTypeSymbol booleanType = new PrimitiveTypeSymbol("boolean");

		public PrimitiveTypeSymbol(String name) {
			super(name);
		}		

        @Override
        public int getFieldSize() { return Config.SIZEOF_PTR; }

        @Override
        public String getVtableLabel() {
            throw new RuntimeException("Primitive types don't have a vtable.");
        }

		public boolean isReferenceType() {
			return false;
		}
		
		public TypeSymbol getSuperType() {
			throw new RuntimeException("should not call this on PrimitiveTypeSymbol");
		}

        @Override
        public int getRefSize() {
            if (this == booleanType)
                return 1;

            return super.getRefSize();
        }
	}
	
	public static class ArrayTypeSymbol extends TypeSymbol {
		public final TypeSymbol elementType;
		
		public ArrayTypeSymbol(TypeSymbol elementType) {
			super(elementType.name+"[]");
			this.elementType = elementType;
		}

        public int getFieldSize() { return Config.SIZEOF_PTR; }

        @Override
        public String getVtableLabel() { return null; }

		public boolean isReferenceType() {
			return true;
		}
		
		public TypeSymbol getSuperType() {
			return ClassSymbol.objectType;
		}
		
	}
	
	public static class ClassSymbol extends TypeSymbol {
		public final Ast.ClassDecl ast;
		public ClassSymbol superClass;
		public final VariableSymbol thisSymbol =
			new VariableSymbol("this", this);
		public final Map<String, VariableSymbol> fields = 
			new HashMap<String, VariableSymbol>();
		public final Map<String, MethodSymbol> methods =
			new HashMap<String, MethodSymbol>();

		/** Symbols for the built-in Object and null types */
		public static final ClassSymbol nullType = new ClassSymbol("<null>");
		public static final ClassSymbol objectType = new ClassSymbol("Object"); 
		
		public ClassSymbol(Ast.ClassDecl ast) {
			super(ast.name);
			this.ast = ast;
		}
		
		/** Used to create the default {@code Object} 
		 *  and {@code <null>} types */
		public ClassSymbol(String name) {
			super(name);
			this.ast = null;
		}

        // Go through the fields of the class and sum their sizes.
        public int getFieldSize() {
            // TODO Careful with ordering here.
            int size = fields.keySet().stream()
                    .mapToInt(a -> fields.get(a).type.getFieldSize())
                    .sum();

            if (superClass != objectType) {
                return superClass.getFieldSize() + size;
            } else {
                return size;
            }
        }

        @Override
        public String getVtableLabel() { return this.name; }

		public boolean isReferenceType() { return true; }
		
		public TypeSymbol getSuperType() {
			return superClass;
		}
		
		public VariableSymbol getField(String name) {
			VariableSymbol fsym = fields.get(name);
			if (fsym == null && superClass != null)
				return superClass.getField(name);
			return fsym;
		}
		
		public MethodSymbol getMethod(String name) {
			MethodSymbol msym = methods.get(name);
			if (msym == null && superClass != null)
				return superClass.getMethod(name);
			return msym;
		}

        /** Returns an ordered map representing this classes full vtable. */
        public LinkedHashMap<String,Tuple<ClassSymbol,MethodSymbol>> getVTable() {
            LinkedHashMap<String,Tuple<ClassSymbol,MethodSymbol>> methodMap;
            if (superClass == objectType) {
                methodMap = new LinkedHashMap<String,Tuple<ClassSymbol,MethodSymbol>>();
            } else {
                methodMap = superClass.getVTable();
            }

            // Overwrite with our own implementations.
            this.ast.methods().stream()
                    .forEach(methodDecl -> {
                        methodMap.put(methodDecl.sym.name, new Tuple<ClassSymbol, MethodSymbol>(this,methodDecl.sym));
                    });

            return methodMap;
        }
	}

	public static class MethodSymbol extends Symbol {
		
		public final Ast.MethodDecl ast;
		public final Map<String, VariableSymbol> locals =
			new HashMap<String, VariableSymbol>();
		public final List<VariableSymbol> parameters =
			new ArrayList<VariableSymbol>();
		
		public TypeSymbol returnType;

        public int offset;

		public MethodSymbol(Ast.MethodDecl ast) {
			super(ast.name);
			this.ast = ast;
		}
		
		public String toString() {
			return name + "(...)";
		}
	}
	
	public static class VariableSymbol extends Symbol {
		
		public static enum Kind { PARAM, LOCAL, FIELD };
		public final TypeSymbol type;
		public final Kind kind;
        public int offset;
		
		public VariableSymbol(String name, TypeSymbol type) {
			this(name, type, Kind.PARAM);
		}

		public VariableSymbol(String name, TypeSymbol type, Kind kind) {
			super(name);
			this.type = type;
			this.kind = kind;		
		}
		
		public String toString() {
			return name;
		}
	}

	protected Symbol(String name) {
		this.name = name;
	}

}
