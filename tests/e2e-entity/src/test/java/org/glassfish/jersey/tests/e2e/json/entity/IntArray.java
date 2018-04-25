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

import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@SuppressWarnings("RedundantIfStatement")
@XmlRootElement(name = "intArray")
public class IntArray {

    public int[] intArray;

    public Integer[] integerArray;

    public int number;

    public IntArray() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IntArray other = (IntArray) obj;
        if (!Arrays.equals(this.intArray, other.intArray)) {
            return false;
        }
        if (!Arrays.deepEquals(this.integerArray, other.integerArray)) {
            return false;
        }
        if (this.number != other.number) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Arrays.hashCode(this.intArray);
        hash = 89 * hash + Arrays.deepHashCode(this.integerArray);
        hash = 89 * hash + this.number;
        return hash;
    }

    @Override
    public String toString() {
        return String.format("{ \"intArray\":%s, \"integerArray\":%s, \"number\":%d}", Arrays.toString(intArray),
                Arrays.toString(integerArray), number);
    }

    public static Object createTestInstance() {
        IntArray result = new IntArray();

        result.number = 8;
        result.intArray = new int[]{4};
        result.integerArray = new Integer[]{3};

        return result;
    }

}
