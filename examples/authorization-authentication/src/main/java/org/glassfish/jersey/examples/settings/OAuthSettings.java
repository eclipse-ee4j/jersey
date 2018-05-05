package org.glassfish.jersey.examples.settings;

public class OAuthSettings {

    public static final String FACEBOOK_PROVIDER_LABEL = "facebook";

    public static final String FACEBOOK_QUERY_SELECTOR = "fields";
    public static final String FACEBOOK_QUERY_FIELDS = "name,email,last_name,first_name";
    public static final String FACEBOOK_EMAIL_SCOPE = "email";

    public static final String ACCESS_TOKEN_QUERY_PARAM = "access_token";
    public static final String PRIVATE_FACEBOOK_RESOURCE = "https://graph.facebook.com/me";

    public static final String AUTHORIZATION_CALLBACK_URI = "http://localhost:8080/myapp/login/authorize";

    public static final String FACEBOOK_CLIENT_ID = "my_app_client_id";
    public static final String FACEBOOK_CLIENT_SECRETE = "my_app_client_secrete";

}
