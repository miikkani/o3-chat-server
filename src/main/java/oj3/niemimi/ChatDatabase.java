package oj3.niemimi;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;

import java.util.Base64;
import java.security.SecureRandom;
import org.apache.commons.codec.digest.Crypt;

public class ChatDatabase {
    private static ChatDatabase db;
    private Logger log;

    public Connection connection;

    private ChatDatabase(){
        log = Logger.getLogger("chatserver");

    }
    
    public static synchronized ChatDatabase getInstance() {
        if(db == null) {
            db = new ChatDatabase();
        }
        return db;
    }

    /**
     * Opens connection to the database. If database does not exists at given 
     * path, creates new empty database to path.
     * 
     * @param path   location of the database file
     * 
     * @throws SQLException 
     */
    public void open(String path) throws SQLException {
        File database = new File(path);
        boolean hasDatabase = database.exists();
        connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
        log.info( "Database connection established to '" + path + "' ...");
        if(!hasDatabase) {
            createTables();
            log.info("New database created.");
        }
    }

    /**
     * Builds new database with empty tables. Tables are created if there
     * is no database file found in server root folder. SQlite creates new
     * database file automatically when trying to open non-existing file.
     * This method uses the newly created empty database file.
     * 
     * @throws SQLException
     */
    private void createTables() throws SQLException {
        String userTable = """
            CREATE TABLE user (
                name        TEXT    NOT NULL,
                email       TEXT    NOT NULL,
                password    TEXT    NOT NULL,

                PRIMARY KEY (name)
            )
            """;

        String messageTable = """
            CREATE TABLE message (
                sent        INTEGER    NOT NULL,
                nick        TEXT       NOT NULL,
                text        TEXT       NOT NULL,

                PRIMARY KEY (sent)
            )
            """;

        try(Statement stm = connection.createStatement()) {
            log.info("Creating tables...");
            stm.executeUpdate(userTable);
            stm.executeUpdate(messageTable);
        }
    }


    /**
     * Compares given credentials to ones in database. 
     * 
     * @param username
     * @param password
     * @return  true if username and password are found and matching
     */
    public boolean checkCredentials(String username, String password) {
        log.info("params: " + username + " " + password);
        boolean ok = false;
        String preparedQuery = """
            SELECT name, password FROM user
            WHERE name = ? 
            """;

        try(PreparedStatement pstmt = connection.prepareStatement(preparedQuery)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()) {
                String user = rs.getString(1);
                String pw = rs.getString(2);
                log.info("from db: " + user + "::" + pw);
                ok = pw.equals(Crypt.crypt(password, pw));
                log.info("matches (var: ok) -> " + ok);
            }
        } catch(SQLException sqe) {
            log.log(Level.SEVERE, "database error.\n " + sqe.getMessage(), sqe);
        }
        return ok;
    }


    /**
     * Adds user to the database.
     * 
     * @param username
     * @param password
     * @param email
     * 
     * @return true if user was added
     */
    public boolean addUser(String username, String password, String email) {
            boolean ok = false;

            // create salt and encrypt password  
            byte[] bytes = new byte[13];
            new SecureRandom().nextBytes(bytes);
            String salt = new String(Base64.getEncoder().encode(bytes));
            String saltedPassword = Crypt.crypt(password, "$6$" + salt );

            String query = """
                INSERT OR IGNORE INTO user (name, email, password)
                VALUES (?, ?, ?)
                """;
            try(PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, email);
                pstmt.setString(3, saltedPassword);
                int result = pstmt.executeUpdate();
                if(result == 1) ok = true;
            } catch (SQLException e) {
                log.log(Level.WARNING, e.getMessage());
            }
            return ok;
        }

    /**
     * Adds message to the database.
     * 
     * @param time
     * @param user
     * @param message
     * @throws SQLException
     */
    public void addMessage(long time, String user, String message) 
        throws SQLException {
            String query = """
                INSERT INTO message (sent, nick, text)
                VALUES (?, ?, ?)
                """;
            try(PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setLong(1, time);
                pstmt.setString(2, user);
                pstmt.setString(3, message);
                pstmt.executeUpdate();
            }
        }

        /**
         * Constructs list of messages after given time.
         * @param since     a long integer representing time since epoch in
         *                  milliseconds
         * @return
         * @throws SQLException
         */
    public ArrayList<ChatMessage> getMessages(long since) throws SQLException {
        ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
        String query = """
            SELECT * FROM message
            WHERE sent > %d
            ORDER BY sent
            """.formatted(since);
        try(Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                messages.add(new ChatMessage(
                    rs.getString(2),
                    rs.getString(3),
                    rs.getLong(1)
                    ));
            }
        }
        return messages;
    }

}
