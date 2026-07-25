package org.glassfish.jersey.examples.model.user.request;

import org.glassfish.jersey.examples.settings.OAuthSettings;
import org.glassfish.jersey.client.oauth2.ClientIdentifier;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import org.glassfish.jersey.client.oauth2.OAuth2CodeGrantFlow;


public class OAuthLogin {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;

    /**
     * Contains null or actually authorization flow.
     */
    private static OAuth2CodeGrantFlow flow;
    private static ClientIdentifier clientIdentifier;

    public OAuthLogin() {
        if (clientIdentifier == null) {
            clientIdentifier = new ClientIdentifier(OAuthSettings.FACEBOOK_CLIENT_ID, OAuthSettings.FACEBOOK_CLIENT_SECRETE);
            flow = OAuth2ClientSupport.
                    facebookFlowBuilder(clientIdentifier, OAuthSettings.AUTHORIZATION_CALLBACK_URI).
                    scope(OAuthSettings.FACEBOOK_EMAIL_SCOPE).
                    build();
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public OAuth2CodeGrantFlow getFlow() {
        return flow;
    }

    public void setFlow(OAuth2CodeGrantFlow flow) {
        this.flow = flow;
    }

    public ClientIdentifier getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(ClientIdentifier clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
