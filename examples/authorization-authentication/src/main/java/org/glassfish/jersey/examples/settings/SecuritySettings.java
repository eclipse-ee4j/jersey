package org.glassfish.jersey.examples.settings;

public class SecuritySettings {

    public static final String PASSWROD_ID = "password";

    public static final String JWT_ISSUER = "change-me";
    public static final String JWT_SECRETE = "secrete";
    public static final Long JWT_TOKEN_VALIDTY = 1L; //Days
    public static final String JWT_USER_CLAIM = "username";
    public static final String JWT_ROLES_CLAIM = "roles";

    public static final String UNKNOWN_USER = "Guest";

    public static final String AUTHORIZATION_HEADER = "authorization";

    public static final String AUTHORIZATION_SCHEME = "Bearer";


    public static final long FORGET_PASSWORD_TOKEN_VALIDTY = 36000L;


}
