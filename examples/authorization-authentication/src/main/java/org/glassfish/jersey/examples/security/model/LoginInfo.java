package org.glassfish.jersey.examples.security.model;

import org.glassfish.jersey.examples.settings.SecuritySettings;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@NamedQuery(name = "LoginInfo.findByProviderId", query = "SELECT OBJECT(u) FROM LoginInfo u where u.providerId=:providerId")
public class LoginInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    private String providerKey;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;


    public LoginInfo() {
    }

    public LoginInfo(String label,String providerId, String providerKey) {
        this.label = label;
        this.providerId = providerId;
        this.providerKey = providerKey;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean hasResetPasswordCapability(){
        return this.getLabel().equals(SecuritySettings.PASSWROD_ID);
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
        if (!(o instanceof LoginInfo)) return false;
        LoginInfo loginInfo = (LoginInfo) o;
        return Objects.equals(getProviderId(), loginInfo.getProviderId()) &&
                Objects.equals(getProviderKey(), loginInfo.getProviderKey());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getProviderId(), getProviderKey());
    }

    @Override
    public String toString() {
        return "LoginInfo{" +
                "id=" + id +
                ", providerId='" + providerId + '\'' +
                ", providerKey='" + providerKey + '\'' +
                '}';
    }
}
