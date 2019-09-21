/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.multipart.webapp;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Bean {

    public String value;

    public Bean() {
    }

    public Bean(String str) {
        value = str;
    }

}
