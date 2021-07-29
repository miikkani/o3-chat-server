package oj3.niemimi;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.logging.Logger; 

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationHandler implements HttpHandler {
    private ChatAuthenticator auth;
    private Logger log;

    RegistrationHandler(ChatAuthenticator auth) {
        this.auth = auth;
        log = Logger.getLogger("chatserver");

    }

    /**
     * Handles client requests to registrate to chatserver. 
     */
    public void handle(HttpExchange ex) {
        log.info("start HANDLE..");
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

                log.info("POST Content-Type: " + contentType);

                /* content-type must match first */
                if(contentType != null
                    && contentType.equalsIgnoreCase("application/json"))
                {


                requestBody = RequestBodyReader.parse(ex.getRequestBody());

                /* Logging */
                log.info("\nuser posted :\n" + requestBody + "\n");

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
                log.info(" hasWhiteSpaces(un,pw): "
                    + usernameHasWhiteSpaces
                    + " " + passwordHasWhiteSpaces);

                if(usernameHasWhiteSpaces || passwordHasWhiteSpaces) {
                    resCode = HttpURLConnection.HTTP_BAD_REQUEST;
                } else {
                    
                    log.info("""
                    
                                trying to register...
                                username: %s
                                password: %s
                                email: %s

                            """.formatted(username,password,email)); 

                            /* Finally try to add user credentials */
                    boolean success = auth.addUser( username, password, email);

                    if(!success){
                     resCode = HttpURLConnection.HTTP_BAD_REQUEST;
                     log.warning("user already exists.");
                    }
                }

                } else {
                    log.warning("Wrong content-type or missing");
                    resCode = HttpURLConnection.HTTP_BAD_REQUEST;
                }
            /* Other than POST request */
            } else resCode = HttpURLConnection.HTTP_BAD_REQUEST; 

        } catch(JSONException jse) {
            // jse.printStackTrace();
            log.warning("JSON is not valid.");
            response = "JSON is not valid.";
            messageBytes = response.getBytes(StandardCharsets.UTF_8).length;
            resCode = HttpURLConnection.HTTP_BAD_REQUEST;

        } catch(IOException ioe) {
            ioe.printStackTrace();
            log.severe(ioe.getLocalizedMessage());
            log.severe("I/O Error during registration.");

        } catch(Exception e) {
            e.printStackTrace();
            log.severe(e.getLocalizedMessage());
            log.severe("Error during registration.");
        } finally {
            log.info("@RegistrationHandler, finally-block");
            try {
                if(response != null) {
                    responseHeaders.set(
                        "Content-Type",
                        "text/plain; charset=utf-8");
                } 
                ex.sendResponseHeaders(resCode, messageBytes);
                log.info("response sent...");
                
                if(response != null) {
                    ResponseWriter.writeBody(response, ex.getResponseBody());
                    log.info("body payload written..");
                }
            } catch(IOException ioe){
                ioe.printStackTrace();
                log.severe(ioe.getLocalizedMessage());
                log.severe("Error sending response");
            }

        }
        log.info("Handle..DONE");
    }
}
