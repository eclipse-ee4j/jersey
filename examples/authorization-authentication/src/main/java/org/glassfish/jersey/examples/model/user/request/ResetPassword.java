package org.glassfish.jersey.examples.model.user.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ResetPassword {

    @NotNull
    @Size(min = 35,max = 36)
    private String token;

    @NotNull
    @Size(min = 6, max = 50)
    private String password;

    public ResetPassword() {
    }

    public ResetPassword(String token, String password) {
        this.token = token;
        this.password = password;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public String toString() {
        return "ResetPassword{" +
                "token='" + token + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
