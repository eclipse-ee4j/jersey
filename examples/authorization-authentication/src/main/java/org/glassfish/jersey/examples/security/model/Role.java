package org.glassfish.jersey.examples.security.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

@Entity
@NamedQuery(name = "Role.findByName", query = "SELECT OBJECT(u) FROM Role u where u.name=:name")
public class Role implements Comparable<Role> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 50)
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    public Role() {
    }

    public Role(@NotNull String name) {
        this.name = name.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @PrePersist
    protected void onCreate() {
        created = new Date(System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return Objects.equals(getName(), role.getName());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", created=" + created +
                '}';
    }

    @Override
    public int compareTo(Role o) {
        return this.getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }
}
