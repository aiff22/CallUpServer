package org.webrtc.web;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.webrtc.common.Helper;
import org.webrtc.db.Db;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Logger;

public class RequestServlet extends HttpServlet {

    private Db db = new Db();
    private static final Logger logger = Logger.getLogger(RequestServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Enumeration<String> parameterNames = req.getParameterNames();
        String value = req.getParameterValues("request_type")[0];

        try {

            if (value.equals("getId")) getId(req, resp);
            if (value.equals("login")) login(req, resp);
            if (value.equals("register")) register(req, resp);
            if (value.equals("hello")) hello(req, resp);
            if (value.equals("call")) call(req, resp);
            if (value.equals("msg")) msg(req, resp);
            if (value.equals("contact")) contact(req, resp);
            if (value.equals("unfriend")) unfriend(req, resp);
            if (value.equals("delcontact")) delcontact(req, resp);
            if (value.equals("msgstatus")) msgstatus(req, resp);
            if (value.equals("respcall")) respcall(req, resp);


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void respcall(HttpServletRequest req, HttpServletResponse resp) {

        Integer login = Integer.valueOf(req.getParameterValues("login")[0]);
        String pass = req.getParameterValues("pass")[0];
        Integer id_contact = Integer.valueOf(req.getParameterValues("id_contact")[0]);
        Integer decision = Integer.valueOf(req.getParameterValues("decision")[0]);

        try {
            resp.getWriter().print(db.respcall(login, pass, id_contact, decision));
        } catch (SQLException e) {
//>>
            e.printStackTrace();
        } catch (IOException e) {
//>>
            e.printStackTrace();
        }
    }

    private void msgstatus(HttpServletRequest req, HttpServletResponse resp) {

        Integer login = Integer.valueOf(req.getParameterValues("login")[0]);
        String pass = req.getParameterValues("pass")[0];
        Integer id_contact = Integer.valueOf(req.getParameterValues("id_contact")[0]);

        try {
            resp.getWriter().print(Integer.toString(db.msgstatus(login, pass, id_contact)));
        } catch (SQLException e) {
//>>
            e.printStackTrace();
        } catch (IOException e) {
//>>
            e.printStackTrace();
        }
    }

    private void delcontact(HttpServletRequest req, HttpServletResponse resp) {

        Integer login = Integer.valueOf(req.getParameterValues("login")[0]);
        String pass = req.getParameterValues("pass")[0];
        Integer id_contact = Integer.valueOf(req.getParameterValues("id_contact")[0]);

        try {
            resp.getWriter().print(Integer.toString(db.delcontact(login, pass, id_contact)));
        } catch (SQLException e) {
//>>
            e.printStackTrace();
        } catch (IOException e) {
//>>
            e.printStackTrace();
        }
    }

    private void unfriend(HttpServletRequest req, HttpServletResponse resp) {

        Integer login = Integer.valueOf(req.getParameterValues("login")[0]);
        String pass = req.getParameterValues("pass")[0];
        Integer id_contact = Integer.valueOf(req.getParameterValues("id_contact")[0]);

        try {
            resp.getWriter().print(Integer.toString(db.unfriend(login, pass, id_contact)));
        } catch (SQLException e) {
//>>
            e.printStackTrace();
        } catch (IOException e) {
//>>
            e.printStackTrace();
        }

    }

    private void msg(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Integer login = Integer.valueOf(req.getParameterValues("login")[0]);
        String pass = req.getParameterValues("pass")[0];
        Integer id_contact = Integer.valueOf(req.getParameterValues("id_contact")[0]);
        String msg_text = req.getParameterValues("msg_text")[0];

        logger.info("User " + Integer.toString(login) + "send message to user " + Integer.toString(id_contact));

        try {
            resp.getWriter().print(db.send_msg(login, pass, id_contact, msg_text));
        } catch (SQLException e) {
            resp.getWriter().print("-3");
            e.printStackTrace();
        } catch (IOException e) {
//>>
            e.printStackTrace();
        }

    }

    private void contact(HttpServletRequest req, HttpServletResponse resp) {

        Integer login = Integer.valueOf(req.getParameterValues("login")[0]);
        String pass = req.getParameterValues("pass")[0];
        String name = req.getParameterValues("contact_name")[0];
        Integer id_contact = Integer.valueOf(req.getParameterValues("id_contact")[0]);
        Integer be_friends = Integer.valueOf(req.getParameterValues("be_friends")[0]);

        try {
            resp.getWriter().print(Integer.toString(db.add_contact(login, pass, id_contact, be_friends, name)));
        } catch (SQLException e) {
//>>
            e.printStackTrace();
        } catch (IOException e) {
//>>
            e.printStackTrace();
        }

    }

    private void call(HttpServletRequest req, HttpServletResponse resp) throws SQLException {

        Integer login = Integer.valueOf(req.getParameterValues("login")[0]);
        String pass = req.getParameterValues("pass")[0];
        Integer id_contact = Integer.valueOf(req.getParameterValues("id_contact")[0]);

        logger.info("User " + Integer.toString(login) + "calls user " + Integer.toString(id_contact));
        try {
            resp.getWriter().print(db.call(login, pass, id_contact));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void hello(HttpServletRequest req, HttpServletResponse resp) throws SQLException {

        Integer login = Integer.valueOf(req.getParameterValues("login")[0]);
        String pass = req.getParameterValues("pass")[0];

        logger.info("User " + Integer.toString(login) + "says hello.");
        try {

            /*Kryo kryo = new Kryo();
            OutputStream stream = resp.getOutputStream();
            Output out = new Output(stream);
            kryo.writeObject(out, db.hello(login, pass));
            out.flush();
            out.close();*/

            OutputStream out = resp.getOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(out);
            stream.writeObject(db.hello(login, pass));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void register(HttpServletRequest req, HttpServletResponse resp) throws SQLException {

        Integer login = Integer.valueOf(req.getParameterValues("login")[0]);
        String pass = req.getParameterValues("pass")[0];
        String email = req.getParameterValues("email")[0];
        String name = req.getParameterValues("name")[0];

        logger.info("New user tries to register; login = " + Integer.toString(login) + ", password = " + pass);

        if (!db.checkId(login)) {
            try {

                OutputStream out = resp.getOutputStream();
                ObjectOutputStream stream = new ObjectOutputStream(out);
                stream.writeObject(db.register(login, pass, name, email));

            } catch (IOException e) {
                try {
                    resp.getWriter().print("-1");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        } else {

            //  Invalid phone number

            try {
                resp.getWriter().print("-2");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    private void getId(HttpServletRequest req, HttpServletResponse resp) throws SQLException {

        Integer id = Integer.valueOf(Helper.generate_random(8));
        while (db.checkId(id)) id = Integer.valueOf(Helper.generate_random(8));

        logger.info("New id created: " + Integer.toString(id));

        try {
//>>
            resp.getWriter().print(id);
        } catch (IOException e) {
//>>            
            e.printStackTrace();
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws SQLException {

        Integer login = Integer.valueOf(req.getParameterValues("login")[0]);
        String pass = req.getParameterValues("pass")[0];
        logger.info("New user tries to log in; login = " + Integer.toString(login) + ", password = " + pass);

        try {

            /*Kryo kryo = new Kryo();
            OutputStream stream = resp.getOutputStream();
            Output out = new Output(stream);
            kryo.writeObject(out, db.getclient(login, pass));
            out.flush();
            out.close();
*/

            OutputStream out = resp.getOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(out);
            stream.writeObject(db.getclient(login, pass));
            //stream.flush();
            //stream.close();

            //resp.getWriter().print(db.getclient(login, pass));

        } catch (IOException e) {
            try {
                resp.getWriter().print("-2");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } catch (RuntimeException e) {
            try {
                resp.getWriter().print("-1");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.getMessage();
        }
    }
}


//Kryo kryo = new Kryo();
//InputStream stream = req.getInputStream();
//Input input = new Input(stream);
//List<List<String>> list = kryo.readObject(input, List.class);
//input.close;