package org.istio.jwtsecuritytokenservice.config;

import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.Resteasy;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakTransaction;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class SessionServletFilter implements Filter {
    public SessionServletFilter() {
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        servletRequest.setCharacterEncoding("UTF-8");
        final HttpServletRequest request = (HttpServletRequest)servletRequest;
        KeycloakSessionFactory sessionFactory = (KeycloakSessionFactory)servletRequest.getServletContext().getAttribute(KeycloakSessionFactory.class.getName());
        KeycloakSession session = sessionFactory.create();
        Resteasy.pushContext(KeycloakSession.class, session);
        ClientConnection connection = new ClientConnection() {
            public String getRemoteAddr() {
                return request.getRemoteAddr();
            }

            public String getRemoteHost() {
                return request.getRemoteHost();
            }

            public int getRemotePort() {
                return request.getRemotePort();
            }

            public String getLocalAddr() {
                return request.getLocalAddr();
            }

            public int getLocalPort() {
                return request.getLocalPort();
            }
        };
        // session.getContext().setConnection(connection);
        Resteasy.pushContext(ClientConnection.class, connection);
        KeycloakTransaction tx = session.getTransactionManager();
        Resteasy.pushContext(KeycloakTransaction.class, tx);
        tx.begin();

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            if (servletRequest.isAsyncStarted()) {
                servletRequest.getAsyncContext().addListener(this.createAsyncLifeCycleListener(session));
            } else {
                this.closeSession(session);
            }

        }

    }

    private AsyncListener createAsyncLifeCycleListener(final KeycloakSession session) {
        return new AsyncListener() {
            public void onComplete(AsyncEvent event) {
                SessionServletFilter.this.closeSession(session);
            }

            public void onTimeout(AsyncEvent event) {
                SessionServletFilter.this.closeSession(session);
            }

            public void onError(AsyncEvent event) {
                SessionServletFilter.this.closeSession(session);
            }

            public void onStartAsync(AsyncEvent event) {
            }
        };
    }

    private void closeSession(KeycloakSession session) {
        if (session.getTransactionManager() != null && session.getTransactionManager().isActive()) {
            session.getTransactionManager().rollback();
        }

        session.close();
        Resteasy.clearContextData();
    }

    public void destroy() {
    }
}