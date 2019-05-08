/* CHAT ROOM <MyClass.java>
 * EE422C Project 7 submission by
 * Replace <...> with your actual data.
 * Carlos Villapudua
 * civ298
 * 16190
 * David Day
 * dld2864
 * 16190
 * Slip days used: 3
 * Spring 2019
 */

package ServerSide;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Database {

    public static final String DATABASE_URL = "jdbc:h2:tcp://localhost/~/test";

    @DatabaseTable(tableName = "users")
    static class User {

        @DatabaseField(id = true)
        private String username;

        /*
        @DatabaseField
        private String password;    // char[] more secure than immutable String for password storage
        */

        @DatabaseField
        private String ipAddress;

        public User() {
            // default constructor necessary for H2
        }

        public User(String username, String password, String ipAddress) throws UnknownHostException {
            this.username = username;
            //this.password = password;
            this.ipAddress = ipAddress;
        }


        public String getUsername() {
            return username;
        }

        /*
        public String getPassword() {
            return password;
        }
        */

        public String getIpAddress() {
            return ipAddress;
        }
    }

    /**
     * Add a user to H2 database
     * @param user user to add
     * @param dbUrl URL of database
     * @throws SQLException error occurs during database accessing process
     * @throws IOException error occurs on closing ConnectionSource
     */
    public static void addUsertoDatabase(User user, String dbUrl) throws SQLException, IOException {
        ConnectionSource connectionSource = new JdbcConnectionSource(dbUrl, "sa", "");
        Dao<User, String> userDao = DaoManager.createDao(connectionSource, User.class);

        if (userDao.queryForSameId(user) == null) {
            userDao.create(user);
        }

        connectionSource.close();
    }

    /**
     * Get a user from the database via username
     * @param username username to query
     * @param dbUrl URL of database
     * @return user if present in database, else null
     * @throws SQLException error occurs during database accessing process
     * @throws IOException error occurs on closing ConnectionSource
     */
    public static User getUserFromDatabase(String username, String dbUrl) throws SQLException, IOException {
        ConnectionSource connectionSource = new JdbcConnectionSource(dbUrl, "sa", "");
        Dao<User, String> userDao = DaoManager.createDao(connectionSource, User.class);

        User user = userDao.queryForId(username);

        connectionSource.close();

        return user;
    }

    /**
     * Return a list of all users in the database
     * @param dbUrl URL of database
     * @return list of users, only including usernames
     * @throws SQLException error occurs during database accessing process
     * @throws IOException error occurs on closing ConnectionSource
     */
    public static List<String> getAllUsers(String dbUrl) throws SQLException, IOException {
        List<String> usernames = new ArrayList<>();
        ConnectionSource connectionSource = new JdbcConnectionSource(dbUrl, "sa", "");
        Dao<User, String> userDao = DaoManager.createDao(connectionSource, User.class);

        List<User> users = userDao.queryForAll();

        for (User u : users) {
            usernames.add(u.username);
        }

        connectionSource.close();

        return usernames;
    }

    public static List<String> getUsernamesViaIpAdress(String dbURL, String ipAddress) throws SQLException, IOException {
        List<String> usernames = new ArrayList<>();
        ConnectionSource connectionSource = new JdbcConnectionSource(dbURL, "sa", "");
        Dao<User, String> userDao = DaoManager.createDao(connectionSource, User.class);

        // check ipAddress field of table for match(es)
        List<User> users = userDao.queryForEq("ipAddress", ipAddress);

        for (User u: users) {
            usernames.add(u.username);
        }

        connectionSource.close();

        return usernames;
    }

    /**
     * Remove all users stored in database
     * @param dbUrl URL of database
     * @throws SQLException error occurs during database accessing process
     * @throws IOException error occurs on closing ConnectionSource
     */
    public static void clearDataBase(String dbUrl) throws SQLException, IOException {
        ConnectionSource connectionSource = new JdbcConnectionSource(dbUrl, "sa", "");

        TableUtils.clearTable(connectionSource, User.class);

        connectionSource.close();
    }

    /*
    public static void main(String[] args) throws Exception {
        //addUsertoDatabase(new User("david",null), DATABASE_URL);
        //addUsertoDatabase(new User("carlos",null), DATABASE_URL);

        List<String> usernames = getUsernamesViaIpAdress(DATABASE_URL, "127.0.0.1");

        for (String s: usernames) {
            System.out.println(s);
        }
    }*/
}
