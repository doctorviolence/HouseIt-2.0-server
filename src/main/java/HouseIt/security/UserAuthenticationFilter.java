package HouseIt.security;

import HouseIt.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Authenticates user's credentials and, if the user exists in DB, issues them to the Authentication Manager
 **/

@Component
public class UserAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final Logger logger = LoggerFactory.getLogger(UserAuthenticationFilter.class);

    @Value("${security.token.expiration}")
    private Long expiration;

    @Value("${security.token.secret}")
    private String secret;

    @Value("${security.token.prefix}")
    private String tokenPrefix;

    @Value("${security.token.header}")
    private String tokenHeader;

    public UserAuthenticationFilter(AuthenticationManager authenticationManager) {
        setFilterProcessesUrl("/login");
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            ServletInputStream json = request.getInputStream();
            User user = new ObjectMapper().readValue(json, User.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            user.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            logger.error(e.getMessage());
            logger.debug(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // If user's credentials are successful then this method is called by the filter
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication auth)
            throws IOException, ServletException {
        String token = generateToken(auth);

        if (token != null) {
            response.addHeader(tokenHeader, tokenPrefix + token);
        }

        logger.info(String.format("Successful authentication at %s", new UrlPathHelper().getPathWithinApplication(request)));
    }

    // If user's credentials fail then this method is called
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, String.format("Bad credentials: Authentication failed at %s",
                new UrlPathHelper().getPathWithinApplication(request)));

        logger.error(String.format("Bad credentials: Authentication failed at %s", new UrlPathHelper().getPathWithinApplication(request)));
    }

    // Generates JWT
    private String generateToken(Authentication authentication) {
        try {
            AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

            return Jwts.builder()
                    .setSubject(user.getUsername())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(SignatureAlgorithm.HS512, secret.getBytes("UTF-8"))
                    .compact();

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException();
        }
    }

}