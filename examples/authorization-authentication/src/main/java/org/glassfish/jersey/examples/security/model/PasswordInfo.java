package org.glassfish.jersey.examples.security.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

@Entity
@NamedQuery(name = "PasswordInfo.findByLoginId", query = "SELECT OBJECT(u) FROM PasswordInfo u where u.loginInfo.id=:id")
public class PasswordInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String hash;

    @Column(nullable = false)
    private String salt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private LoginInfo loginInfo;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    public PasswordInfo() {
    }

    public PasswordInfo(@NotNull String hash,@NotNull String salt, LoginInfo loginInfo) {
        this.hash = hash;
        this.salt = salt;
        this.loginInfo = loginInfo;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public LoginInfo getLoginInfo() {
        return loginInfo;
    }

    public void setLoginInfo(LoginInfo loginInfo) {
        this.loginInfo = loginInfo;
    }

    @PrePersist
    protected void onCreate() {
        updated = created = new Date(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        updated = new Date(System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PasswordInfo)) return false;
        PasswordInfo that = (PasswordInfo) o;
        return Objects.equals(getHash(), that.getHash()) &&
                Objects.equals(getSalt(), that.getSalt());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getHash(), getSalt());
    }

    @Override
    public String toString() {
        return "PasswordInfo{" +
                "id=" + id +
                ", hash='" + hash + '\'' +
                ", salt='" + salt + '\'' +
                ", loginInfo=" + loginInfo +
                ", created=" + created +
                ", updated=" + updated +
                '}';
    }
}
