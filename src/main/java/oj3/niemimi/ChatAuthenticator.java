package oj3.niemimi;

import java.util.logging.Logger;

import com.sun.net.httpserver.BasicAuthenticator;

public class ChatAuthenticator extends BasicAuthenticator{
    private ChatDatabase db;
    Logger log;

    public ChatAuthenticator() {
        super("chat");
        log = Logger.getLogger("chatserver");
        db = ChatDatabase.getInstance();
    }

    /**
     * Checks user authentication.
     * @return  true if authentication was successful
     */
    public boolean checkCredentials(String username, String password) {
        boolean ok = false;
        ok = db.checkCredentials(username, password);
        log.finest("user authenticated: " + ok);
        return ok;
    }

    /**
     * Registers user to server.
     * @param username      the username from the request
     * @param password      the password from the request
     * @param email         the email address from the request
     * @return              true if successful
     * 
     */
    public boolean addUser(String username, String password, String email) {
        boolean added = false;
        added = db.addUser(username, password, email);
        if(added) {
            log.finest("""
            
                    %s :: %s
                    email: %s
                    registered.

                """.formatted(username, "<hidden>", email));
        }
        return added;
    }
}
