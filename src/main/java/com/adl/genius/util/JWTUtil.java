package com.adl.genius.util;

import com.adl.genius.entity.UserContext;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JWTUtil {
    private static final String SECRET_KEY = "gpt_genius";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);

    private static final String CLAIM_ID = "id";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_TIME = "time";

    public static String getToken(UserContext userContext) {
        return JWT.create()
                .withClaim(CLAIM_ID, userContext.getId())
                .withClaim(CLAIM_USERNAME, userContext.getUsername())
                .withClaim(CLAIM_TIME, System.currentTimeMillis())
                .sign(ALGORITHM);
    }

    public static UserContext parseToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(ALGORITHM).build().verify(token);
            return new UserContext(
                    decodedJWT.getClaim(CLAIM_ID).asInt(),
                    decodedJWT.getClaim(CLAIM_USERNAME).asString()
            );
        } catch (RuntimeException e) {
            throw new RuntimeException("Invalid token.", e);
        }
    }
}
