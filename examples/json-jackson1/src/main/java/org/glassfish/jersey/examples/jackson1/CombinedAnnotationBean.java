/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jackson1;

import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Jakub Podlesak
 */
@XmlRootElement(name = "account")
public class CombinedAnnotationBean {

    @JsonProperty("value")
    int x;

    public CombinedAnnotationBean(final int x) {
        this.x = x;
    }

    public CombinedAnnotationBean() {
        this(15);
    }
}
