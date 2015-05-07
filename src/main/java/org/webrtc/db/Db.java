package org.webrtc.db;

import org.webrtc.common.Helper;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
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
//                      5 - request accepted
//                      6 - friend deleted

// Contact status:
//
//                      0 - in contact list, friend: status pending
//                      1 - in contact list
//                      2 - friend
//                      3 - incoming contact, status pending

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

            /*String insertTableSQL = "DELETE  FROM contacts WHERE id = 91176959 AND id_contact = 51892423";

            PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.executeUpdate();

            insertTableSQL = "DELETE  FROM contacts WHERE id = 51892423 AND id_contact = 91176959";
            preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.executeUpdate();*/

            //stat.execute("DROP table users");
            //stat.execute("DROP table contacts");
            //stat.execute("DROP table calls");
            //stat.execute("DROP table messages");
            //stat.execute("DROP table events");

            stat.execute("create table users(id INT primary key, password VARCHAR (20), id_name VARCHAR (20), status BIGINT, email VARCHAR (20))");
            stat.execute("create table contacts(id INT, id_contact int, contact_name VARCHAR (20), contact_status INT)");
            stat.execute("create table calls(id INT, id_contact int, call_date TIMESTAMP , call_status INT)");
            stat.execute("create table messages(id INT, id_contact int, msg_text VARCHAR (200), msg_status INT, msg_date TIMESTAMP)");
            stat.execute("create table events(id INT, id_contact INT, event_text VARCHAR (200), event_type INT, event_date TIMESTAMP)");


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
                list.add(Arrays.asList("messages", res.getString("id_contact"), res.getString("msg_text"), res.getString("msg_status"), res.getString("msg_date")));

            res = stat.executeQuery("select * from events where id = " + login);
            while (res.next())
                list.add(Arrays.asList("events", res.getString("id_contact"), res.getString("event_text"), res.getString("event_type"), res.getString("event_date")));


            ResultSet rs = stat.executeQuery("select (" + System.currentTimeMillis() + " - status < 12000) as online from users where id IN " +
                    "(select id_contact from contacts where id = " + login +
                    " AND contact_status = 2)");

            // Delete all events for user

            String insertTableSQL = "DELETE FROM events WHERE id = ?";

            PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.setInt(1, login);
            preparedStatement.executeUpdate();

            preparedStatement.close();

            List online_status = new ArrayList();
            online_status.add("friends_online");
            while (rs.next()) online_status.add(String.valueOf(rs.getBoolean("online")));
            list.add(online_status);

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
                + "(id, id_contact, msg_text, msg_status, msg_date) VALUES"
                + "(?,?,?,?,?)";

        preparedStatement = conn.prepareStatement(insertTableSQL);
        preparedStatement.setInt(1, login);
        preparedStatement.setInt(2, 00000001);
        preparedStatement.setString(3, "This is test incoming message");
        preparedStatement.setInt(4, 3);
        preparedStatement.setTimestamp(5, new Timestamp(new Date().getTime()));
        preparedStatement.executeUpdate();

        insertTableSQL = "INSERT INTO messages"
                + "(id, id_contact, msg_text, msg_status, msg_date) VALUES"
                + "(?,?,?,?,?)";

        preparedStatement = conn.prepareStatement(insertTableSQL);
        preparedStatement.setInt(1, login);
        preparedStatement.setInt(2, 00000001);
        preparedStatement.setString(3, "This is new unread message");
        preparedStatement.setInt(4, 2);
        preparedStatement.setTimestamp(5, new Timestamp(new Date().getTime()));
        preparedStatement.executeUpdate();

        // *** Events ***

        insertTableSQL = "INSERT INTO events"
                + "(id, id_contact, event_text, event_type, event_date) VALUES"
                + "(?,?,?,?,?)";

        preparedStatement = conn.prepareStatement(insertTableSQL);
        preparedStatement.setInt(1, login);
        preparedStatement.setInt(2, 00000001);
        preparedStatement.setString(3, "Hi, friend! Welcome to CallUp!");
        preparedStatement.setInt(4, 1);
        preparedStatement.setTimestamp(5, new Timestamp(new Date().getTime()));
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
                list.add(Arrays.asList("events", rs.getString("id_contact"), rs.getString("event_text"), rs.getString("event_type"), rs.getString(
                        "event_date")));

            rs = stat.executeQuery("select (" + System.currentTimeMillis() + " - status < 12000) as online from users where id IN " +
                    "(select id_contact from contacts where id = " + login +
                    " AND contact_status = 2)");

            // Delete all events for user

            insertTableSQL = "DELETE FROM events WHERE id = ?";

            preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.setInt(1, login);
            preparedStatement.executeUpdate();

            preparedStatement.close();

            List online_status = new ArrayList();
            online_status.add("friends_online");
            while (rs.next()) online_status.add(String.valueOf(rs.getBoolean("online")));
            list.add(online_status);

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

                logger.info("User " + Integer.toString(login) + " makes a call to user " + Integer.toString(id_contact));

                // Update peer's event table

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

                // Update user's calls table

                insertTableSQL = "INSERT INTO calls"
                        + "(id, id_contact, call_date, call_status) VALUES"
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
                final ReentrantLock lock = new ReentrantLock();
                final Condition c = lock.newCondition();
                final Connection finalConn = conn;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        try {

                            ResultSet rs = stat.executeQuery("select event_text from events where id = " + login +
                                    " AND id_contact = " + id_contact + " AND event_type = 2");

                            Boolean isResponse = rs.next();

                            if ((isResponse && !rs.getString("event_text").equals("")) || sec.get() > 30) {

                                if (isResponse && !rs.getString("event_text").equals("")) room[0] = rs.getString("event_text");
                                logger.info("Response room returned: " + room[0]);
                                timer.cancel();
                                stat.close();
                                finalConn.close();

                                lock.lock();
                                c.signal();
                                lock.unlock();

                            } else {
                                sec.incrementAndGet();
                                logger.info("Time passed: " + String.valueOf(sec.get()));
                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }


                    }
                }, 1000, 1000);

                try {
                    lock.lock();
                    //if (room[0] == "-1")
                    c.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }

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

        logger.info("User " + Integer.toString(login) + " is making call response 2");

        if (rs.next()) {

            String room = "-2";
            Integer callStatus = 3;

            if (decision == 1) {
                room = Helper.generate_random(17);
                callStatus = 2;
            }

            logger.info("User " + Integer.toString(login) + " response " + String.valueOf(callStatus));

            logger.info("User " + Integer.toString(login) + " generated room " + room);

            // Update peer's event table

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

            logger.info("User " + Integer.toString(login) + " inserted data to peer's event table");

            // Add incoming call to user table

            insertTableSQL = "INSERT INTO calls"
                    + "(id, id_contact, call_date, call_status) VALUES"
                    + "(?,?,?,?)";

            preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.setInt(1, login);
            preparedStatement.setInt(2, id_contact);
            preparedStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
            preparedStatement.setInt(4, callStatus);
            preparedStatement.executeUpdate();

            preparedStatement.close();

            logger.info("User " + Integer.toString(login) + " inserted data to call table");

            return room;

        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            return "-3";
            //throw new RuntimeException("User not found");
        }
    }

    public String send_msg(Integer login, String pass, Integer id_contact, String msg_text) throws SQLException {

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        Statement stat = conn.createStatement();
        boolean exist = stat.executeQuery("select * from users where id = " + login + " AND password = " + pass).next();

        if (exist) {

            ResultSet rs = stat.executeQuery("select * from users where id = " + id_contact);

            if (rs.next()) {

                // Insert data into sender message table

                String insertTableSQL = "INSERT INTO messages"
                        + "(id, id_contact, msg_text, msg_status, msg_date) VALUES"
                        + "(?,?,?,?,?)";

                PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
                preparedStatement.setInt(1, login);
                preparedStatement.setInt(2, id_contact);
                preparedStatement.setString(3, msg_text);
                preparedStatement.setInt(4, 1);
                preparedStatement.setTimestamp(5, new Timestamp(new Date().getTime()));
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

                return String.valueOf(new Timestamp(new Date().getTime()));
            } else {
                logger.info("No peer " + Integer.toString(id_contact) + "is found!");
                return "-2";
            }

        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            return "-1";
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
                Boolean isNext = rs.next();

                if (!isNext || (be_friends == 1 && Integer.valueOf(rs.getString("contact_status")) != 2)) {

                    if (!isNext) {

                        logger.info("User " + Integer.toString(login) + " added peer to contact table");

                        String insertTableSQL = "INSERT INTO contacts"
                                + "(id, id_contact, contact_name, contact_status) VALUES"
                                + "(?,?,?,?)";

                        PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
                        preparedStatement.setInt(1, login);
                        preparedStatement.setInt(2, id_contact);
                        preparedStatement.setString(3, contact_name);
                        preparedStatement.setInt(4, be_friends == 1 ? 0 : 1);
                        preparedStatement.executeUpdate();

                        preparedStatement.close();
                    }

                    if (be_friends == 1) {

                        // Add contact to peer contact table

                        rs = stat.executeQuery("select contact_status as status from contacts where id = " + String.valueOf(id_contact) + " AND id_contact = " + String.valueOf(login));
                        isNext = rs.next();

                        if (isNext) {

                            logger.info("User " + Integer.toString(login) + " update peer's contact table");

                            String insertTableSQL = "UPDATE contacts SET contact_status = ? WHERE id = ? AND id_contact = ?";

                            PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
                            preparedStatement.setInt(1, rs.getInt("status") == 0 ? 2 : 3);
                            preparedStatement.setInt(2, id_contact);
                            preparedStatement.setInt(3, login);
                            preparedStatement.executeUpdate();

                            preparedStatement.close();

                        } else {

                            logger.info("User " + Integer.toString(login) + " adds new row to peer's contact table");

                            String insertTableSQL = "INSERT INTO contacts"
                                    + "(id, id_contact, contact_name, contact_status) VALUES"
                                    + "(?,?,?,?)";

                            PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
                            preparedStatement.setInt(1, id_contact);
                            preparedStatement.setInt(2, login);
                            preparedStatement.setString(3, id_name);
                            preparedStatement.setInt(4, 3);
                            preparedStatement.executeUpdate();

                            preparedStatement.close();
                        }
                        // Add contact to peer events table

                        String insertTableSQL = "INSERT INTO events"
                                + "(id, id_contact, event_text, event_type, event_date) VALUES"
                                + "(?,?,?,?,?)";

                        PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
                        preparedStatement.setInt(1, id_contact);
                        preparedStatement.setInt(2, login);
                        preparedStatement.setString(3, id_name);
                        if (isNext) preparedStatement.setInt(4, rs.getInt("status") == 0 ? 5 : 4);
                        else preparedStatement.setInt(4, 4);
                        preparedStatement.setTimestamp(5, new Timestamp(new Date().getTime()));
                        preparedStatement.executeUpdate();

                        preparedStatement.close();

                        // Update user contact table

                        insertTableSQL = "UPDATE contacts SET contact_status = ? WHERE id = ? AND id_contact = ?";

                        preparedStatement = conn.prepareStatement(insertTableSQL);
                        if (isNext) preparedStatement.setInt(1, rs.getInt("status") == 0 ? 2 : 0);
                        else preparedStatement.setInt(1, 0);
                        preparedStatement.setInt(2, login);
                        preparedStatement.setInt(3, id_contact);
                        preparedStatement.executeUpdate();

                        preparedStatement.close();

                    }

                    logger.info("User " + String.valueOf(login) + " added user " + String.valueOf(id_contact) + " to friends");

                    return 1;

                } else {
                    logger.info("The contact is already in the list!");
                    if (be_friends == 0)
                        return 2; //throw new RuntimeException("The contact exists in your phone book");
                    else return 3; // throw new RuntimeException("The contact is your friend");
                }

            } else {
                logger.info("No peer " + Integer.toString(id_contact) + "is found!");
                return -2;
                //throw new RuntimeException("Peer does not exist");
            }
        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            return -1;
            //throw new RuntimeException("User not found");
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

                // Add event to peer events table

                insertTableSQL = "INSERT INTO events"
                        + "(id, id_contact, event_text, event_type, event_date) VALUES"
                        + "(?,?,?,?,?)";

                preparedStatement = conn.prepareStatement(insertTableSQL);
                preparedStatement.setInt(1, id_contact);
                preparedStatement.setInt(2, login);
                preparedStatement.setString(3, " ");
                preparedStatement.setInt(4, 6);
                preparedStatement.setTimestamp(5, new Timestamp(new Date().getTime()));
                preparedStatement.executeUpdate();

                preparedStatement.close();

                //

                return 1;

            } else {
                logger.info("Peer " + Integer.toString(id_contact) + " not found!");
                return -2;
                //throw new RuntimeException("Peer not found");
            }

        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            return -1;
            //throw new RuntimeException("User not found");
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


                insertTableSQL = "UPDATE contacts SET contact_status = ? WHERE id = ? AND id_contact = ?";

                preparedStatement = conn.prepareStatement(insertTableSQL);
                preparedStatement.setInt(1, 1);
                preparedStatement.setInt(2, id_contact);
                preparedStatement.setInt(3, login);
                preparedStatement.executeUpdate();

                preparedStatement.close();

                // Add event to peer events table

                insertTableSQL = "INSERT INTO events"
                        + "(id, id_contact, event_text, event_type, event_date) VALUES"
                        + "(?,?,?,?,?)";

                preparedStatement = conn.prepareStatement(insertTableSQL);
                preparedStatement.setInt(1, id_contact);
                preparedStatement.setInt(2, login);
                preparedStatement.setString(3, " ");
                preparedStatement.setInt(4, 6);
                preparedStatement.setTimestamp(5, new Timestamp(new Date().getTime()));
                preparedStatement.executeUpdate();

                preparedStatement.close();

                //

                return 1;

            } else return -2;

        } else {
            logger.info("User " + Integer.toString(login) + " not found!");
            return -1;
            //throw new RuntimeException("User not found");
        }

    }

    public int msgstatus(Integer login, String pass, Integer id_contact) throws SQLException {

        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:h2:~/users");

        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * from users where id = " + login + " AND password = " + pass);

        if (rs.next()) {

            String insertTableSQL = "UPDATE messages SET msg_status = ? WHERE id = ? AND id_contact = ? AND msg_status = 2";

            PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.setInt(1, 3);
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


