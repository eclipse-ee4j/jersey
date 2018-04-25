/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.tools;

/**
 * Implementation of {@link org.glassfish.jersey.tests.performance.tools.TestValueGenerator} producing constant results.
 *
 * Due to its constant nature, this strategy is not suitable for use with {@link java.util.Set}.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class ConstantTestValueGenerator extends TestValueGenerator {
    private static final int intConstant = 123456789;
    private static final int charConstant = 'x';
    private static final String stringConstant = "Hello, world!";
    private static final long longConstant = 987654321L;
    private static final float floatConstant = 3.1415f;
    private static final double doubleConstant = 3.1415926535;
    private static final byte byteConstant = (byte) 127;
    private static final short shortConstant = (short) 1024;
    private static final boolean booleanConstant = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt() {
        return intConstant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char getChar() {
        return charConstant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString() {
        return stringConstant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLong() {
        return longConstant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFloat() {
        return floatConstant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getDouble() {
        return doubleConstant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getByte() {
        return byteConstant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort() {
        return shortConstant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBoolean() {
        return booleanConstant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getEnum(Class<T> enumType) {
        T[] values = enumType.getEnumConstants();
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }
}
