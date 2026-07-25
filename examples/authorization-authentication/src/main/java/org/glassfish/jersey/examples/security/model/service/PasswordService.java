package org.glassfish.jersey.examples.security.model.service;

import org.glassfish.jersey.examples.security.model.LoginInfo;
import org.glassfish.jersey.examples.security.model.PasswordInfo;
import org.glassfish.jersey.examples.settings.SecuritySettings;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {


    public static PasswordInfo issuePassword(String password) {

        final String gensalt = BCrypt.gensalt();

        String hashed = BCrypt.hashpw(password, gensalt);

        return new PasswordInfo(hashed, gensalt, new LoginInfo(SecuritySettings.PASSWROD_ID, SecuritySettings.PASSWROD_ID, SecuritySettings.PASSWROD_ID));
    }
}
