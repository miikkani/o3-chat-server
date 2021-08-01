package oj3.niemimi;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpContext;
import java.net.InetSocketAddress;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;

import java.sql.SQLException;

/**
 * Chatserver 
 *
 * todo
 * - readerclass to static or factory
 * - error checking > userRegistration
 * 
 * 
 * 
 */
public class ChatServer {
    final static int PORT = 8001;
    final static String DATABASE_FILE = "chat.db";
    // final static String DATABASE_FILE = ":memory:";
    public static void main( String[] args ) {
            // System.out.println(
            //     "#### THREAD: " + Thread.currentThread().getId());

            String logLevel = "SEVERE";
            try{
                if(args.length == 1) {
                    logLevel = switch(args[0]) {
                        case "-v" -> "WARNING";
                        case "-vv" -> "ALL";
                        default -> throw new IllegalArgumentException(
                            """
                            Usage: java -jar chat-server-file [OPTION]

                            -v   log warnings
                            -vv  log everything

                            Log more information. Writes to 'server.log' file
                            and prints same information to active console.
                            
                            """
                        );
                    };
                }
            } catch(IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;  // exit program
            }

        Logger log = Logger.getLogger("chatserver");
        try {
            /* configure logging */
            FileHandler filehandler = new FileHandler("server.log");
            filehandler.setFormatter(new MyFormatter());
            ConsoleHandler consolehandler = new ConsoleHandler();
            consolehandler.setFormatter(new MyFormatter());
            log.setLevel(Level.parse(logLevel));
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
            }
            );

           
            

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
                "/registration", new RegistrationHandler(cauth));

            log.info("/registration context created...");

            /* connect to database */
            // ChatDatabase.getInstance().open("chat.db");
            ChatDatabase.getInstance().open(DATABASE_FILE);

            /* start ChatServer using threadpool */
            // ExecutorService pool = Executors.newFixedThreadPool(30);
            ExecutorService pool = Executors.newCachedThreadPool();
            server.setExecutor(pool);
            server.start();
            log.info("server started...");

            /* wait keyboard input */
            boolean running = true;
            BufferedReader stdin = new BufferedReader(
                new InputStreamReader(System.in));

            while(running) {
                if(stdin.readLine().equals("/quit")) {
                    running = false;
                }
            }
            
            log.info("server is shutting down..");

            server.stop(3);
            ChatDatabase.getInstance().connection.close();
            pool.shutdownNow();

            log.info("server stopped successfully.");
            System.out.println("server stopped.");

        } catch (IOException e) {
            e.printStackTrace();
            log.log(Level.SEVERE, e.getMessage(), e);
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
