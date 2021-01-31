package oj3.niemimi;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ChatHandler implements HttpHandler {
    private ArrayList<String> messages = new ArrayList<String>();

        //test

    public void handle(HttpExchange ex) {
        if(ex.getRequestMethod().equalsIgnoreCase("GET")) {
            StringBuffer msgBody = new StringBuffer();
            for(String s: messages) {
                msgBody.append(s);
                msgBody.append("\n");
            }
            // set response headers to utf8
             ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");


            try { 
                ex.sendResponseHeaders(200, msgBody.toString().getBytes("UTF-8").length);

                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(ex.getResponseBody(), StandardCharsets.UTF_8));
                w.write(msgBody.toString());
                w.close();

            } catch(IOException e) {
                e.printStackTrace();
                System.out.println("\n\nTULI ERRORI! TERRORI!\n\n");


            }
        } else if(ex.getRequestMethod().equalsIgnoreCase("POST")) {
            try {
                InputStream stream = ex.getRequestBody();
                String text = new BufferedReader(
                                    new InputStreamReader(stream,
                                                            StandardCharsets.UTF_8))
                                                            .lines()
                                                            .collect(Collectors.joining("\n"));
                if(!text.isEmpty()) messages.add(text);
                stream.close();

                ex.sendResponseHeaders(200, -1);

            } catch(Exception e){
                e.printStackTrace();
                System.out.println("\nVIESTIN LUKU FEILI! VOI VOI..\n\n");
            }



        } else {
            try {
                StringBuffer body = new StringBuffer();
                body.append(ex.getRequestMethod() + " is not supported..");
                ex.sendResponseHeaders(400, body.toString().getBytes().length);

                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(ex.getResponseBody()));
                w.write(body.toString());
                w.close();

            } catch(Exception e){
                e.printStackTrace();
                System.out.println("\nTUULETTIMEEN MENI!!\n\n");
            }
        }
    }
    
}
