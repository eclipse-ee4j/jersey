package org.glassfish.jersey.examples.model.user.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserRegister {

    @NotNull
    @XmlElement
    @Size(min = 1,max = 50)
    private String firstname;

    @NotNull
    @XmlElement
    @Size(min = 1, max = 50)
    private String username;

    @NotNull
    @XmlElement
    @Size(min = 1, max = 50)
    private String lastname;

    @NotNull
    @XmlElement
    @Size(min = 6, max = 50)
    private String password;


    public UserRegister() {
    }

    public UserRegister(String firstname,
                        String username,
                        String lastname,
                        String password) {

        this.firstname = firstname;
        this.username = username;
        this.lastname = lastname;
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public String toString() {
        return "UserRegister{" +
                "firstname='" + firstname + '\'' +
                ", username='" + username + '\'' +
                ", lastname='" + lastname + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
