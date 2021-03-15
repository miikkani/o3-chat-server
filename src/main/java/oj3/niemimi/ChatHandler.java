package oj3.niemimi;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ChatHandler implements HttpHandler {
    private ArrayList<String> messages = new ArrayList<String>();


    public void handle(HttpExchange ex) {
        try {
            StringBuffer msgBody = new StringBuffer();

            int res_code = 200;


            /**
             * Handle GET 
             */
            if(ex.getRequestMethod().equalsIgnoreCase("GET")) {
                for(String s: messages) {
                    msgBody.append(s);
                    msgBody.append("\n");
                }

                // set response headers to utf8
                ex.getResponseHeaders().set("Content-Type", 
                                            "text/plain; charset=utf-8");


                ex.sendResponseHeaders(200, msgBody
                                            .toString()
                                            .getBytes("UTF-8").length);

                writeResponse(msgBody.toString(), ex.getResponseBody());





            /**
             * Handle POST 
             */
            } else if(ex.getRequestMethod().equalsIgnoreCase("POST")) {
                String text = readBody(ex.getRequestBody());


                if(!text.isEmpty()) {
                    messages.add(text);
                    ex.sendResponseHeaders(res_code, -1);
                } else {
                    res_code = 400;
                    msgBody.append("Empty message.");
                    ex.sendResponseHeaders(res_code,
                                           msgBody.toString()
                                           .getBytes("UTF-8").length);

                    writeResponse(msgBody.toString(), ex.getResponseBody());

                }





            /**
             * Handle everything else 
             */
            } else {
                msgBody.append("Sorry, only tea here..");
                ex.sendResponseHeaders(418, msgBody.toString().getBytes().length);
                writeResponse(msgBody.toString(), ex.getResponseBody());
            }

        } catch (IOException e){
            e.printStackTrace();

            System.out.println("I/O virhe.");
        }
    }
    
    /**
     * Return request body as a String
     * @return body
     */
    public String readBody(InputStream stream) throws IOException {
        String body = new BufferedReader(
                        new InputStreamReader(
                            stream, 
                            StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));

        return body;
    }

    /**
     * Write response body.
     */
    public void writeResponse(String body, OutputStream stream) throws IOException {

        BufferedWriter w =  new BufferedWriter(
                            new OutputStreamWriter(
                                stream, StandardCharsets.UTF_8));

        w.write(body);
        w.close();

    }



}
