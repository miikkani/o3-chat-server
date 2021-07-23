package oj3.niemimi;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatHandler implements HttpHandler {
    private ArrayList<ChatMessage> messages;
    private Logger log;
    private ChatDatabase db;

    public ChatHandler() {
        log = Logger.getLogger("chatserver");
        db = ChatDatabase.getInstance();
    }


    public void handle(HttpExchange ex) {
            String requestBody = null;
            String responseBody = null;
            int responseCode = HttpURLConnection.HTTP_OK;
            int bytes = -1;
            Headers requestHeaders = ex.getRequestHeaders();
            Headers responseHeaders = ex.getResponseHeaders();

        try {
            /**
             * Handle GET 
             */
            if(ex.getRequestMethod().equalsIgnoreCase("GET")) {
                long defaultTime = Instant.now()
                    .minus(Duration.ofHours(24L))
                    .toEpochMilli();

                messages = db.getMessages(defaultTime);
                if(!messages.isEmpty()) {
                    JSONArray response = new JSONArray();
                    for(ChatMessage m: messages) {
                        response.put(
                            new JSONObject()
                                .put("user", m.getUsername())
                                .put("message", m.getText())
                                .put("sent", m.getUTC())
                        );
                    }
                    responseBody = response.toString();
                    bytes = responseBody
                            .getBytes(StandardCharsets.UTF_8).length;

                    responseHeaders.set("Content-Type", "application/json");
                } else {
                    responseCode = HttpURLConnection.HTTP_NO_CONTENT;
                }

            /**
             * Handle POST 
             */
            } else if(ex.getRequestMethod().equalsIgnoreCase("POST")) {

                String contentType = requestHeaders.getFirst("Content-Type");

                /* Logging */
                log.info("req_contentType: " + contentType);

                /* content-type must match  */
                if(contentType != null
                    && contentType.equalsIgnoreCase("application/json"))
                {
                    requestBody = RequestBodyReader.parse(ex.getRequestBody());
                    JSONObject clientJson = new JSONObject(requestBody);

                    String username = clientJson.getString("user");
                    String message = clientJson.getString("message");
                    String sent = clientJson.getString("sent");

                    
                    DateTimeFormatter formatter = DateTimeFormatter
                        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

                    long millis = OffsetDateTime
                        .parse(sent, formatter)
                        .toInstant()
                        .toEpochMilli();

                    db.addMessage(millis, username, message);

                } else {
                    responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
                }
            

            /**
             * Handle everything else 
             */
            } else {
                responseBody = "I'm a teapot.";
                bytes = responseBody.getBytes(StandardCharsets.UTF_8).length;
                responseCode = 418;
            }

        } catch(JSONException jse ) {
            responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
            log.warning("invalid JSON from client.");
        } catch(DateTimeParseException dpe ) {
            responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
            log.warning("invalid timeformat in client JSON.");
        } catch (SQLException e) {
            responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
            log.log(Level.WARNING, e.getMessage(), e);

        } catch (IOException e){
            e.printStackTrace();
            log.severe("I/O error.");
        } finally {
            log.info("sending response...");
            try {
                ex.sendResponseHeaders(responseCode, bytes);
                log.info("...responseheaders sent");
                if(bytes > 0) {
                    ResponseWriter.writeBody(responseBody, ex.getResponseBody());
                    log.info("...messagebody written");
                }
                log.info("...done");
            } catch(IOException ioe){
                ioe.printStackTrace();
                log.severe("ERROR SENDING RESPONSE");
            }
        }
    }
}
