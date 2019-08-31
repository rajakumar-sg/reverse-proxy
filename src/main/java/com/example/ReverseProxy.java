package com.example;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

/**
 * Hello world!
 */
public class ReverseProxy {


    public static void main(String[] args) throws Exception {
        Server server = new Server();

        // HTTPS configuration
        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());

        // Configuring SSL
        SslContextFactory sslContextFactory = new SslContextFactory();

        // Defining keystore path and passwords
        sslContextFactory.setKeyStorePath(ReverseProxy.class.getResource("/my-key.jks").toExternalForm());
        sslContextFactory.setKeyStorePassword("reverse123");
        sslContextFactory.setKeyManagerPassword("reverse123");
        sslContextFactory.setNeedClientAuth(true);

        // Configuring the connector
        ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
        sslConnector.setPort(8443);

        server.setConnectors(new Connector[]{sslConnector});

        ConnectHandler proxy = new ConnectHandler();
        server.setHandler(proxy);
        ServletContextHandler context = new ServletContextHandler(proxy, "/", ServletContextHandler.SESSIONS);
        ServletHolder proxyServlet = new ServletHolder(PCFReverseProxy.class);
        proxyServlet.setInitParameter("proxyTo", "http://localhost:8080/");
        proxyServlet.setInitParameter("prefix", "/");
        context.addServlet(proxyServlet, "/*");

        server.start();
    }

    public static class PCFReverseProxy extends ProxyServlet.Transparent {
        private final Logger log = Logger.getLogger(PCFReverseProxy.class.getName());

        @Override
        protected void addProxyHeaders(HttpServletRequest clientRequest, Request proxyRequest) {
            super.addProxyHeaders(clientRequest, proxyRequest);

            X509Certificate certs[] =
                    (X509Certificate[])clientRequest.getAttribute("javax.servlet.request.X509Certificate");
            if (certs != null && certs.length > 0) {
                X509Certificate clientCert = certs[0];
                X500Principal subjectDN = clientCert.getSubjectX500Principal();
                log.info("Subject " + subjectDN.getName());
            } else {
                log.info("No cert");
            }
        }
    }
}
