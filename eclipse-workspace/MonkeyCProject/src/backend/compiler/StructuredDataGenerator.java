/**
 * <h1>StructuredDataGenerator</h1>
 *
 * <p>Generate code to allocate arrays, records, and strings.</p>
 *
 * <p>Copyright (c) 2024 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
package backend.compiler;

import java.util.ArrayList;

import intermediate.symtab.*;
import intermediate.type.*;
import intermediate.type.Typespec.Form;

import static intermediate.symtab.SymtabEntry.Kind.*;
import static intermediate.type.Typespec.Form.*;
import static backend.compiler.Instruction.*;

public class StructuredDataGenerator
{
    private CodeEmitter emitter;
    private LocalVariables localVariables;
    private LocalStack localStack;

    /**
     * Constructor.
     * @param the parent generator.
     */
    public StructuredDataGenerator(Compiler compiler)
    {
        emitter = compiler.getCodeEmitter();
        localVariables = emitter.getLocalVariables();
        localStack = emitter.getLocalStack();
    }

    /**
     * Emit code to allocate the string, array, and record variables
     * of a program, procedure, or function.
     * @param routineEntry the routine's symbol table entry.
     */
    public void emitData(SymtabEntry routineEntry)
    {
        Symtab symtab = routineEntry.getRoutineSymtab();
        ArrayList<SymtabEntry> ids = symtab.sortedEntries();

        // Loop over all the symbol table's identifiers to emit
        // data allocation code for array and record variables.
        for (SymtabEntry id : ids) 
        {
            if (id.getKind() == VARIABLE) 
            {
                Typespec type = id.getType();
                Form form = type.getForm();
                    
                if      (form == ARRAY)  emitAllocateArray(id, type);
                else if (form == RECORD) emitAllocateRecord(id, type, DUP);
            }
        }
    }

    /**
     * Emit code to allocate an array for a target variable or field.
     * @param targetEntry the target variable's or field's symbol table entry.
     * @param arrayType the array type.
     */
    private void emitAllocateArray(SymtabEntry targetEntry, Typespec arrayType)
    {
        Typespec elmtType = arrayType;
        int dimensionCount = 0;

        // Count the dimensions and emit a load constant of each element count.
        emitter.emitLine();
        do 
        {
            int elmtCount = elmtType.getArrayElementCount();
            ++dimensionCount;
            emitter.emitLoadConstant(elmtCount);
            elmtType = elmtType.getArrayElementType();
        } while (elmtType.getForm() == ARRAY);

        // The array element type.
        elmtType = elmtType.baseType();
        Form elmtForm = elmtType.getForm();
        String typeName =
              elmtType == Predefined.integerType ? "int"
            : elmtType == Predefined.realType    ? "double"
            : elmtType == Predefined.booleanType ? "boolean"
            : elmtType == Predefined.charType    ? "char"
            : elmtType == Predefined.stringType  ? "java/lang/String"
            : elmtForm == ENUMERATION            ? "int"
            : elmtForm == RECORD                 ? elmtType.getIdentifier()
                                                           .getName()
            :                                      null;

        // One-dimensional array.
        if (dimensionCount == 1) 
        {
            if (elmtType.getForm() == RECORD)
            {
                emitter.emit(ANEWARRAY, elmtType.getRecordTypePath());
                emitter.emit(DUP);
            }
            else if (elmtType == Predefined.stringType) 
            {
                emitter.emit(ANEWARRAY, typeName);
            }
            else
            {
                emitter.emit(NEWARRAY, typeName);
            }
        }

        // Multidimensional array.
        else 
        {
            emitter.emit(MULTIANEWARRAY, 
                         emitter.typeDescriptor(targetEntry.getType()),
                         Integer.toString(dimensionCount));
            localStack.decrease(dimensionCount - 1);

            if (elmtType.getForm() == RECORD) emitter.emit(DUP);
        }

        // Store the allocation into the array variable.
        emitter.emitStoreValue(targetEntry, targetEntry.getType());

        // Allocate data for record elements.
        if (elmtType.getForm() == RECORD)
        {
            emitAllocateArrayElements(targetEntry, targetEntry.getType(), 
                                      1, dimensionCount);
            emitter.emit(POP);
        }
    }

    /**
     * Emit code that loops over the array dimensions to allocate
     * data for each element.
     * @param targetEntry the symbol table entry of the target variable.
     * @param elmtType the data type of the array element.
     * @param dimensionIndex the first dimension is 1, second is 2, etc.
     * @param dimensionCount the count of dimensions.
     */
    private void emitAllocateArrayElements(
                                    SymtabEntry targetEntry, Typespec elmtType, 
                                    int dimensionIndex, int dimensionCount)
    {
        int count = elmtType.getArrayElementCount();
        int tempIndex = localVariables.reserve();  // temporary loop variable
        Label loopStartLabel = new Label();
        Label loopExitLabel  = new Label();

        // Initialize temporary variable to 0.
        emitter.emitLoadConstant(0);
        emitter.emitStoreLocal(Predefined.integerType, tempIndex);

        // Top of the loop: 
        // Compare the temporary variable to the element count.
        emitter.emitLabel(loopStartLabel);
        emitter.emitLoadLocal(Predefined.integerType, tempIndex);
        emitter.emitLoadConstant(count);
        emitter.emit(IF_ICMPGE, loopExitLabel);
        emitter.emitLine();
        emitter.emit(DUP);
        
        Form form = elmtType.getArrayElementType().getForm();
        
        // Allocate data for the next array dimension.
        if (form == ARRAY)
        {
            // Subscript
            emitter.emitLoadLocal(Predefined.integerType, tempIndex);
            
            emitter.emit(AALOAD);
            emitAllocateArrayElements(targetEntry, elmtType.getArrayElementType(), 
                                      dimensionIndex + 1, dimensionCount);
        }
        
        // Allocate data for a record element.
        else if (form == RECORD) 
        {
            // Subscript
            emitter.emitLoadLocal(Predefined.integerType, tempIndex);
            
            emitAllocateRecord(null, elmtType.getArrayElementType(), DUP_X2);
        }

        // Bottom of the loop: 
        // If it's not the last dimension, pop off the copy of the record
        // address used by that dimension.
        if (dimensionIndex != dimensionCount) emitter.emit(POP);
        
        // Increment the temporary variable and branch back to the top 
        // of the loop.
        emitter.emit(IINC, tempIndex, 1);
        emitter.emit(GOTO, loopStartLabel);
        emitter.emitLabel(loopExitLabel);

        localVariables.release(tempIndex);
    }

    /**
     * Emit code to allocate a record variable as a class.
     * @param variableEntry the symbol table entry of the variable.
     * @param recordType the record data type.
     */
    private void emitAllocateRecord(
                SymtabEntry variableEntry, Typespec recordType, Instruction dup)
    {
        // Allocate and store into the record variable.
        emitter.emit(NEW, recordType.getRecordTypePath());
        emitter.emit(DUP);
        emitter.emit(INVOKESPECIAL, 
                     recordType.getRecordTypePath() + "/<init>()V");
        localStack.decrease(1);
        
        boolean hasStructuredField = false;
        for (SymtabEntry fieldEntry : 
                                recordType.getRecordSymtab().sortedEntries())
        {
            if (fieldEntry.getKind() == RECORD_FIELD)
            {
                if (fieldEntry.getType().isStructured())
                {
                    hasStructuredField = true;
                    break;
                }
            }
        }
        
        // Duplicate the record address to use to initialize structured fields:
        //   DUP    to later store this record into an unstructured variable
        //   DUP_X1 to later store this record into a record field
        //   DUP_X2 to later store this record into an array element
        if (hasStructuredField)
        {
            emitter.emit(dup);
            // Stack: @record @record ...
        }

        // Store newly allocated record.
        if (variableEntry != null)
        {
            emitter.emitStoreValue(variableEntry, variableEntry.getType());
        }
        else
        {
            emitter.emitStoreValue(null, null);
        }
        // Stack: @record ...
        
        // Allocate data for any structured fields.
        if (hasStructuredField)
        {
            for (SymtabEntry fieldEntry : 
                                recordType.getRecordSymtab().sortedEntries())
            {
                if (fieldEntry.getKind() == RECORD_FIELD)
                {
                    Typespec fieldType = fieldEntry.getType();
                    if (fieldType.getForm() == ARRAY)
                    {
                        // Duplicate record address to store array field.
                        emitter.emit(DUP);  
                        
                        emitAllocateArray(fieldEntry, fieldType);
                        // Stack: @record
                    }
                    else if (fieldType.getForm() == RECORD)
                    {
                        // Duplicate record address to store record field.
                        emitter.emit(DUP);  
                        
                        emitAllocateRecord(fieldEntry, fieldType, DUP_X1);
                        // Stack: @record
                    }
                }
            }
            
            // Pop off the remaining duplicated record address
            // from either DUP or DUP_X1 or DUP_X2
            emitter.emit(POP);
        }
    }
}
