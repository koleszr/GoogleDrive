package hu.bme.google.auth;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Zoltán on 2015.04.21..
 *
 * Temporary Jetty web server which listens for a Google authorization code.
 */
public class ServerTask {

    private Server server;

    private static volatile String code;

    /**
     * Creates a web server on port 8080-
     * @throws Exception
     */
    public void createServer() throws Exception {
        server = new Server(8080);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(AuthenticationServlet.class, "/*");

        server.start();
        server.join();
    }

    public void stopServer() throws Exception {
        server.stop();
    }

    /**
     * Listen for a request that has a "code" parameter.
     */
    public static class AuthenticationServlet extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if (req.getParameter("code") != null) {
                code = req.getParameter("code");
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
