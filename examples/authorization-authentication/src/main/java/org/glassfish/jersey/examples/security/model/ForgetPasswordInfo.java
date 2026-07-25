package org.glassfish.jersey.examples.security.model;

import org.glassfish.jersey.examples.settings.SecuritySettings;

import javax.persistence.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

@Entity
@NamedQuery(name = "ForgetPasswordInfo.findByToken", query = "SELECT OBJECT(u) FROM ForgetPasswordInfo u where u.token=:token")
public class ForgetPasswordInfo {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    private UserLoginInfo userLoginInfo;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresIn;

    public ForgetPasswordInfo() {
    }

    public ForgetPasswordInfo(String token, UserLoginInfo userLoginInfo) {
        this.token = token;
        this.userLoginInfo = userLoginInfo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserLoginInfo getUserLoginInfo() {
        return userLoginInfo;
    }

    public void setUserLoginInfo(UserLoginInfo userLoginInfo) {
        this.userLoginInfo = userLoginInfo;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Date expiresIn) {
        this.expiresIn = expiresIn;
    }

    @PrePersist
    protected void onCreate() {
        created = new Date(System.currentTimeMillis());
        expiresIn = new Date(created.toInstant().plus(SecuritySettings.FORGET_PASSWORD_TOKEN_VALIDTY, ChronoUnit.SECONDS).toEpochMilli());
    }

    public boolean isValid(){

        if(expiresIn == null){
            return false;
        }
        final Date now  = new Date(System.currentTimeMillis());
        return now.before(this.expiresIn);
    }

    public void invalidate(){
        this.expiresIn = new Date(System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForgetPasswordInfo)) return false;
        ForgetPasswordInfo that = (ForgetPasswordInfo) o;
        return Objects.equals(getToken(), that.getToken());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getToken());
    }

    @Override
    public String toString() {
        return "ForgetPasswordInfo{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", userLoginInfo=" + userLoginInfo +
                ", created=" + created +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
