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
 * Entity into which the Twitter user is deserialized.
 *
 * @author Martin Matula
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @XmlElement(name = "name")
    private String name;

    public String getName() {
        return name;
    }
}
