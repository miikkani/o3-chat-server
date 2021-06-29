package oj3.niemimi;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationHandler implements HttpHandler {
    private ChatAuthenticator auth;

    RegistrationHandler(ChatAuthenticator auth) {
        this.auth = auth;

    }

    /**
     * Handles client requests to registrate to chatserver. 
     */
    public void handle(HttpExchange ex) {
        System.out.println("Start HANDLE..");
        int resCode = HttpURLConnection.HTTP_OK;
        int messageBytes = -1;
        String response = null;
        String requestBody = null;
        Headers requestHeaders = ex.getRequestHeaders();
        Headers responseHeaders = ex.getResponseHeaders();

        try {
            

            /* Register user to the server */
            if(ex.getRequestMethod().equalsIgnoreCase("POST")) {
                String contentType = requestHeaders.getFirst("Content-Type");

                /* Logging */
                System.out.println(contentType);

                /* content-type must match first */
                if(contentType != null
                    && contentType.equalsIgnoreCase("application/json"))
                {


                requestBody = RequestBodyReader.parse(ex.getRequestBody());

                /* Logging */
                System.out.print("in RegistrationHandler, user posted :\n"
                    + requestBody);

                JSONObject clientJson = new JSONObject(requestBody);

                String username = clientJson.getString("username").strip();
                String password = clientJson.getString("password").strip();
                String email = clientJson.getString("email");

                boolean usernameHasWhiteSpaces = false;
                boolean passwordHasWhiteSpaces = false;

                /*
                * Check if strings contains only non-whitespace
                * characters using regex.
                */
                if(!(username.matches("\\S+"))) {
                    usernameHasWhiteSpaces = true;
                }

                if(!(password.matches("\\S+"))) {
                    passwordHasWhiteSpaces = true;
                }

                /* Logging */
                System.out.print(" hasWhiteSpaces(un,pw): "
                    + usernameHasWhiteSpaces
                    + " " + passwordHasWhiteSpaces);

                if(usernameHasWhiteSpaces || passwordHasWhiteSpaces) {
                    resCode = HttpURLConnection.HTTP_BAD_REQUEST;
                } else {
                    
                    System.out.println(" -> registering "
                            + "username: " + username 
                            + "password: " + password 
                            + "email: " + email); 

                            /* Finally try to add user credentials */
                    boolean success = auth.addUser( username, password, email);

                    if(!success) resCode = HttpURLConnection.HTTP_BAD_REQUEST;
                }

                } else {
                    System.out.println("Wrong content-type or missing");
                    resCode = HttpURLConnection.HTTP_BAD_REQUEST;
                }
            /* Other than POST request */
            } else resCode = HttpURLConnection.HTTP_BAD_REQUEST; 

        } catch(JSONException jse) {
            jse.printStackTrace();
            System.out.println("\nJSON is not valid.");
            response = "JSON is not valid.";
            messageBytes = response.getBytes(StandardCharsets.UTF_8).length;
            resCode = HttpURLConnection.HTTP_BAD_REQUEST;

        } catch(IOException ioe) {
            ioe.printStackTrace();
            System.out.println("\nI/O Error during registration.");

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("\nError during registration.");
        } finally {
            System.out.println("@RegistrationHandler, finally-block");
            try {
                if(response != null) {
                    responseHeaders.set(
                        "Content-Type",
                        "text/plain; charset=utf-8");
                } 
                ex.sendResponseHeaders(resCode, messageBytes);
                System.out.println("response sent...");
                
                if(response != null) {
                    ResponseWriter.writeBody(response, ex.getResponseBody());
                    System.out.println("body payload written..");
                }
            } catch(IOException ioe){
                ioe.printStackTrace();
                System.out.println("Error sending response");
            }

        }
        System.out.println("Handle..DONE");
    }
}
