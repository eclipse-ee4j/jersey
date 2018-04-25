/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos
 */
@SuppressWarnings("RedundantIfStatement")
@XmlRootElement(name = "animalList")
public class AnimalList {

    public List<Animal> animals;

    public static Object createTestInstance() {
        final AnimalList aList = new AnimalList();
        aList.animals = new LinkedList<>();
        aList.animals.add(new Dog("Fifi"));
        aList.animals.add(new Cat("Daisy"));
        return aList;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnimalList other = (AnimalList) obj;
        if (this.animals != other.animals && (this.animals == null || !this.animals.equals(other.animals))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.animals != null ? this.animals.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return (animals != null) ? animals.toString() : null;
    }

}
