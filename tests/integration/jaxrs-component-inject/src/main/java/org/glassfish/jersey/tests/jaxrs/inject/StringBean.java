/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.tests.jaxrs.inject;

/**
 * This is the object which standard implementation does not have a provider
 * for, even though its some simple String holder. It can also be used as mutable
 * string.
 */
public class StringBean {
    private String header;

    public StringBean(String header) {
        this.header = header;
    }

    public String get() {
        return header;
    }

    public void set(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return "StringBean. To get a value, use rather #get() method.";
    }
}
