/**
 * <h1>MonkeyC</h1>
 *
 * <p>The main class.</p>
 *
 * <p>Copyright (c) 2024 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
import static backend.BackendMode.*;

import java.io.FileInputStream;
import java.io.InputStream;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import frontend.*;

import intermediate.antlr4.*;
import intermediate.symtab.*;

import backend.compiler.Compiler;

public class MonkeyC 
{
    public static void main(String[] args) throws Exception 
    {
        if (args.length != 1)
        {
            System.out.println("USAGE: sourceFileName");
            return;
        }
        
        String sourceFileName = args[0];
        
        // Generate a source file listing.
        new Listing(sourceFileName);
        
        // Create the input stream.
        InputStream source = new FileInputStream(sourceFileName);
        
        // Create the character stream from the input stream.
        CharStream cs = CharStreams.fromStream(source);
        
        // Custom syntax error handler.
        SyntaxErrorHandler syntaxErrorHandler = new SyntaxErrorHandler();
        
        // Create a lexer which scans the character stream
        // to create a token stream.
        MonkeyCLexer lexer = new MonkeyCLexer(cs);
        lexer.removeErrorListeners();
        lexer.addErrorListener(syntaxErrorHandler);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        // Create a parser which parses the token stream.
        MonkeyCParser parser = new MonkeyCParser(tokens);
        
        // Pass 1: Check syntax and create the parse tree.
        System.out.printf("\nPASS 1 Syntax: ");
        parser.removeErrorListeners();
        parser.addErrorListener(syntaxErrorHandler);
        ParseTree tree = parser.program();
        
        int errorCount = syntaxErrorHandler.getCount();
        if (errorCount > 0) 
        {
            System.out.printf("\nThere were %d syntax errors.\n", errorCount);
            System.out.println("Object file not created or modified.");
            return;
        }
        else
        {
            System.out.println("There were no syntax errors.");
        }
        
        // Pass 2: Semantic operations.
        System.out.printf("\nPASS 2 Semantics:\n");
        Semantics pass2 = new Semantics(COMPILER);
        pass2.visit(tree);
        
        errorCount = pass2.getErrorCount();
        if (errorCount > 0)
        {
            System.out.printf("\nThere were %d semantic errors.\n", errorCount);
            System.out.println("Object file not created or modified.");
            return;
        }

        
        // Compile the MonkeyC program.
        System.out.printf("\nPASS 3 Compilation: ");
        SymtabEntry programEntry = pass2.getProgramEntry();
        Compiler pass3 = new Compiler(programEntry);
        pass3.visit(tree);
        
        System.out.printf("Object file \"%s\" created.\n", pass3.getObjectFileName());
    }
}
