/**
 * <h1>Predefined</h1>
 *
 * <p>Enter the predefined Pascal types, identifiers, and constants
 * into the symbol table.</p>
 *
 * <p>Copyright (c) 2024 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
package intermediate.symtab;

import java.util.ArrayList;

import intermediate.symtab.SymtabEntry.Kind;
import intermediate.symtab.SymtabEntry.Routine;
import intermediate.type.*;

import static intermediate.symtab.SymtabEntry.Kind.*;
import static intermediate.symtab.SymtabEntry.Routine.*;
import static intermediate.type.Typespec.Form.*;

public class Predefined
{
    // Predefined types.
    public static Typespec integerType;
    public static Typespec realType;
    public static Typespec booleanType;
    public static Typespec charType;
    public static Typespec stringType;
    public static Typespec undefinedType;

    // Predefined identifiers.
    public static SymtabEntry integerEntry;
    public static SymtabEntry realEntry;
    public static SymtabEntry booleanEntry;
    public static SymtabEntry charEntry;
    public static SymtabEntry stringEntry;
    public static SymtabEntry falseEntry;
    public static SymtabEntry trueEntry;
    public static SymtabEntry readEntry;
    public static SymtabEntry readlnEntry;
    public static SymtabEntry writeEntry;
    public static SymtabEntry writelnEntry;
    public static SymtabEntry absEntry;
    public static SymtabEntry arctanEntry;
    public static SymtabEntry chrEntry;
    public static SymtabEntry cosEntry;
    public static SymtabEntry eofEntry;
    public static SymtabEntry eolnEntry;
    public static SymtabEntry expEntry;
    public static SymtabEntry lnEntry;
    public static SymtabEntry oddEntry;
    public static SymtabEntry ordEntry;
    public static SymtabEntry predEntry;
    public static SymtabEntry roundEntry;
    public static SymtabEntry sinEntry;
    public static SymtabEntry sqrEntry;
    public static SymtabEntry sqrtEntry;
    public static SymtabEntry succEntry;
    public static SymtabEntry truncEntry;

    /**
     * Initialize a symbol table stack with predefined identifiers.
     * @param symTab the symbol table stack to initialize.
     */
    public static void initialize(SymtabStack symTabStack)
    {
        initializeTypes(symTabStack);
        initializeConstants(symTabStack);
        initializeStandardRoutines(symTabStack);
    }

    /**
     * Initialize the predefined types.
     * @param symTabStack the symbol table stack to initialize.
     */
    private static void initializeTypes(SymtabStack symTabStack)
    {
        // Type integer.
        integerEntry = symTabStack.enterLocal("integer", TYPE);
        integerType = new Typespec(SCALAR);
        integerType.setIdentifier(integerEntry);
        integerEntry.setType(integerType);

        // Type real.
        realEntry = symTabStack.enterLocal("real", TYPE);
        realType = new Typespec(SCALAR);
        realType.setIdentifier(realEntry);
        realEntry.setType(realType);

        // Type boolean.
        booleanEntry = symTabStack.enterLocal("boolean", TYPE);
        booleanType = new Typespec(ENUMERATION);
        booleanType.setIdentifier(booleanEntry);
        booleanEntry.setType(booleanType);

        // Type char.
        charEntry = symTabStack.enterLocal("char", TYPE);
        charType = new Typespec(SCALAR);
        charType.setIdentifier(charEntry);
        charEntry.setType(charType);

        // Type string.
        stringEntry = symTabStack.enterLocal("string", TYPE);
        stringType = new Typespec(SCALAR);
        stringType.setIdentifier(stringEntry);
        stringEntry.setType(stringType);

        // Undefined type.
        undefinedType = new Typespec(SCALAR);
    }

    /**
     * Initialize the predefined constant.
     * @param symTabStack the symbol table stack to initialize.
     */
    private static void initializeConstants(SymtabStack symTabStack)
    {
        // Boolean enumeration constant false.
        falseEntry = symTabStack.enterLocal("false", ENUMERATION_CONSTANT);
        falseEntry.setType(booleanType);
        falseEntry.setValue(0);

        // Boolean enumeration constant true.
        trueEntry = symTabStack.enterLocal("true", ENUMERATION_CONSTANT);
        trueEntry.setType(booleanType);
        trueEntry.setValue(1);

        // Add false and true to the boolean enumeration type.
        ArrayList<SymtabEntry> constants = booleanType.getEnumerationConstants();
        constants.add(falseEntry);
        constants.add(trueEntry);
    }

    /**
     * Initialize the standard procedures and functions.
     * @param symTabStack the symbol table stack to initialize.
     */
    private static void initializeStandardRoutines(SymtabStack symTabStack)
    {
        readEntry    = enterStandard(symTabStack, PROCEDURE, "read",    READ);
        readlnEntry  = enterStandard(symTabStack, PROCEDURE, "readln",  READLN);
        writeEntry   = enterStandard(symTabStack, PROCEDURE, "write",   WRITE);
        writelnEntry = enterStandard(symTabStack, PROCEDURE, "writeln", WRITELN);

        absEntry    = enterStandard(symTabStack, FUNCTION, "abs",    ABS);
        arctanEntry = enterStandard(symTabStack, FUNCTION, "arctan", ARCTAN);
        chrEntry    = enterStandard(symTabStack, FUNCTION, "chr",    CHR);
        cosEntry    = enterStandard(symTabStack, FUNCTION, "cos",    COS);
        eofEntry    = enterStandard(symTabStack, FUNCTION, "eof",    EOF);
        eolnEntry   = enterStandard(symTabStack, FUNCTION, "eoln",   EOLN);
        expEntry    = enterStandard(symTabStack, FUNCTION, "exp",    EXP);
        lnEntry     = enterStandard(symTabStack, FUNCTION, "ln",     LN);
        oddEntry    = enterStandard(symTabStack, FUNCTION, "odd",    ODD);
        ordEntry    = enterStandard(symTabStack, FUNCTION, "ord",    ORD);
        predEntry   = enterStandard(symTabStack, FUNCTION, "pred",   PRED);
        roundEntry  = enterStandard(symTabStack, FUNCTION, "round",  ROUND);
        sinEntry    = enterStandard(symTabStack, FUNCTION, "sin",    SIN);
        sqrEntry    = enterStandard(symTabStack, FUNCTION, "sqr",    SQR);
        sqrtEntry   = enterStandard(symTabStack, FUNCTION, "sqrt",   SQRT);
        succEntry   = enterStandard(symTabStack, FUNCTION, "succ",   SUCC);
        truncEntry  = enterStandard(symTabStack, FUNCTION, "trunc",  TRUNC);
    }

    /**
     * Enter a standard procedure or function into the symbol table stack.
     * @param symTabStack the symbol table stack to initialize.
     * @param kind either PROCEDURE or FUNCTION.
     * @param name the procedure or function name.
     * @param routineCode the routine code.
     */
    private static SymtabEntry enterStandard(SymtabStack symTabStack,
                                             Kind kind, String name,
                                             Routine routineCode)
    {
        SymtabEntry routineEntry = symTabStack.enterLocal(name, kind);
        routineEntry.setRoutineCode(routineCode);

        return routineEntry;
    }
}
