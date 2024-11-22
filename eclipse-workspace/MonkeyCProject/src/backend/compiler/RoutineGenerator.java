/**
 * <h1>RoutineGenerator</h1>
 *
 * <p>Generate object code for a MonkeyC routine (procedure or function).</p>
 *
 * <p>Copyright (c) 2024 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
package backend.compiler;

import java.util.ArrayList;

import intermediate.antlr4.MonkeyCParser;
import intermediate.symtab.Predefined;
import intermediate.symtab.Symtab;
import intermediate.symtab.SymtabEntry;
import intermediate.symtab.SymtabEntry.Kind;
import intermediate.type.Typespec;
import static intermediate.symtab.SymtabEntry.Kind.*;

import static backend.compiler.Directive.*;
import static backend.compiler.Instruction.*;

public class RoutineGenerator
{
    private Compiler compiler;
    private CodeEmitter emitter;

    /**
     * Constructor.
     * @param the parent generator.
     * @param compiler the compiler to use.
     */
    public RoutineGenerator(Compiler compiler)
    {
        this.compiler = compiler;
        emitter = compiler.getCodeEmitter();
    }

    /**
     * Emit code for declared procedures and functions.
     */
    public void emitRoutines(MonkeyCParser.RoutinesPartContext ctx)
    {
        for (MonkeyCParser.RoutineDefinitionContext defnCtx : 
                                                    ctx.routineDefinition())
        {
            SymtabEntry routineEntry = defnCtx.procedureHead() != null 
                    ? defnCtx.procedureHead().routineIdentifier().entry
                    : defnCtx.functionHead().routineIdentifier().entry;
            emitRoutine(routineEntry);
        }
    }
    
    /**
     * Emit code for a declared procedure or function.
     * @param routineEntry the symbol table entry of the routine's name.
     */
    public void emitRoutine(SymtabEntry routineEntry)
    {
        LocalStack localStack = new LocalStack();
        emitter.setLocalStack(localStack);

        emitRoutineHeader(routineEntry);
        emitRoutineLocals(routineEntry);

        // Generate code to allocate any arrays, records, and strings.
        StructuredDataGenerator structuredCode = 
                                    new StructuredDataGenerator(compiler);
        structuredCode.emitData(routineEntry);

        // Emit code for the compound statement.
        MonkeyCParser.CompoundStatementContext stmtCtx = 
            (MonkeyCParser.CompoundStatementContext) routineEntry.getExecutable();
        compiler.visit(stmtCtx);
        
        emitRoutineReturn(routineEntry);
        emitRoutineEpilogue();
    }

    /**
     * Emit the routine header.
     * @param routineEntry the symbol table entry of the routine's name.
     */
    private void emitRoutineHeader(SymtabEntry routineEntry)
    {
        String routineName = routineEntry.getName();
        ArrayList<SymtabEntry> parmEntries = routineEntry.getRoutineParameters();
        StringBuilder buffer = new StringBuilder();

        // Procedure or function name.
        buffer.append(routineName);
        buffer.append("(");

        // Parameter and return type descriptors.
        if (parmEntries != null) 
        {
            for (SymtabEntry parmEntry : parmEntries)
            {
                buffer.append(emitter.typeDescriptor(parmEntry));
            }
        }
        buffer.append(")");
        buffer.append(emitter.typeDescriptor(routineEntry));

        emitter.emitLine();
        if (routineEntry.getKind() == PROCEDURE) 
        {
            emitter.emitComment("PROCEDURE " + routineName);
        }
        else
        {
            emitter.emitComment("FUNCTION " + routineName);
        }
              
        emitter.emitDirective(METHOD_PRIVATE_STATIC, buffer.toString());
    }

    /**
     * Emit directives for the local variables.
     * @param routineEntry the symbol table entry of the routine's name.
     */
    private void emitRoutineLocals(SymtabEntry routineEntry)
    {
        Symtab symtab = routineEntry.getRoutineSymtab();
        ArrayList<SymtabEntry> entries = symtab.sortedEntries();
        
        int size = 0;
        for (SymtabEntry entry : entries)
        {
            size++;            
            if (entry.getType() == Predefined.realType) size++;
        }
        
        LocalVariables localVariables = new LocalVariables(size);
        emitter.setLocalVariables(localVariables);

        emitter.emitLine();

        // Loop over all the routine's identifiers and
        // emit a .var directive for each variable and formal parameter.
        for (SymtabEntry entry : entries) 
        {
            Kind kind = entry.getKind();

            if ((kind == VARIABLE) || (kind == VALUE_PARAMETER)
                                   || (kind == REFERENCE_PARAMETER)) 
            {
                int slot = entry.getSlotNumber();
                emitter.emitDirective(VAR, slot + " is " + entry.getName(),
                                           emitter.typeDescriptor(entry));
            }
        }
    }

    /**
     * Emit the routine's return code.
     * @param routineEntry the symbol table entry of the routine's name.
     */
    private void emitRoutineReturn(SymtabEntry routineEntry)
    {
        emitter.emitLine();

        // Function: Return the value in the implied function variable.
        if (routineEntry.getKind() == FUNCTION) 
        {
            Typespec type = routineEntry.getType();

            // Get the slot number of the function variable.
            String varName = routineEntry.getName();
            SymtabEntry varEntry = routineEntry.getRoutineSymtab().lookup(varName);
            emitter.emitLoadLocal(type, varEntry.getSlotNumber());
            emitter.emitReturnValue(type);
        }

        // Procedure: Just return.
        else emitter.emit(RETURN);
    }

    /**
     * Emit the routine's epilogue.
     */
    private void emitRoutineEpilogue()
    {
        LocalVariables localVariables = emitter.getLocalVariables();
        LocalStack localStack = emitter.getLocalStack();

        emitter.emitLine();
        emitter.emitDirective(LIMIT_LOCALS, localVariables.count());
        emitter.emitDirective(LIMIT_STACK,  localStack.capacity());
        emitter.emitDirective(END_METHOD);
    }
}
