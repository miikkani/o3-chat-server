package oj3.niemimi;

import java.util.Hashtable;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;



public class ChatAuthenticator extends BasicAuthenticator{
    private Map<String,String> users = null;


    public ChatAuthenticator() {
        super("chat");
        users = new Hashtable<String,String>();
        users.put("dummy","passwd");
    }



    /**
     * Check user auth.
     * @return
     */
    public boolean checkCredentials(String username, String password) {
        boolean ok = false;

        if(users.containsKey(username) && 
            users.get(username).equals(password))
            ok = true;



        return ok;

    }

    
}
