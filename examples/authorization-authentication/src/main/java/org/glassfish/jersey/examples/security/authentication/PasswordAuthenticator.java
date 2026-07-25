package org.glassfish.jersey.examples.security.authentication;

import org.glassfish.jersey.examples.model.user.User;
import org.glassfish.jersey.examples.model.user.persistence.UserRepository;
import org.glassfish.jersey.examples.model.user.request.Login;
import org.glassfish.jersey.examples.security.authentication.exceptions.InvalidCredentialsException;
import org.glassfish.jersey.examples.security.authentication.exceptions.UserCredentialsRecordException;
import org.glassfish.jersey.examples.security.authentication.exceptions.UserNotExistsException;
import org.glassfish.jersey.examples.security.model.LoginInfo;
import org.glassfish.jersey.examples.security.model.PasswordInfo;
import org.glassfish.jersey.examples.security.model.UserLoginInfo;
import org.glassfish.jersey.examples.security.model.persistence.PasswordRepository;
import org.glassfish.jersey.examples.security.model.persistence.UserLoginRepository;
import org.glassfish.jersey.examples.security.model.response.Token;
import org.glassfish.jersey.examples.security.model.service.TokenService;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class PasswordAuthenticator implements  Authenticator<Login> {

    private UserRepository userRepository;
    private UserLoginRepository loginRepository;
    private PasswordRepository passwordRepository;

    public PasswordAuthenticator(UserRepository userRepository,
                                 UserLoginRepository loginRepository,
                                 PasswordRepository passwordRepository) {

        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.passwordRepository = passwordRepository;
    }

    /**
     *
     * @param login
     * @throws UserNotExistsException
     * @return
     */
    @Override
    public Token authenticates(Login login) {

        if(login.getUsername() == null || login.getUsername().isEmpty()){
            throw new IllegalArgumentException();
        }

        final Optional<User> user = userRepository.getUserByUsername(login.getUsername());

        if(!user.isPresent()){
            throw new UserNotExistsException();
        }

        final User userFromDb = user.get();

        final Optional<UserLoginInfo> userLoginInfo = loginRepository.findByUserId(userFromDb.getId());

        if(!userLoginInfo.isPresent()){
            throw new UserCredentialsRecordException();
        }

        final UserLoginInfo loginInfo = userLoginInfo.get();

        final LoginInfo infos = loginInfo.getLoginInfos();

        final Optional<PasswordInfo> passwordInfo = passwordRepository.findByLoginId(infos.getId());

        if(!passwordInfo.isPresent()){
            throw new UserCredentialsRecordException();
        }

        if(!BCrypt.checkpw(login.getPassword(),passwordInfo.get().getHash())){
            throw new InvalidCredentialsException();
        }

        return TokenService.issueToken(userLoginInfo.get());
    }
}
