package oj3.niemimi;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import java.sql.SQLException;

import java.util.logging.Logger;
// import java.util.logging.SimpleFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
/**
 * Chatserver 
 *
 * todo
 * - readerclass to static or factory
 * - proper logging (java built-in?)
 * - error checking > userRegistration
 * 
 * 
 * 
 */
public class ChatServer {
    final static int PORT = 8001;
    public static void main( String[] args ) {
            /**
             * TODO:
             *  - read command line arguments for desired loggin level
             */



            Logger log = Logger.getLogger("chatserver");
        try {
            /* setup logging */
            FileHandler filehandler = new FileHandler("server.log");
            // filehandler.setFormatter(new SimpleFormatter());
            filehandler.setFormatter(new MyFormatter());
            ConsoleHandler consolehandler = new ConsoleHandler();
            consolehandler.setFormatter(new MyFormatter());
            log.setLevel(Level.ALL);
            log.addHandler(filehandler);
            log.addHandler(consolehandler);
            log.setUseParentHandlers(false);
            System.out.println("Using logging level: " + log.getLevel() + "\n");
            log.info("log file: 'server.log'");

            /* start initializing server */
            HttpsServer server = HttpsServer.create(
                                new InetSocketAddress(PORT), 0);
            log.info("using port: " + PORT);
            SSLContext sslContext = chatServerSSLContext();

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    InetSocketAddress remote = params.getClientAddress();
                    log.info(remote + " connected...");
                    SSLContext c = getSSLContext();
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });

            /* initialize authenicator for server */
            ChatAuthenticator cauth = new ChatAuthenticator();

             /* create path: /chat    */
            HttpContext chatContext = server.createContext(
                                    "/chat", 
                                    new ChatHandler());

            chatContext.setAuthenticator(cauth);
            log.info("/chat context created...");

            /* create path: /registration    */
            server.createContext(
                "/registration",
                new RegistrationHandler(cauth));
            log.info("/registration context created...");

            /* connect to database */
            ChatDatabase.getInstance().open("chat.db");

            /* start ChatServer in thread */
            server.setExecutor(null);
            server.start();
            log.info("server started...");


        } catch (IOException e) {
            e.printStackTrace();
            log.severe("IO error. Unable to create server.");
        } catch (SQLException sqe) {
            sqe.printStackTrace();
            log.severe("Database error.");
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("Error creating server.");
        }

    }

    /**
     * Create SSLContext.
     * 
     * @return ssl - SSLContext
     */
    private static SSLContext chatServerSSLContext() throws Exception {
        char[] passphrase = "password".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore.jks"), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;




    } 
}
