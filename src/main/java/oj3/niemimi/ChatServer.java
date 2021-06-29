package oj3.niemimi;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
//import java.net.http.HttpConnectTimeoutException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
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
        try {
            HttpsServer server = HttpsServer.create(
                                new InetSocketAddress(PORT), 0);
            System.out.println("server port: " + PORT);
            SSLContext sslContext = chatServerSSLContext();

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    InetSocketAddress remote = params.getClientAddress();
                    System.out.println(remote + " connected...");
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
            System.out.println("/chat context created...");

            /* create path: /registration    */
            server.createContext(
                "/registration",
                new RegistrationHandler(cauth));
            System.out.println("/registration context created...");


            server.setExecutor(null);
            server.start();
            System.out.println("server started...");


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO error. Unable to create server.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error creating server.");
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
