/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.oauth.twitterclient;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Entity into which the Twitter status (tweet) is deserialized
 *
 *  @author Martin Matula
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {

    @XmlElement(name = "created_at")
    private String createdAt;
    @XmlElement(name = "text")
    private String text;
    @XmlElement(name = "user")
    private User user;

    public String getCreatedAt() {
        return createdAt;
    }

    public String getText() {
        return text;
    }

    public User getUser() {
        return user;
    }
}
