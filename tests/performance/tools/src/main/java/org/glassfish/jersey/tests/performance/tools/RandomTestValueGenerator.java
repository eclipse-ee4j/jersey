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

import java.util.Random;

/**
 * Implementation of {@link org.glassfish.jersey.tests.performance.tools.TestValueGenerator} producing random results.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class RandomTestValueGenerator extends TestValueGenerator {
    private static final int MAX_STRING_LENGTH = 50;

    private static final Random random = new Random();
    private static final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890 _";

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getEnum(Class<T> enumType) {
        T[] enumValues = enumType.getEnumConstants();
        return enumValues[random.nextInt(enumValues.length)];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt() {
        return random.nextInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char getChar() {
        return (char) random.nextInt(Character.MAX_VALUE + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString() {
        return randomString(random.nextInt(MAX_STRING_LENGTH));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLong() {
        return random.nextLong();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFloat() {
        return random.nextFloat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getDouble() {
        return random.nextDouble();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getByte() {
        return (byte) random.nextInt(Byte.MAX_VALUE + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort() {
        return (short) random.nextInt(Short.MAX_VALUE + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBoolean() {
        return random.nextBoolean();
    }

    private String randomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

}
