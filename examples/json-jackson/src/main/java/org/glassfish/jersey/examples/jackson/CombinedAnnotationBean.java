/*
 * Copyright (c) 2010, 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jackson;

import jakarta.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TODO javadoc.
 *
 * @author Jakub Podlesak
 */
// Jackson works with javax API, do not change it to jakarta API.
@XmlRootElement(name = "account")
public class CombinedAnnotationBean {

    @JsonProperty("value")
    int x;

    public CombinedAnnotationBean(int x) {
        this.x = x;
    }

    public CombinedAnnotationBean() {
        this(15);
    }
}
