package oj3.niemimi;


import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import java.net.HttpURLConnection;

public class RegistrationHandler implements HttpHandler {
    private ChatAuthenticator auth;
    private RequestBodyReader reader;
    private String clientBody;
    private String resBody;
    private String[] account;


    RegistrationHandler(ChatAuthenticator auth) {
        this.auth = auth;

    }

    /**
     * Handles client requests to registrate to chatserver. 
     */
    public void handle(HttpExchange ex) {
        int resCode = HttpURLConnection.HTTP_OK;

        try {
            reader = new RequestBodyReader(ex.getRequestBody());


            /* Register user to the server */
            if(ex.getRequestMethod().equalsIgnoreCase("POST")) {
                clientBody = reader.readBody();

                //logging
                System.out.println("User posted: " + clientBody);
                //logging

                account = clientBody.strip().split(":");

                boolean hasWhiteSpaces = !(clientBody.strip().matches("\\S+"));

                //logging
                System.out.println("hasWhiteSpaces: " + hasWhiteSpaces);
                //logging

                //logging
                if(account.length == 2) {
                    // System.out.println("true");
                    System.out.println("Result of split >" + account[0] + "<>" + account[1] );
                }
                //logging


                if(account.length != 2 || hasWhiteSpaces) {
                    resCode = HttpURLConnection.HTTP_BAD_REQUEST;
                } else {
                    
                //logging
                    System.out.println("Trying to register:\n"
                            +  ">" + account[0] + "<" + "\n" 
                            + ">" + account[1] + "<" );
                //logging

                    boolean success = auth.addUser( account[0], account[1]);

                    if(!success) resCode = HttpURLConnection.HTTP_BAD_REQUEST;
                }

            /* Other than POST request */
            } else resCode = HttpURLConnection.HTTP_BAD_REQUEST; 


            /* Send final response */
            ex.sendResponseHeaders(resCode, -1);


        } catch(IOException ioe) {
            ioe.printStackTrace();
            System.out.println("\nI/O Error during registration.");

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("\nError during registration.");
        }
    }
}
