package org.glassfish.jersey.examples.security.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

@Entity
@NamedQuery(name = "OAuth2Info.findByLoginId", query = "SELECT OBJECT(u) FROM OAuth2Info u where u.loginInfo.id=:id")
public class OAuth2Info {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private Long expiresIn;

    @Column(nullable = true)
    private String refreshToken;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private LoginInfo loginInfo;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;


    public OAuth2Info() {
    }

    public OAuth2Info(@NotNull String accessToken, Long expiresIn, String refreshToken,@NotNull LoginInfo loginInfo) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
        this.loginInfo = loginInfo;
    }

    public Long getId() {
        return id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
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
        if (!(o instanceof OAuth2Info)) return false;
        OAuth2Info that = (OAuth2Info) o;
        return Objects.equals(getAccessToken(), that.getAccessToken()) &&
                Objects.equals(getExpiresIn(), that.getExpiresIn());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getAccessToken(), getExpiresIn());
    }
}