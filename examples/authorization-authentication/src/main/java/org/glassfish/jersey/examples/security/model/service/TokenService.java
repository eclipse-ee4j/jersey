package org.glassfish.jersey.examples.security.model.service;

import org.glassfish.jersey.examples.security.model.UserLoginInfo;
import org.glassfish.jersey.examples.security.model.response.Token;
import org.glassfish.jersey.examples.settings.SecuritySettings;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenService {

    private final static Logger logger = Logger.getLogger(TokenService.class.getSimpleName());

    public static Token issueToken(UserLoginInfo userLoginInfo){

        final ZonedDateTime utc = ZonedDateTime.now(ZoneId.of("UTC"));
        final ZonedDateTime utcPlus1day = utc.plusDays(SecuritySettings.JWT_TOKEN_VALIDTY);

        try {

            final String token = Jwts.builder()
                    .setSubject(userLoginInfo.getUser().getUsername())
                    .claim(SecuritySettings.JWT_ROLES_CLAIM, userLoginInfo.roles())
                    .claim(SecuritySettings.JWT_USER_CLAIM, userLoginInfo.getUser().getUsername())
                    .setSubject(userLoginInfo.getUser().getUsername())
                    .setIssuedAt(Date.from(utc.toInstant()))
                    .setExpiration(Date.from(utcPlus1day.toInstant()))
                    .setIssuer(SecuritySettings.JWT_ISSUER)
                    .signWith(SignatureAlgorithm.HS512, SecuritySettings.JWT_SECRETE)
                    .compact();

            return new Token(token,utcPlus1day.toInstant().toEpochMilli(),UUID.randomUUID().toString());

        } catch (Throwable e){
            logger.log(Level.SEVERE,"WTF goes wrong",e);
        }

        return new Token(UUID.randomUUID().toString(),utcPlus1day.toInstant().toEpochMilli(),UUID.randomUUID().toString());
    }
}
