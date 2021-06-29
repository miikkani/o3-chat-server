package oj3.niemimi;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.BufferedReader;


/**
 * This class implements a simple reader for http requestbody.
 * 
 */
public final class RequestBodyReader {


    /**
     * Reads all lines from stream as utf8 and returns a string representation
     * of the contents.
     * 
     * @param stream    a InputStream to read from
     * @return          a String containing all lines
     * @throws IOException
     */
    public static String parse(InputStream stream) throws IOException {
        try(BufferedReader in = new BufferedReader(
            new InputStreamReader( stream, StandardCharsets.UTF_8)))
        {
            return in.lines().collect(Collectors.joining("\n"));
        }
    }


    
}
