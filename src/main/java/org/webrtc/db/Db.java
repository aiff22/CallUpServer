package org.webrtc.db;

import org.webrtc.common.Helper;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

// Message types:
//
//                      1 - sent message
//                      2 - incoming message, new
//                      3 - incoming message, read

// Event types:
//
//                      1 - new message
//                      2 - new call
//                      3 - missing call
//                      4 - new contact

// Contact status:
//
//                      1 - in contact list
//                      2 - friend
//                      3 - incoming contact, status pending

// Message status:
//
//                      1 - new
//                      0 - read

// Call status:
//
//                      1 - outcoming
//                      2 - incoming
//                      3 - missing

public class Db {
    private static final Logger logger = Logger.getLogger(Db.class.getName());

    public Db() {

        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:~/users");
            Statement stat = conn.createStatement();


            //stat.execute("DROP table users");
            //stat.execute("DROP table contacts");
            //stat.execute("DROP table calls");
            //stat.execute("DROP table messages");
            //stat.execute("DROP table events");

            stat.execute("create table users(id INT primary key, password VARCHAR (20), id_name VARCHAR (20), status BIGINT, email VARCHAR (20))");
            stat.execute("create table contacts(id INT, id_contact int, contact_name VARCHAR (20), contact_status INT)");
            stat.execute("create table calls(id INT, id_contact int, call_date TIMESTAMP , call_status INT)");
            stat.execute("create table messages(id INT, id_contact int, msg_text VARCHAR (200), msg_status INT)");
            stat.execute("create table events(id INT, id_contact INT, event_text VARCHAR (200), event_type INT)");



            // *** Add test user ***

            /*conn = DriverManager.getConnection("jdbc:h2:~/users");

            String insertTableSQL = "INSERT INTO users"
                    + "(id, password, id_name, status, email) VALUES"
                    + "(?,?,?,?,?)";

            PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.setInt(1, 00000001);
            preparedStatement.setString(2, "1234");
            preparedStatement.setString(3, "TestCallUp");
            preparedStatement.setLong(4, System.currentTimeMillis());
            preparedStatement.setString(5, "callup.com@gmail.com");
            preparedStatement.executeUpdate();
            */

            stat.close();
            conn.close();
            logger.info("Create new database!");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public List<List<String>> getclient(Integer login, String pass) throws SQLException {

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        Statement stat = conn.createStatement();
        boolean exist = stat.executeQuery("select * from users where id = " + login + " AND password = " + pass).next();

        if (exist) {

            List<List<String>> list = new ArrayList<List<String>>();
            ResultSet res = stat.executeQuery("select * from contacts where id = " + login);
            while (res.next())
                list.add(Arrays.asList("contacts", res.getString("id_contact"), res.getString("contact_name"), res.getString("contact_status")));

            res = stat.executeQuery("select * from calls where id = " + login);
            while (res.next())
                list.add(Arrays.asList("calls", res.getString("id_contact"), res.getString("call_date"), res.getString("call_status")));

            res = stat.executeQuery("select * from messages where id = " + login);
            while (res.next())
                list.add(Arrays.asList("messages", res.getString("id_contact"), res.getString("msg_text"), res.getString("msg_status")));

            res = stat.executeQuery("select * from events where id = " + login);
            while (res.next())
                list.add(Arrays.asList("events", res.getString("id_contact"), res.getString("event_text"), res.getString("event_type")));

            stat.close();
            conn.close();

            return list;

        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            throw new RuntimeException("User not found");
        }

    }

    public boolean checkId(Integer id) throws SQLException {

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        Statement stat = conn.createStatement();
        boolean exist = stat.executeQuery("select * from users where id = " + id).next();

        stat.close();
        conn.close();

        logger.info("User exist: " + exist);
        return exist ? true : false;
    }

    public List<List<String>> register(Integer login, String pass, String id_name, String email) throws SQLException {

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        String insertTableSQL = "INSERT INTO users"
                + "(id, password, id_name, status, email) VALUES"
                + "(?,?,?,?,?)";

        PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
        preparedStatement.setInt(1, login);
        preparedStatement.setString(2, pass);
        preparedStatement.setString(3, id_name);
        preparedStatement.setLong(4, System.currentTimeMillis());
        preparedStatement.setString(5, email);
        preparedStatement.executeUpdate();


        // Insert test data ->

        // *** Contacts ***

        insertTableSQL = "INSERT INTO contacts"
                + "(id, id_contact, contact_name, contact_status) VALUES"
                + "(?,?,?,?)";

        preparedStatement = conn.prepareStatement(insertTableSQL);
        preparedStatement.setInt(1, login);
        preparedStatement.setInt(2, 00000001);
        preparedStatement.setString(3, "CallUpTest");
        preparedStatement.setInt(4, 2);
        preparedStatement.executeUpdate();

        // *** Calls ***

        insertTableSQL = "INSERT INTO calls"
                + "(id, id_contact, call_date, call_status) VALUES"
                + "(?,?,?,?)";

        preparedStatement = conn.prepareStatement(insertTableSQL);
        preparedStatement.setInt(1, login);
        preparedStatement.setInt(2, 00000001);
        preparedStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
        preparedStatement.setInt(4, 1);
        preparedStatement.executeUpdate();

        insertTableSQL = "INSERT INTO calls"
                + "(id, id_contact, call_date, call_status) VALUES"
                + "(?,?,?,?)";

        preparedStatement = conn.prepareStatement(insertTableSQL);
        preparedStatement.setInt(1, login);
        preparedStatement.setInt(2, 00000001);
        preparedStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
        preparedStatement.setInt(4, 2);
        preparedStatement.executeUpdate();

        insertTableSQL = "INSERT INTO calls"
                + "(id, id_contact, call_date, call_status) VALUES"
                + "(?,?,?,?)";

        preparedStatement = conn.prepareStatement(insertTableSQL);
        preparedStatement.setInt(1, login);
        preparedStatement.setInt(2, 00000001);
        preparedStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
        preparedStatement.setInt(4, 3);
        preparedStatement.executeUpdate();

        // *** Messages ***

        insertTableSQL = "INSERT INTO messages"
                + "(id, id_contact, msg_text, msg_status) VALUES"
                + "(?,?,?,?)";

        preparedStatement = conn.prepareStatement(insertTableSQL);
        preparedStatement.setInt(1, login);
        preparedStatement.setInt(2, 00000001);
        preparedStatement.setString(3, "This is test incoming message");
        preparedStatement.setInt(4, 1);
        preparedStatement.executeUpdate();

        // *** Events ***

        insertTableSQL = "INSERT INTO events"
                + "(id, id_contact, event_text, event_type) VALUES"
                + "(?,?,?,?)";

        preparedStatement = conn.prepareStatement(insertTableSQL);
        preparedStatement.setInt(1, login);
        preparedStatement.setInt(2, 00000001);
        preparedStatement.setString(3, "Hi, friend! Welcome to CallUp!");
        preparedStatement.setInt(4, 1);
        preparedStatement.executeUpdate();

        // <- Insert test data

        preparedStatement.close();
        conn.close();
        logger.info("User added: login = " + Integer.toString(login) + "; password = " + pass);
        return getclient(login, pass);
    }

    public List<List<String>> hello(Integer login, String pass) throws SQLException {

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        Statement stat = conn.createStatement();
        ResultSet rs;
        rs = stat.executeQuery("select * from users where id = " + login + " AND password = " + pass);

        if (rs.next()) {

            String insertTableSQL = "UPDATE users SET status = ? WHERE id = ?";

            PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.setLong(1, System.currentTimeMillis());
            preparedStatement.setInt(2, login);
            preparedStatement.executeUpdate();

            preparedStatement.close();

            List<List<String>> list = new ArrayList<List<String>>();
            rs = stat.executeQuery("select * from events where id = " + login);
            while (rs.next())
                list.add(Arrays.asList("events", rs.getString("id_contact"), rs.getString("event_text"), rs.getString("event_type")));

            rs = stat.executeQuery("select (" + System.currentTimeMillis() + " - status < 12000) from users where id IN " +
                    "(select id_contact from contacts where id = " + login +
                    " AND contact_status = 2)");

            // Delete all events for user

            insertTableSQL = "DELETE FROM events WHERE id = ?";

            preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.setInt(1, login);
            preparedStatement.executeUpdate();

            preparedStatement.close();

            list.add(Arrays.asList("friends_online", rs.toString()));

            return list;

        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            throw new RuntimeException("User not found");
        }

    }

    public String call(final Integer login, String pass, final Integer id_contact) throws SQLException {

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        final Statement stat = conn.createStatement();
        ResultSet rs;
        rs = stat.executeQuery("select * from users where id = " + login + " AND password = " + pass);

        if (rs.next()) {

            rs = stat.executeQuery("select * from users where id = " + id_contact);

            if (rs.next()) {

                String insertTableSQL = "INSERT INTO events"
                        + "(id, id_contact, event_text, event_type) VALUES"
                        + "(?,?,?,?)";

                PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
                preparedStatement.setInt(1, id_contact);
                preparedStatement.setInt(2, login);
                preparedStatement.setString(3, "");
                preparedStatement.setInt(4, 2);
                preparedStatement.executeUpdate();

                preparedStatement.close();

                insertTableSQL = "INSERT INTO calls"
                        + "(id, id_contact, event_text, event_type) VALUES"
                        + "(?,?,?,?)";

                preparedStatement = conn.prepareStatement(insertTableSQL);
                preparedStatement.setInt(1, login);
                preparedStatement.setInt(2, id_contact);
                preparedStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
                preparedStatement.setInt(4, 1);
                preparedStatement.executeUpdate();

                preparedStatement.close();

                logger.info("Invite for user " + Integer.toString(id_contact) + " is sent!");

                final String[] room = new String[]{"-1"};
                final Timer timer = new Timer();

                final AtomicInteger sec = new AtomicInteger();

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        try {

                            ResultSet rs = stat.executeQuery("select * from events where id = " + login +
                                    " AND id_contact = " + id_contact + " AND event_type = 2");

                            if (rs.next() || sec.get() == 20) {

                                room[0] = rs.getString("event_text");
                                timer.cancel();

                            } else sec.incrementAndGet();

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }


                    }
                }, 1000, 1000);

                stat.close();
                conn.close();
                return room[0];

            } else {
                logger.info("No peer " + Integer.toString(id_contact) + "is found!");
                throw new RuntimeException("Peer does not exist");
            }

        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            throw new RuntimeException("User not found");
        }
    }

    public String respcall(Integer login, String pass, Integer id_contact, Integer decision) throws SQLException {

        // decision: 0 - decline, 1 - accept

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        Statement stat = conn.createStatement();
        ResultSet rs;
        rs = stat.executeQuery("select * from users where id = " + Integer.toString(login) + " AND password = " + pass);

        if (rs.next()) {

            String room = "-1";
            Integer callStatus = 3;

            if (decision == 1) {
                room = Helper.generate_random(17);
                callStatus = 2;
            }

            String insertTableSQL = "INSERT INTO events"
                    + "(id, id_contact, event_text, event_type) VALUES"
                    + "(?,?,?,?)";

            PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.setInt(1, id_contact);
            preparedStatement.setInt(2, login);
            preparedStatement.setString(3, room);
            preparedStatement.setInt(4, 2);
            preparedStatement.executeUpdate();

            preparedStatement.close();

            // Add incoming call to user table

            insertTableSQL = "INSERT INTO calls"
                    + "(id, id_contact, event_text, event_type) VALUES"
                    + "(?,?,?,?)";

            preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.setInt(1, login);
            preparedStatement.setInt(2, id_contact);
            preparedStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
            preparedStatement.setInt(4, callStatus);
            preparedStatement.executeUpdate();

            preparedStatement.close();

            return room;

        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            throw new RuntimeException("User not found");
        }
    }

    public int send_msg(Integer login, String pass, Integer id_contact, String msg_text) throws SQLException {

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        Statement stat = conn.createStatement();
        boolean exist = stat.executeQuery("select * from users where id = " + login + " AND password = " + pass).next();

        if (exist) {

            ResultSet rs = stat.executeQuery("select * from users where id = " + id_contact);

            if (rs.next()) {

                // Insert data into sender message table

                String insertTableSQL = "INSERT INTO messages"
                        + "(id, id_contact, msg_text, msg_status) VALUES"
                        + "(?,?,?,?)";

                PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
                preparedStatement.setInt(1, login);
                preparedStatement.setInt(2, id_contact);
                preparedStatement.setString(3, msg_text);
                preparedStatement.setInt(4, 1);
                preparedStatement.executeUpdate();

                preparedStatement.close();


                // Insert data into receiver message table

                insertTableSQL = "INSERT INTO messages"
                        + "(id, id_contact, msg_text, msg_status) VALUES"
                        + "(?,?,?,?)";

                preparedStatement = conn.prepareStatement(insertTableSQL);
                preparedStatement.setInt(1, id_contact);
                preparedStatement.setInt(2, login);
                preparedStatement.setString(3, msg_text);
                preparedStatement.setInt(4, 2);
                preparedStatement.executeUpdate();

                preparedStatement.close();

                // Insert data into receiver event table

                insertTableSQL = "INSERT INTO events"
                        + "(id, id_contact, event_text, event_type) VALUES"
                        + "(?,?,?,?)";

                preparedStatement = conn.prepareStatement(insertTableSQL);
                preparedStatement.setInt(1, id_contact);
                preparedStatement.setInt(2, login);
                preparedStatement.setString(3, msg_text);
                preparedStatement.setInt(4, 1);
                preparedStatement.executeUpdate();

                preparedStatement.close();

                conn.close();

                return 1;
            } else {
                logger.info("No peer " + Integer.toString(id_contact) + "is found!");
                throw new RuntimeException("Peer does not exist");
            }

        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            throw new RuntimeException("User not found");
        }
    }

    public int add_contact(Integer login, String pass, Integer id_contact, Integer be_friends, String contact_name) throws SQLException {

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * from users where id = " + login + " AND password = " + pass);

        if (rs.next()) {

            String id_name = rs.getString("id_name");

            rs = stat.executeQuery("select * from users where id = " + id_contact);

            if (rs.next()) {

                rs = stat.executeQuery("select * from contacts where id = " + login + " AND id_contact = " + id_contact);

                if (!rs.next() || (be_friends == 1 && Integer.valueOf(rs.getString("contact_status")) == 1)) {

                    if (!rs.next()) {
                        String insertTableSQL = "INSERT INTO contacts"
                                + "(id, id_contact, contact_name, contact_status) VALUES"
                                + "(?,?,?,?)";

                        PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
                        preparedStatement.setInt(1, login);
                        preparedStatement.setInt(2, id_contact);
                        preparedStatement.setString(3, contact_name);
                        preparedStatement.setInt(4, 1);
                        preparedStatement.executeUpdate();

                        preparedStatement.close();
                    }

                    if (be_friends == 1) {

                        // Add contact to peer contact table

                        String insertTableSQL = "INSERT INTO contacts"
                                + "(id, id_contact, name_contact, contact_status) VALUES"
                                + "(?,?,?,?)";

                        PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
                        preparedStatement.setInt(1, id_contact);
                        preparedStatement.setInt(2, login);
                        preparedStatement.setString(3, id_name);
                        preparedStatement.setInt(4, 3);
                        preparedStatement.executeUpdate();

                        preparedStatement.close();

                        // Add contact to peer events table

                        insertTableSQL = "INSERT INTO events"
                                + "(id, id_contact, event_text, event_type) VALUES"
                                + "(?,?,?,?)";

                        preparedStatement = conn.prepareStatement(insertTableSQL);
                        preparedStatement.setInt(1, id_contact);
                        preparedStatement.setInt(2, login);
                        preparedStatement.setString(3, id_name);
                        preparedStatement.setInt(4, 4);
                        preparedStatement.executeUpdate();

                        preparedStatement.close();

                    }

                    return 1;

                } else {
                    logger.info("The contact is already in the list!");
                    if (be_friends == 0) throw new RuntimeException("The contact exists in your phone book");
                    else throw new RuntimeException("The contact is your friend");
                }

            } else {
                logger.info("No peer " + Integer.toString(id_contact) + "is found!");
                throw new RuntimeException("Peer does not exist");
            }
        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            throw new RuntimeException("User not found");
        }
    }

    public int unfriend(Integer login, String pass, Integer id_contact) throws SQLException {

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * from users where id = " + login + " AND password = " + pass);

        if (rs.next()) {

            rs = stat.executeQuery("select * from contacts where id = " + login + " AND id_contact = " + id_contact);

            if (rs.next()) {

                // Update user contact table

                String insertTableSQL = "UPDATE contacts SET contact_status = ? WHERE id = ? AND id_contact = ?";

                PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
                preparedStatement.setInt(1, 1);
                preparedStatement.setInt(2, login);
                preparedStatement.setInt(3, id_contact);
                preparedStatement.executeUpdate();

                preparedStatement.close();

                // Update peer's contact table

                insertTableSQL = "UPDATE contacts SET contact_status = ? WHERE id = ? AND id_contact = ?";

                preparedStatement = conn.prepareStatement(insertTableSQL);
                preparedStatement.setInt(1, 1);
                preparedStatement.setInt(2, id_contact);
                preparedStatement.setInt(3, login);
                preparedStatement.executeUpdate();

                preparedStatement.close();

                return 1;

            } else {
                logger.info("Peer " + Integer.toString(id_contact) + " not found!");
                throw new RuntimeException("Peer not found");
            }

        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            throw new RuntimeException("User not found");
        }
    }

    public int delcontact(Integer login, String pass, Integer id_contact) throws SQLException {

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * from users where id = " + login + " AND password = " + pass);

        if (rs.next()) {

            rs = stat.executeQuery("select * from contacts where id = " + login + " AND id_contact = " + id_contact);

            if (rs.next()) {

                // Update user contact table

                String insertTableSQL = "DELETE  FROM contacts WHERE id = ? AND id_contact = ?";

                PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
                preparedStatement.setInt(1, login);
                preparedStatement.setInt(2, id_contact);
                preparedStatement.executeUpdate();

                preparedStatement.close();

                // Update peer's contact table

                rs = stat.executeQuery("select * from contacts where id = " + id_contact + " AND id_contact = " + login);

                if (Integer.valueOf(rs.getString("contact_status")) == 3) {

                    insertTableSQL = "DELETE * FROM contacts WHERE id = ? AND id_contact = ?";

                    preparedStatement = conn.prepareStatement(insertTableSQL);
                    preparedStatement.setInt(1, id_contact);
                    preparedStatement.setInt(2, login);
                    preparedStatement.executeUpdate();

                    preparedStatement.close();

                } else {

                    insertTableSQL = "UPDATE contacts SET contact_status = ? WHERE id = ? AND id_contact = ?";

                    preparedStatement = conn.prepareStatement(insertTableSQL);
                    preparedStatement.setInt(1, 1);
                    preparedStatement.setInt(2, id_contact);
                    preparedStatement.setInt(3, login);
                    preparedStatement.executeUpdate();

                    preparedStatement.close();
                }

                return 1;

            } else {
                logger.info("Peer " + Integer.toString(id_contact) + " not found!");
                throw new RuntimeException("Peer not found");
            }

        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            throw new RuntimeException("User not found");
        }

    }

    public int msgstatus(Integer login, String pass, Integer id_contact) throws SQLException {

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * from users where id = " + login + " AND password = " + pass);

        if (rs.next()) {

            String insertTableSQL = "UPDATE messages SET msg_status = ? WHERE id = ? AND id_contact = ?";

            PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.setInt(1, 0);
            preparedStatement.setInt(2, login);
            preparedStatement.setInt(3, id_contact);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            return 1;

        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            throw new RuntimeException("User not found");
        }

    }
}


