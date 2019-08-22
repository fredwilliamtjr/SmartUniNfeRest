package br.com.smartfull.rest;

import org.jboss.logging.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class BasicAuthenticationPoint extends BasicAuthenticationEntryPoint {

    private static final Logger logger = Logger.getLogger(BasicAuthenticationPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
            throws IOException, ServletException {
        logger.info("REQUEST SEM AUTENTICACAO");
        logger.info("SOLICITADO : " + request.getRequestURI());
        logger.info("PELO IP    : " + request.getRemoteAddr());
        response.addHeader("WWW-Authenticate", "Basic realm=" +getRealmName());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = response.getWriter();
        writer.println("HTTP Status 401 - " + authEx.getMessage());
        writer.println("Solicite seu usuário e senha ao suporte");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setRealmName("Fwtj");
        super.afterPropertiesSet();
    }


}
