package org.glassfish.jersey.examples.security.authentication;

import org.glassfish.jersey.examples.model.user.User;
import org.glassfish.jersey.examples.model.user.persistence.UserRepository;
import org.glassfish.jersey.examples.model.user.request.OAuthLogin;
import org.glassfish.jersey.examples.model.user.request.UserFacebookSocialLogin;
import org.glassfish.jersey.examples.security.authentication.exceptions.IssuerRequestPrivateResourceException;
import org.glassfish.jersey.examples.security.authentication.exceptions.UnmarshallingException;
import org.glassfish.jersey.examples.security.model.LoginInfo;
import org.glassfish.jersey.examples.security.model.OAuth2Info;
import org.glassfish.jersey.examples.security.model.Role;
import org.glassfish.jersey.examples.security.model.UserLoginInfo;
import org.glassfish.jersey.examples.security.model.persistence.OAuth2Repository;
import org.glassfish.jersey.examples.security.model.persistence.UserLoginRepository;
import org.glassfish.jersey.examples.security.model.response.Token;
import org.glassfish.jersey.examples.security.model.service.RoleService;
import org.glassfish.jersey.examples.security.model.service.TokenService;
import org.glassfish.jersey.examples.settings.OAuthSettings;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.List;
import java.util.Optional;

public class AccessTokenAuthenticator implements Authenticator<OAuthLogin> {

    private UserRepository userRepository;
    private OAuth2Repository auth2Repository;
    private UserLoginRepository userLoginRepository;
    private RoleService roleService;

    public AccessTokenAuthenticator(UserRepository userRepository,
                                    OAuth2Repository auth2Repository,
                                    UserLoginRepository userLoginRepository,
                                    RoleService roleService) {

        this.userRepository = userRepository;
        this.auth2Repository = auth2Repository;
        this.userLoginRepository = userLoginRepository;
        this.roleService = roleService;

    }

    /**
     * @param credentials OAuthLogin
     * @return
     * @throws UnmarshallingException                when provider response cannot be mapped UserFacebookSocialLogin
     * @throws RuntimeException                      I/O failure what we gone to do, retry request!?
     * @throws IssuerRequestPrivateResourceException when not a ok was received
     */
    @Override
    public Token authenticates(OAuthLogin credentials) {


        User user = null;
        UserLoginInfo theUserLoginInfo = null;
        UserFacebookSocialLogin socialLogin;

        final Client client = ClientBuilder.newClient();


        try {

            socialLogin = client.target(URI.create(OAuthSettings.PRIVATE_FACEBOOK_RESOURCE)).
                    queryParam(OAuthSettings.ACCESS_TOKEN_QUERY_PARAM, credentials.getAccessToken()).
                    queryParam(OAuthSettings.FACEBOOK_QUERY_SELECTOR, OAuthSettings.FACEBOOK_QUERY_FIELDS).
                    request(MediaType.APPLICATION_JSON).
                    buildGet().
                    invoke(UserFacebookSocialLogin.class);

        } catch (ResponseProcessingException e) {
            throw new UnmarshallingException();
        } catch (ProcessingException e) {
            throw new RuntimeException();
        } catch (WebApplicationException e) {
            //We have screw up with the request
            throw new IssuerRequestPrivateResourceException();
        }


        final Optional<User> userByUsername = userRepository.getUserByUsername(socialLogin.getEmail());

        user = userByUsername.orElseGet(() -> userRepository.save(new User(socialLogin.getEmail(), socialLogin.getFirstName(), socialLogin.getLastName())));

        final Optional<UserLoginInfo> userLoginInfo = userLoginRepository.findByUserId(user.getId());


        if (!userLoginInfo.isPresent()) {

            final LoginInfo loginInfo = new LoginInfo(OAuthSettings.FACEBOOK_PROVIDER_LABEL, OAuthSettings.FACEBOOK_CLIENT_ID, OAuthSettings.FACEBOOK_CLIENT_SECRETE);
            final OAuth2Info oAuth2Info = auth2Repository.save(new OAuth2Info(credentials.getAccessToken(), credentials.getExpiresIn(), credentials.getRefreshToken(), loginInfo));
            final OAuth2Info oAuth2Info1 = auth2Repository.save(oAuth2Info);
            final List<Role> defaultUserRoles = roleService.getDefaultUserRoles();
            theUserLoginInfo = userLoginRepository.save(new UserLoginInfo(user, oAuth2Info1.getLoginInfo(), defaultUserRoles));

        } else {
            theUserLoginInfo = userLoginInfo.get();

            final LoginInfo loginInfos = userLoginInfo.get().getLoginInfos();
            final Optional<OAuth2Info> oAuth2Info = auth2Repository.findByLoginId(loginInfos.getId());

            if (oAuth2Info.isPresent()) {
                oAuth2Info.get().setAccessToken(credentials.getAccessToken());
                auth2Repository.save(oAuth2Info.get());
            } else {
                auth2Repository.save(new OAuth2Info(credentials.getAccessToken(), credentials.getExpiresIn(), credentials.getRefreshToken(), loginInfos));
            }
        }

        return TokenService.issueToken(theUserLoginInfo);
    }
}
