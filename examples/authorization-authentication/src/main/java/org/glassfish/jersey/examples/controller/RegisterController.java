package org.glassfish.jersey.examples.controller;

import org.glassfish.jersey.examples.model.user.User;
import org.glassfish.jersey.examples.model.user.persistence.UserRepository;
import org.glassfish.jersey.examples.model.user.persistence.jpa.JpaUserRepository;
import org.glassfish.jersey.examples.model.user.request.UserRegister;
import org.glassfish.jersey.examples.security.model.PasswordInfo;
import org.glassfish.jersey.examples.security.model.Role;
import org.glassfish.jersey.examples.security.model.UserLoginInfo;
import org.glassfish.jersey.examples.security.model.persistence.PasswordRepository;
import org.glassfish.jersey.examples.security.model.persistence.UserLoginRepository;
import org.glassfish.jersey.examples.security.model.persistence.jpa.JpaPasswordRepository;
import org.glassfish.jersey.examples.security.model.persistence.jpa.JpaRoleRepository;
import org.glassfish.jersey.examples.security.model.persistence.jpa.JpaUserLoginRepository;
import org.glassfish.jersey.examples.security.model.service.PasswordService;
import org.glassfish.jersey.examples.security.model.service.RoleService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterController {

    private final static Logger logger = Logger.getLogger(RegisterController.class.getSimpleName());
    private UserRepository repository;
    private RoleService roleService;
    private PasswordRepository passwordRepository;
    private UserLoginRepository loginRepository;

    public RegisterController() {
        repository = new JpaUserRepository();
        roleService = new RoleService(new JpaRoleRepository());
        passwordRepository = new JpaPasswordRepository();
        loginRepository = new JpaUserLoginRepository();
    }

    public boolean registerNewUser(UserRegister register){

        try {
            final List<Role> userRoles = roleService.getDefaultUserRoles();

            final User user = repository.save(new User(register.getUsername(), register.getFirstname(),register.getLastname()));

            final PasswordInfo passwordInfo = PasswordService.issuePassword(register.getPassword());

            final PasswordInfo savedPassowrd = passwordRepository.save(passwordInfo);

            final UserLoginInfo userLoginInfo = new UserLoginInfo(user, savedPassowrd.getLoginInfo(), userRoles);

            return loginRepository.add(userLoginInfo);

        } catch (Exception exception){
            logger.log(Level.SEVERE,"Duplicate user not allowed", exception);
        }

        return false;
    }

}
