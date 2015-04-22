package hu.bme.google.auth;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by Zoltán on 2015.04.21..
 */
public class ServerThread implements Runnable {

    private Server server;

    private static volatile String code;
    private static volatile String token;
    private static volatile String refreshToken;

    private void createServer() throws Exception {

        server = new Server(8080);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(AuthenticationServlet.class, "/*");

        // szerver elindítása
        server.start();
        server.join();
    }


    @Override
    public void run() {
        try {
            createServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class AuthenticationServlet extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if (req.getParameter("code") != null) {
                code = req.getParameter("code");
                //System.out.println(code);
            }

            Enumeration<String> params = req.getParameterNames();

            while (params.hasMoreElements()) {
                String key = params.nextElement();
                System.out.println(key + ": " + req.getParameter(key));
            }
        }
    }

    public Server getServer() {
        return server;
    }

    public static String getCode() {
        return code;
    }
}
