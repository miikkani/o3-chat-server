package oj3.niemimi;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
//import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.OutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatHandler implements HttpHandler {
    private ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();


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
             * 
             */
            if(ex.getRequestMethod().equalsIgnoreCase("GET")) {
                if(!messages.isEmpty()) {
                    JSONArray response = new JSONArray();
                    for(ChatMessage m: messages) {
                        response.put(
                            new JSONObject()
                                .put("user", m.getUsername())
                                .put("message", m.getMessage())
                                .put("sent", m.getUTC())
                        );
                    }
                    responseBody = response.toString();
                    bytes = responseBody
                            .getBytes(StandardCharsets.UTF_8).length;

                    responseHeaders
                        .set("Content-Type", "application/json; charset=utf-8");




                } else {
                    responseCode = HttpURLConnection.HTTP_NO_CONTENT;
                }
            

            /**
             * Handle POST 
             */
            } else if(ex.getRequestMethod().equalsIgnoreCase("POST")) {

                String contentType = requestHeaders.getFirst("Content-Type");

                /* Logging */
                System.out.println(contentType);

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

                    ChatMessage m = new ChatMessage(username, message, millis);
                    messages.add(m);

                    /** 
                     * Sort all messages to chronological order based on
                     * timestamp. Otherwise network delays could affect
                     * message order.
                     */
                    messages.sort(Comparator.comparingLong(ChatMessage::getMillis));

                } else {
                    responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
                }
            

            /**
             * Handle everything else 
             */
            } else {
                responseBody = "Sorry, only tea here..";
                bytes = responseBody.getBytes(StandardCharsets.UTF_8).length;
                responseCode = 418;
            }

        } catch(JSONException jse ) {
            responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
            System.out.println("Invalid JSON.");
        } catch(DateTimeParseException dpe ) {
            responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
            System.out.println("Invalid TimeFormat in json.");
        } catch (IOException e){
            e.printStackTrace();
            System.out.println("I/O error.");
        } finally {
            System.out.println("@ChatHandler, finally-block");
            try {
                ex.sendResponseHeaders(responseCode, bytes);
                System.out.println("response sent...");
                if(bytes > 0) {
                    ResponseWriter.writeBody(responseBody, ex.getResponseBody());
                    System.out.println("body payload written..");
                }
            } catch(IOException ioe){
                ioe.printStackTrace();
                System.out.println("Error sending response");
            }


        }
    }
}
