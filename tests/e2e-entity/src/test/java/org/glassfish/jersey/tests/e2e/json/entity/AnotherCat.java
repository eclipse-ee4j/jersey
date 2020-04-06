/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.json.entity;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author Jakub Podlesak
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement(name = "cat")
public class AnotherCat extends AnotherAnimal {

    private String nickName;

    public AnotherCat() {
    }

    public AnotherCat(String name) {
        super(name);
    }

    public AnotherCat(String name, String nickName) {
        super(name);
        this.nickName = nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return this.nickName;
    }

    @Override
    public String toString() {
        return String.format("{ \"cat\" : %s , %s}", super.toString(), this.nickName);
    }
}
