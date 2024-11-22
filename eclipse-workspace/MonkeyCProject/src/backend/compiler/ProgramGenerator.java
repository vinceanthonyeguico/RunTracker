/**
 * <h1>ProgramGenerator</h1>
 *
 * <p>Generate object code for a MonkeyC program and its routines.</p>
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
import static intermediate.symtab.SymtabEntry.Kind.*;
import static intermediate.type.Typespec.Form.*;

import static backend.compiler.Directive.*;
import static backend.compiler.Instruction.*;

public class ProgramGenerator
{
    private Compiler compiler;
    private SymtabEntry programEntry;  // symbol table entry of the program name
    private CodeEmitter emitter;

    /**
     * Constructor.
     * @param the parent generator.
     * @param compiler the compiler to use.
     */
    public ProgramGenerator(Compiler compiler)
    {
        this.compiler = compiler;
        emitter = compiler.getCodeEmitter();
    }
    
    /**
     * Emit code for a program.
     * @param ctx the ProgramContext.
     */
    public void emitProgram(MonkeyCParser.ProgramContext ctx)
    {
        programEntry = ctx.programHeader().programIdentifier().entry;
        Symtab programSymtab = programEntry.getRoutineSymtab();
        
        emitRecords(programSymtab);
        
        emitter.emitDirective(CLASS_PUBLIC, emitter.getProgramName());
        emitter.emitDirective(SUPER, "java/lang/Object");

        emitProgramVariables();
        emitInputScanner();
        emitConstructor(emitter);
        
        MonkeyCParser.RoutinesPartContext routinesCtx = 
                                    ctx.block().declarations().routinesPart();
        if (routinesCtx != null)
        {
            RoutineGenerator routineGenerator = new RoutineGenerator(compiler);
            routineGenerator.emitRoutines(routinesCtx);
        }
        
        emitMainMethod(ctx);
        emitter.closeObjectFile();
    }
    
    /**
     * Create a new compiler instance for a record.
     * @param symtab the record type's symbol table.
     */
    public void emitRecords(Symtab symtab)
    {
        for (SymtabEntry id : symtab.sortedEntries())
        {
            if (   (id.getKind() == TYPE)
                && (id.getType().getForm() == RECORD))
            {
                emitRecord(id);
            }
        }
    }
    
    /**
     * Emit code for a record.
     */
    public void emitRecord(SymtabEntry recordEntry)
    {
        Symtab recordSymtab = recordEntry.getType().getRecordSymtab();
        String namePath = recordEntry.getType().getRecordTypePath();
        CodeEmitter recordEmitter = new CodeEmitter(namePath, "j");
        
        recordEmitter.emitDirective(CLASS_PUBLIC, namePath);
        recordEmitter.emitDirective(SUPER, "java/lang/Object");
        recordEmitter.emitLine();
        
        // Emit code for any nested records.
        emitRecords(recordSymtab);
        
        // Emit record fields.
        for (SymtabEntry id : recordSymtab.sortedEntries())
        {
            if (id.getKind() == RECORD_FIELD)
            {
                recordEmitter.emitDirective(FIELD, id.getName(), 
                                            recordEmitter.typeDescriptor(id));
            }
        }
        
        emitConstructor(recordEmitter);
        recordEmitter.close();  // the object file
    }
    
    /**
     * Emit field directives for the program variables.
     */
    private void emitProgramVariables()
    {
        // Runtime timer and standard in.

        Symtab symtab = programEntry.getRoutineSymtab();
        ArrayList<SymtabEntry> ids = symtab.sortedEntries();

        emitter.emitLine();
        emitter.emitDirective(FIELD_PRIVATE_STATIC, 
                              "_sysin", "Ljava/util/Scanner;");

        // Loop over all the program's identifiers and
        // emit a .field directive for each variable.
        for (SymtabEntry id : ids) 
        {
            if (id.getKind() == VARIABLE) 
            {
                emitter.emitDirective(FIELD_PRIVATE_STATIC, id.getName(),
                                      emitter.typeDescriptor(id));
            }
        }
    }
    
    /**
     * Emit code for the runtime input scanner.
     */
    private void emitInputScanner()
    {
        LocalStack localStack = new LocalStack();
        emitter.setLocalStack(localStack);
        
        emitter.emitLine();
        emitter.emitComment("Runtime input scanner");
        emitter.emitDirective(METHOD_STATIC, "<clinit>()V");
        emitter.emitLine();
        
        emitter.emit(NEW, "java/util/Scanner");
        emitter.emit(DUP);
        emitter.emit(GETSTATIC, "java/lang/System/in Ljava/io/InputStream;");
        emitter.emit(INVOKESPECIAL, 
                     "java/util/Scanner/<init>(Ljava/io/InputStream;)V");
        emitter. emit(PUTSTATIC, 
                      emitter.getProgramName() + "/_sysin Ljava/util/Scanner;");
        emitter.emit(RETURN);
        
        emitter.emitLine();
        emitter.emitDirective(LIMIT_LOCALS, 0);
        emitter.emitDirective(LIMIT_STACK,  3);
        emitter.emitDirective(END_METHOD);
    }

    /**
     * Emit code for the main program constructor.
     * @param emitter the code emitter to use
     */
    private void emitConstructor(CodeEmitter emitter)
    {
        LocalStack localStack = new LocalStack();
        emitter.setLocalStack(localStack);
        
        emitter.emitLine();
        emitter.emitComment("Main class constructor");
        emitter.emitDirective(METHOD_PUBLIC, "<init>()V");        
        emitter.emitDirective(VAR, "0 is this L" 
                                            + emitter.getProgramName() + ";");
        emitter.emitLine();

        emitter.emit(ALOAD_0);
        emitter.emit(INVOKESPECIAL, "java/lang/Object/<init>()V");
        emitter.emit(RETURN);

        emitter.emitLine();
        emitter.emitDirective(LIMIT_LOCALS, 1);
        emitter.emitDirective(LIMIT_STACK,  1);
        emitter.emitDirective(END_METHOD);
    }

    /**
     * Emit code for the program body as the main method.
     * @param ctx the ProgramContext.
     */
    private void emitMainMethod(MonkeyCParser.ProgramContext ctx)
    {
        LocalStack localStack = new LocalStack();
        LocalVariables localVariables = new LocalVariables(5);
        
        emitter.setLocalVariables(localVariables);
        emitter.setLocalStack(localStack);
        
        emitter.emitLine();
        emitter.emitComment("MAIN");
        emitter.emitDirective(METHOD_PUBLIC_STATIC, 
                              "main([Ljava/lang/String;)V");

        emitMainPrologue(programEntry);

        // Emit code to allocate any arrays, records, and strings.
        StructuredDataGenerator structureCode = 
                                    new StructuredDataGenerator(compiler);
        structureCode.emitData(programEntry);

        // Emit code for the compound statement.
        emitter.emitLine();
        compiler.visit(ctx.block().compoundStatement());
        
        emitMainEpilogue();
    }

    /**
     * Emit the main method prologue.
     * @parm programEntry the symbol table entry for the program name.
     */
    private void emitMainPrologue(SymtabEntry programEntry)
    {
        LocalStack localStack = emitter.getLocalStack();

        emitter.emitDirective(VAR, "0 is args [Ljava/lang/String;");
        emitter.emitDirective(VAR, "1 is _start Ljava/time/Instant;");
        emitter.emitDirective(VAR, "2 is _end Ljava/time/Instant;");
        emitter.emitDirective(VAR, "3 is _elapsed J");
        
        // Runtime timer.
        emitter.emitLine();
        emitter.emit(INVOKESTATIC, "java/time/Instant/now()Ljava/time/Instant;");
        localStack.increase(1);
        emitter.emit(ASTORE_1);
    }

    /**
     * Emit the main method epilogue.
     */
    private void emitMainEpilogue()
    {
        LocalVariables localVariables = emitter.getLocalVariables();
        LocalStack localStack = emitter.getLocalStack();

        // Print the execution time.
        emitter.emitLine();
        emitter.emit(INVOKESTATIC, "java/time/Instant/now()Ljava/time/Instant;");
        localStack.increase(1);
        emitter.emit(ASTORE_2);           
        emitter.emit(ALOAD_1);             
        emitter.emit(ALOAD_2);             
        emitter.emit(INVOKESTATIC, 
                    "java/time/Duration/between(Ljava/time/temporal/Temporal;" +
                    "Ljava/time/temporal/Temporal;)Ljava/time/Duration;");
        localStack.decrease(1);
        emitter.emit(INVOKEVIRTUAL, "java/time/Duration/toMillis()J");
        localStack.increase(1);
        emitter. emit(LSTORE_3);              
        emitter.emit(GETSTATIC, "java/lang/System/out Ljava/io/PrintStream;");
        emitter.emit(LDC, "\"\\n[%,d milliseconds execution time.]\\n\"");
        emitter.emit(ICONST_1);             
        emitter.emit(ANEWARRAY, "java/lang/Object");
        emitter.emit(DUP);                 
        emitter.emit(ICONST_0);         
        emitter.emit(LLOAD_3);             
        emitter.emit(INVOKESTATIC, "java/lang/Long/valueOf(J)Ljava/lang/Long;");
        emitter.emit(AASTORE);        
        emitter.emit(INVOKEVIRTUAL, "java/io/PrintStream/printf(Ljava/lang/String;" +
                            "[Ljava/lang/Object;)Ljava/io/PrintStream;");
        localStack.decrease(2);
        emitter.emit(POP);          

        emitter.emitLine();
        emitter.emit(RETURN);
        emitter.emitLine();
        
//        int size = 0;
//        Symtab symtab = programEntry.getRoutineSymtab();
//        ArrayList<SymtabEntry> entries = symtab.sortedEntries();
//
//        for (SymtabEntry entry : entries)
//        {
//            size++;            
//            if (entry.getType() == Predefined.realType) size++;
//        }
//        
//        emitter.emitDirective(LIMIT_LOCALS, size);
        emitter.emitDirective(LIMIT_LOCALS, localVariables.count());
        emitter.emitDirective(LIMIT_STACK,  localStack.capacity());
        emitter.emitDirective(END_METHOD);
        
        emitter.close();  // the object file
    }
}
