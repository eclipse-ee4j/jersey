package org.glassfish.jersey.examples.security.model;

import org.glassfish.jersey.examples.model.user.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(name = "UserLoginInfo.findByUserId", query = "SELECT OBJECT(u) FROM UserLoginInfo u where u.user.id=:id"),
        @NamedQuery(name = "UserLoginInfo.findUserByUsername", query = "SELECT OBJECT(u) FROM UserLoginInfo u where u.user.username=:username")
})
public class UserLoginInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private User user;

    @OneToOne
    private LoginInfo loginInfos;

    @OneToMany
    private List<Role> roles;

    public UserLoginInfo() {
    }

    public UserLoginInfo(User user, LoginInfo loginInfos) {
        this.user = user;
        this.loginInfos = loginInfos;
        this.roles = new ArrayList<>();
    }

    public UserLoginInfo(User user, LoginInfo loginInfos,List<Role> roles) {
        this.user = user;
        this.loginInfos = loginInfos;
        this.roles = roles;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LoginInfo getLoginInfos() {
        return loginInfos;
    }

    public void setLoginInfos(LoginInfo loginInfos) {
        this.loginInfos = loginInfos;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String[] roles(){
        String [] roles = new String[this.getRoles().size()];

        for (int i = 0; i < getRoles().size(); i++) {
            roles[i] = getRoles().get(i).getName();
        }

        return roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserLoginInfo)) return false;
        UserLoginInfo that = (UserLoginInfo) o;
        return Objects.equals(getUser(), that.getUser());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getUser());
    }
}
