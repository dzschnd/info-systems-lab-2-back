package org.lab.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.github.cdimascio.dotenv.Dotenv;
import org.lab.model.Role;
import org.lab.model.User;

import java.util.Date;

public class JwtUtils {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String SECRET_KEY = dotenv.get("SECRET_KEY");
    private static final long EXPIRATION_TIME = 1000*60*60 * 24;

    public static String generateToken(User user) {
        assert SECRET_KEY != null;
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withSubject(user.getId().toString())
                .withClaim("username", user.getUsername())
                .withClaim("email", user.getEmail())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public static boolean validateToken(String token, User user) {
        try {
            assert SECRET_KEY != null;
            System.out.println(SECRET_KEY);
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return Integer.valueOf(jwt.getSubject()) == (user.getId());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public static User extractUser(String token) {
        try {
            assert SECRET_KEY != null;
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);

            User user = new User();
            user.setId(Integer.valueOf(jwt.getSubject()));
            user.setUsername(jwt.getClaim("username").asString());
            user.setEmail(jwt.getClaim("email").asString());
            user.setRole(Role.valueOf(jwt.getClaim("role").asString()));

            return user;
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
