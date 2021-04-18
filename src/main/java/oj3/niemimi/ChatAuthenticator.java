package oj3.niemimi;

import java.util.Hashtable;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;



public class ChatAuthenticator extends BasicAuthenticator{
    private Map<String,String> users; 


    public ChatAuthenticator() {
        super("chat");
        users = new Hashtable<String,String>();
        users.put("dummy","passwd");
    }



    /**
     * Checks user authentication.
     * @return  a boolean answer whether authentication was successful.
     */
    public boolean checkCredentials(String username, String password) {


        System.out.println("checking user: >" + username +"<");
        System.out.println("exists: >" + users.containsKey(username) +"<");
        System.out.println("password: >" + users.get(username) + "<");


        return users.containsKey(username) 
                && users.get(username).equals(password);

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
            System.out.println("user registered>" + username + "<>" + password + "<");
            System.out.println("check " + username + ">" + users.containsKey(username));
        } else added = false; 

        return added;
    }

    
}
