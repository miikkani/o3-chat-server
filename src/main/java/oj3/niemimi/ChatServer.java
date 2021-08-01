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
 */
public class ChatServer {
    private static int PORT = 8001;
    private static String DATABASE_FILE;
    private static String CERTIFICATE;
    private static String PASSWORD;

    private static String LOG_FILE = "server.log";
    private static boolean writeLog = false;
    private static String LOG_LEVEL = "SEVERE";

    private static final String howToUse = """
Usage: java -jar chat-server-file.jar <DATABASE> <CERT> <PASSWORD> [debug={LEVEL}]

    DATABASE     database file
    CERT         JKS certificate file
    PASSWORD     certificate password

    Optional:
    debug={LEVEL}    print additional logging information where LEVEL={1|2|3}.
                     Also writes a log file 'server.log' to current dir.
                        LEVEL
                          1    warnings
                          2    informative
                          3    detail, lots of data. Use carefully

Examples:
    java -jar chat-server-file.jar chat.db keystore.jks password
        Use given database and certificate.

    java -jar chat-server-file.jar chat.db keystore.jks password debug=3
        Write lots of information to 'server.log' file
""";
    public static void main( String[] args ) {
        /* read command line arguments */
        try {
            if(args.length >= 3) {
                DATABASE_FILE = args[0];
                if(new java.io.File(args[1]).exists()) {
                    CERTIFICATE = args[1];
                } else throw new IllegalArgumentException("Certificate file '"
                                    + args[1] + "' not found!\n " + howToUse);
                PASSWORD = args[2];
                if(args.length >= 4) {
                        String[] level = args[3].split("debug=");
                        if(level.length < 2) throw new IllegalArgumentException(howToUse);
                        LOG_LEVEL = switch(level[1]) {
                            case "1" -> "WARNING";
                            case "2" -> "INFO";
                            case "3" -> "FINEST";
                            default -> throw new IllegalArgumentException(howToUse);
                        };
                        writeLog = true;
                }
            } else {
                System.out.println(howToUse);
                return;
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        Logger log = Logger.getLogger("chatserver");
        try {
            /* configure logging */
            ConsoleHandler consolehandler = new ConsoleHandler();
            consolehandler.setFormatter(new MyFormatter());
            log.setLevel(Level.parse(LOG_LEVEL));
            if(writeLog) {
                FileHandler filehandler = new FileHandler(LOG_FILE);
                filehandler.setFormatter(new MyFormatter());
                log.addHandler(filehandler);
            }
            log.addHandler(consolehandler);
            log.setUseParentHandlers(false);
            log.info("Using logging level: " + log.getLevel());
            if(writeLog) log.info("WRITING LOG TO \"" + LOG_FILE + "\"");

            /* start initializing server */
            HttpsServer server = HttpsServer.create(
                                new InetSocketAddress(PORT), 0);
            log.info("using port: " + PORT);
            SSLContext sslContext = chatServerSSLContext();

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    InetSocketAddress remote = params.getClientAddress();
                    log.finest(remote + " connected...");
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
                "/registration", new RegistrationHandler(cauth));

            log.info("/registration context created...");

            /* connect to database */
            ChatDatabase.getInstance().open(DATABASE_FILE);

            /* start ChatServer using threadpool */
            ExecutorService pool = Executors.newCachedThreadPool();
            server.setExecutor(pool);
            server.start();
            log.info("server started...");
            System.out.println("server started...");
            System.out.println("address: https://localhost:" + PORT);
            System.out.println("enter '/quit' to shutdown...");

            /* wait keyboard input */
            boolean running = true;
            BufferedReader stdin = new BufferedReader(
                new InputStreamReader(System.in));

            while(running) {
                if(stdin.readLine().equals("/quit")) {
                    running = false;
                }
            }
            

            log.info("server shutting down...");
            server.stop(3);
            ChatDatabase.getInstance().connection.close();
            pool.shutdownNow();

            log.info("server stopped successfully.");

        } catch (IOException e) {
            // e.printStackTrace();
            log.log(Level.SEVERE, e.getMessage(), e);
        } catch (SQLException sqe) {
            sqe.printStackTrace();
            log.severe("Database error.");
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("SERVER ERROR");
        } finally {
            System.out.println("server stopped.");
        }

    }

    /**
     * Create SSLContext.
     * 
     * @return ssl - SSLContext
     */
    private static SSLContext chatServerSSLContext() throws Exception {
        char[] passphrase = PASSWORD.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(CERTIFICATE), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;




    } 
}
