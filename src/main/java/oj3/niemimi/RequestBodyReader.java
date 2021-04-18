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
public class RequestBodyReader {
    private BufferedReader in = null;

    /**
     * Class constructor.
     * @param in    Stream to read from
     */
    RequestBodyReader(InputStream in) {
        this.in = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    /**
     * Reads request body and returns it as a String.
     * 
     * @return  String representation of the requestbody
     * @throws IOException
     */
    String readBody() throws IOException {
        return in.lines().collect(Collectors.joining("\n"));
    }
    
}
