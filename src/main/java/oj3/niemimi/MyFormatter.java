package oj3.niemimi;

import java.util.logging.Formatter;
import java.util.logging.LogRecord; 

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
/**
 * Print a straightforward one line summary of the LogRecord.
 */
public class MyFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        StringBuffer message = new StringBuffer();
        String currentTime;
        String pattern = "'['HH:mm:ss.SSS']'";

        String sourceclass = record.getSourceClassName();
        String[] tokens = sourceclass.split("oj3.niemimi.");
        sourceclass = tokens[1];


        String location = sourceclass + "."
            + record.getSourceMethodName() + "()";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        OffsetDateTime time = OffsetDateTime
            .ofInstant(Instant.now(), ZoneId.systemDefault());
        currentTime = time.format(formatter);

        message.append(currentTime + " ");
        message.append(record.getLevel().getName() + ": ");
        message.append(record.getMessage());
        message.append(" @" + location);
        message.append("\n");

        return message.toString();
    }
    
}