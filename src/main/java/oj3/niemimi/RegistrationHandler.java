package oj3.niemimi;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.net.HttpURLConnection;
import java.io.IOException;

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

                /* Logging */
                System.out.print("Reg_handler >> user posted |"
                    + clientBody + "|");

                account = clientBody.strip().split(":");

                boolean hasInnerWhiteSpaces = !(clientBody.strip().matches("\\S+"));

                /* Logging */
                System.out.print(" hasInnerWhiteSpaces: " + hasInnerWhiteSpaces);

                /* Logging */
                if(account.length == 2) {
                    System.out.println(" split: |"
                        + account[0] + "|"
                        + account[1] + "|");
                } else {
                    System.out.println(". tokens: " + account.length);
                }


                if(account.length != 2 || hasInnerWhiteSpaces) {
                    resCode = HttpURLConnection.HTTP_BAD_REQUEST;
                } else {
                    
                /* Logging */
                    System.out.println("registering "
                            + account[0] + ":" 
                            + account[1]);

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
