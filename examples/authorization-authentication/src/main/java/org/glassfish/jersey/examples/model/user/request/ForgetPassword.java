package org.glassfish.jersey.examples.model.user.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ForgetPassword {

    @NotNull
    @Size(min = 1, max = 50)
    private String username;


    public ForgetPassword() {
    }

    public ForgetPassword(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "ForgetPassword{" +
                "username='" + username + '\'' +
                '}';
    }
}
