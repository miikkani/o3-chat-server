package oj3.niemimi;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Writes response body as utf8 encoded bytes.
 * 
 * @param   body    a string representation of response body
 * @param   stream  an OutputStream to write bytes to
 * 
 * @throws IOException  if operation fails
 * 
 */
public final class ResponseWriter {
   public static void writeBody(String body, OutputStream stream) 
    throws IOException {

        try(BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(stream, StandardCharsets.UTF_8)))
        {
            out.write(body);
        }
    }
}
