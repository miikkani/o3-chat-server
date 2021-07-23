package oj3.niemimi;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private String username;
    private String text;
    private long sent;


    ChatMessage(String username, String message, long sent) {
        this.username = username;
        this.text = message;
        this.sent = sent;
    }




    public String getUTC() {
        DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        OffsetDateTime time = OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(sent), ZoneId.of("UTC"));

        return time.format(formatter);
    }


    public long getMillis(){return this.sent;} 
    public String getUsername(){return this.username;}
    public String getText(){return this.text;}

    
}
