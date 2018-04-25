/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookmark_em.entity;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity class UserEntity.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Entity
@Table(name = "USERS")
@NamedQueries({
        @NamedQuery(name = "UserEntity.findByUserid", query = "SELECT u FROM UserEntity u WHERE u.userid = :userid"),
        @NamedQuery(name = "UserEntity.findByPassword", query = "SELECT u FROM UserEntity u WHERE u.password = :password"),
        @NamedQuery(name = "UserEntity.findByUsername", query = "SELECT u FROM UserEntity u WHERE u.username = :username"),
        @NamedQuery(name = "UserEntity.findByEmail", query = "SELECT u FROM UserEntity u WHERE u.email = :email")
})
@SuppressWarnings("UnusedDeclaration")
public class UserEntity implements Serializable {

    @Id
    @Column(name = "USERID", nullable = false)
    private String userid;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "EMAIL")
    private String email;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userEntity")
    private Collection<BookmarkEntity> bookmarkEntityCollection;

    /**
     * Creates a new instance of UserEntity
     */
    public UserEntity() {
    }

    /**
     * Creates a new instance of UserEntity with the specified values.
     *
     * @param userid the userid of the UserEntity
     */
    public UserEntity(String userid) {
        this.userid = userid;
    }

    /**
     * Creates a new instance of UserEntity with the specified values.
     *
     * @param userid the userid of the UserEntity
     * @param password the password of the UserEntity
     */
    public UserEntity(String userid, String password) {
        this.userid = userid;
        this.password = password;
    }

    /**
     * Gets the userid of this UserEntity.
     *
     * @return the userid
     */
    public String getUserid() {
        return this.userid;
    }

    /**
     * Sets the userid of this UserEntity to the specified value.
     *
     * @param userid the new userid
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * Gets the password of this UserEntity.
     *
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the password of this UserEntity to the specified value.
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the username of this UserEntity.
     *
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the username of this UserEntity to the specified value.
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the email of this UserEntity.
     *
     * @return the email
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Sets the email of this UserEntity to the specified value.
     *
     * @param email the new email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the bookmarkEntityCollection of this UserEntity.
     *
     * @return the bookmarkEntityCollection
     */
    public Collection<BookmarkEntity> getBookmarkEntityCollection() {
        return this.bookmarkEntityCollection;
    }

    /**
     * Sets the bookmarkEntityCollection of this UserEntity to the specified value.
     *
     * @param bookmarkEntityCollection the new bookmarkEntityCollection
     */
    public void setBookmarkEntityCollection(Collection<BookmarkEntity> bookmarkEntityCollection) {
        this.bookmarkEntityCollection = bookmarkEntityCollection;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes
     * a hash code value based on the id fields in this object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.userid != null ? this.userid.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this UserEntity.  The result is
     * <code>true</code> if and only if the argument is not null and is a UserEntity object that
     * has the same id field values as this object.
     *
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     *         <code>false</code> otherwise.
     */
    @SuppressWarnings("StringEquality")
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserEntity)) {
            return false;
        }
        UserEntity other = (UserEntity) object;
        return !(this.userid != other.userid && (this.userid == null || !this.userid.equals(other.userid)));
    }

    /**
     * Returns a string representation of the object.  This implementation constructs
     * that representation based on the id fields.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "UserEntity{"
               + "userid='" + userid + '\''
               + '}';
    }
}
