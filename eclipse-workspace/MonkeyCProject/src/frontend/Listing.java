/**
 * <h1>Listing</h1>
 *
 * <p>Generate a source listing.</p>
 *
 * <p>Copyright (c) 2024 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
package frontend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Listing
{
    public Listing(String sourceFileName) throws IOException
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(sourceFileName));
            
            int lineNumber = 0;
            String line = br.readLine();
            
            while (line != null)
            {
                System.out.printf("%03d %s\n", ++lineNumber, line);
                line = br.readLine();
            }
            
            br.close();
        }
        catch(IOException ex)
        {
            System.out.printf("ERROR: Failed to open source file \"%s\".\n",
                              sourceFileName);
            System.out.printf("       %s\n", ex.getMessage());
            System.exit(-1);
        }
    }
}
