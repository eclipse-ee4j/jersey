/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.json.entity.pojo;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos
 */
@SuppressWarnings("UnusedDeclaration")
public class PojoCat extends PojoAnimal {

    private String nickName;

    public PojoCat() {
    }

    public PojoCat(final String name) {
        super(name);
    }

    public PojoCat(final String name, final String nickName) {
        super(name);
        this.nickName = nickName;
    }

    public void setNickName(final String nickName) {
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
