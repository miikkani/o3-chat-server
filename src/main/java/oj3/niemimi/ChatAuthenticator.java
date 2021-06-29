package oj3.niemimi;

import java.util.Hashtable;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;



public class ChatAuthenticator extends BasicAuthenticator{
    private Map<String,User> users; 


    public ChatAuthenticator() {
        super("chat");
        users = new Hashtable<String,User>();

        /* Test account */
        users.put("dummy",
            new User("dummy", "passwd" , "dummy@test.jp"));
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
        ", user object: |" + users.get(username));

        User user = users.get(username);

        if(user != null){
            ok = user.getPassword().equals(password);
        }

        System.out.println("\tuser authenticated: " + ok);

        return ok;
    }

    /**
     * Registers user to server.
     * @param username      a string representing username 
     * @param password      a string representing password for the account
     * @param email         email address for the user
     * @return              true if successful
     * 
     */
    public boolean addUser(String username, String password, String email) {
        User user = new User(username, password, email);
        boolean added = true;
        if(!users.containsKey(username)) {
            users.put(username, user);

            System.out.println("addUser() >> |"
                + username + "|"
                + password + "|"
                + email + "|"
                + " registered.");
        } else added = false; 

        return added;
    }

    
}
