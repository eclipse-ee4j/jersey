package org.glassfish.jersey.examples.controller;

import org.glassfish.jersey.examples.model.user.request.ForgetPassword;
import org.glassfish.jersey.examples.model.user.request.ResetPassword;
import org.glassfish.jersey.examples.security.authentication.exceptions.ResetPasswordNotSupported;
import org.glassfish.jersey.examples.security.authentication.exceptions.UserNotExistsException;
import org.glassfish.jersey.examples.security.model.ForgetPasswordInfo;
import org.glassfish.jersey.examples.security.model.PasswordInfo;
import org.glassfish.jersey.examples.security.model.UserLoginInfo;
import org.glassfish.jersey.examples.security.model.persistence.ForgetPasswordRepository;
import org.glassfish.jersey.examples.security.model.persistence.PasswordRepository;
import org.glassfish.jersey.examples.security.model.persistence.UserLoginRepository;
import org.glassfish.jersey.examples.security.model.persistence.jpa.JpaForgetPasswordRepository;
import org.glassfish.jersey.examples.security.model.persistence.jpa.JpaPasswordRepository;
import org.glassfish.jersey.examples.security.model.persistence.jpa.JpaUserLoginRepository;
import org.glassfish.jersey.examples.security.model.response.ForgetPasswordCode;
import org.glassfish.jersey.examples.security.model.service.PasswordService;

import javax.ws.rs.NotSupportedException;
import java.util.Optional;
import java.util.UUID;

public class ForgetPasswordController {

    private ForgetPasswordRepository forgetPasswordRepository;
    private UserLoginRepository loginRepository;
    private PasswordRepository passwordRepository;

    public ForgetPasswordController() {
        this.forgetPasswordRepository = new JpaForgetPasswordRepository();
        this.loginRepository = new JpaUserLoginRepository();
        this.passwordRepository = new JpaPasswordRepository();
    }

    /**
     *
     * @param forgetPassword
     * @throws UserNotExistsException
     * @throws ResetPasswordNotSupported
     * @throws RuntimeException
     * @return code
     */
    public ForgetPasswordCode forgetPassword(ForgetPassword forgetPassword){

        final Optional<UserLoginInfo> optUserLoginInfo = loginRepository.findUserByUsername(forgetPassword.getUsername());

        if(optUserLoginInfo.isPresent()){

            final UserLoginInfo userLoginInfo = optUserLoginInfo.get();

            if(!userLoginInfo.getLoginInfos().hasResetPasswordCapability()){
                throw new  ResetPasswordNotSupported();
            }

            final ForgetPasswordInfo toSaveFgtPssw = new ForgetPasswordInfo(UUID.randomUUID().toString(),userLoginInfo);

            if(forgetPasswordRepository.add(toSaveFgtPssw)){
              return new ForgetPasswordCode(toSaveFgtPssw.getToken());
            }
            throw new RuntimeException("Our error, must always be able to save");
        }
        throw new UserNotExistsException();
    }



    public boolean resetPassword(final ResetPassword resetPassword){

        final Optional<ForgetPasswordInfo> token = forgetPasswordRepository.findByToken(resetPassword.getToken());

        if(!token.isPresent()){
            throw new IllegalArgumentException();
        }

        final ForgetPasswordInfo forgetPasswordInfo = token.get();

        if(!forgetPasswordInfo.isValid()){
            return false;
        }

        final UserLoginInfo userLoginInfo = forgetPasswordInfo.getUserLoginInfo();

        final Optional<PasswordInfo> passwordInfo = passwordRepository.findByLoginId(userLoginInfo.getLoginInfos().getId());

        if(!passwordInfo.isPresent()){
            throw new RuntimeException("Our error, must always be able to have the password since the user request reset");
        }

        final PasswordInfo info = passwordInfo.get();

        final PasswordInfo password = PasswordService.issuePassword(resetPassword.getPassword());

        info.setHash(password.getHash());
        info.setSalt(password.getSalt());

        forgetPasswordInfo.invalidate();

        passwordRepository.save(info);
        forgetPasswordRepository.save(forgetPasswordInfo);

        return true;
    }
}
