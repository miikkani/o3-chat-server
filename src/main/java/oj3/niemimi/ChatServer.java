package oj3.niemimi;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
/**
 * Chatserver 
 *
 */
public class ChatServer {
    public static void main( String[] args ) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
            server.createContext("/chat", new ChatHandler());

            server.setExecutor(null);
            server.start();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to create server.");
        }

    }
}
