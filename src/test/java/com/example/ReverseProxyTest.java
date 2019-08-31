package com.example;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

public class ReverseProxyTest {

    private Server server;

    @Before
    public void setup() throws Exception {
        server = new Server();
        // HTTP configuration
        ServerConnector httpConnector = new ServerConnector(server);
        httpConnector.setPort(8080);
        server.setConnectors(new Connector[]{httpConnector});

        ConnectHandler proxy = new ConnectHandler();
        server.setHandler(proxy);
        ServletContextHandler context = new ServletContextHandler(proxy, "/", ServletContextHandler.SESSIONS);
        context.addServlet(EchoHeaderServlet.class, "/hello");

        server.start();
    }

    @Test
    public void shouldAnswerWithTrue() throws Exception {
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(ReverseProxy.class.getResource("/client-keystore.jks").toExternalForm());
        sslContextFactory.setKeyStorePassword("secret123");
        sslContextFactory.setKeyManagerPassword("secret123");
        HttpClient client = new HttpClient(sslContextFactory);
        client.start();

        ContentResponse response = client.newRequest("https://localhost:8443/hello").send();
        System.out.println(response.getContentAsString());
    }

    public static class EchoHeaderServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            Enumeration<String> headers = req.getHeaderNames();

            StringBuilder response = new StringBuilder();
            while (headers.hasMoreElements()) {
                String header = headers.nextElement();
                String headerValue = req.getHeader(header);

                response.append(header)
                        .append(" = ")
                        .append(headerValue)
                        .append('\n');
            }

            resp.setContentType("text/plain");
            resp.getOutputStream().write(response.toString().getBytes());
            resp.setStatus(HttpStatus.OK_200);
        }
    }
}
