package oj3.niemimi;

import java.util.Hashtable;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;



public class ChatAuthenticator extends BasicAuthenticator{
    private Map<String,String> users; 


    public ChatAuthenticator() {
        super("chat");
        users = new Hashtable<String,String>();

        /* Test account */
        users.put("dummy","passwd");
    }



    /**
     * Checks user authentication.
     * @return  true if authentication was successful
     */
    public boolean checkCredentials(String username, String password) {
        boolean ok = false;

        System.out.print("checkCredentials() >>" + 
        " param: username |" + username +"|, " +
        " param: password |" + password +"|, " +
        " user exists: " + users.containsKey(username) +
        ", password: |" + users.get(username) + "|, "
        );

        ok = users.containsKey(username) 
                && users.get(username).equals(password);
        
        System.out.println("\tuser authenticated: " + ok);

        return ok;
    }

    /**
     * Registers user to server.
     * @param username      a string representing username 
     * @param password      a string representing password for the account
     * @return              a boolean value whether registration 
     *                      was successful
     */
    public boolean addUser(String username, String password) {
        boolean added = true;
        if(!users.containsKey(username)) {
            users.put(username, password);

            System.out.println("addUser() >> |"
                + username + "|"
                + password + "|"
                + " registered.");
        } else added = false; 

        return added;
    }

    
}
