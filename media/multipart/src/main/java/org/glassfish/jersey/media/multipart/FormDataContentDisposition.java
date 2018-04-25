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

package org.glassfish.jersey.media.multipart;

import java.text.ParseException;
import java.util.Date;

import org.glassfish.jersey.message.internal.HttpHeaderReader;

/**
 * A form-data content disposition header.
 *
 * @author Paul Sandoz
 * @author imran@smartitengineering.com
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class FormDataContentDisposition extends ContentDisposition {

    private final String name;

    /**
     * Constructor for the builder.
     *
     * @param type the disposition type. will be "form-data".
     * @param name the control name.
     * @param fileName the file name.
     * @param creationDate the creation date.
     * @param modificationDate the modification date.
     * @param readDate the read date.
     * @param size the size.
     * @throws IllegalArgumentException if the type is not equal to "form-data"
     *         or the name is {@code null}
     */
    protected FormDataContentDisposition(String type, String name, String fileName,
            Date creationDate, Date modificationDate, Date readDate,
            long size) {
        super(type, fileName, creationDate, modificationDate, readDate, size);
        this.name = name;

        if (!"form-data".equalsIgnoreCase(getType())) {
            throw new IllegalArgumentException("The content disposition type is not equal to form-data");
        }

        if (name == null) {
            throw new IllegalArgumentException("The name parameter is not present");
        }
    }

    public FormDataContentDisposition(String header) throws ParseException {
        this(header, false);
    }

    public FormDataContentDisposition(String header, boolean fileNameFix) throws ParseException {
        this(HttpHeaderReader.newInstance(header), fileNameFix);
    }

    public FormDataContentDisposition(HttpHeaderReader reader, boolean fileNameFix) throws ParseException {
        super(reader, fileNameFix);
        if (!"form-data".equalsIgnoreCase(getType())) {
            throw new IllegalArgumentException("The content disposition type is not equal to form-data");
        }

        name = getParameters().get("name");
        if (name == null) {
            throw new IllegalArgumentException("The name parameter is not present");
        }
    }

    /**
     * Get the name parameter.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    protected StringBuilder toStringBuffer() {
        StringBuilder sb = super.toStringBuffer();

        addStringParameter(sb, "name", name);

        return sb;
    }

    /**
     * Start building a form data content disposition.
     *
     * @param name the control name.
     * @return the form data content disposition builder.
     */
    public static FormDataContentDispositionBuilder name(String name) {
        return new FormDataContentDispositionBuilder(name);
    }

    /**
     * Builder to build form data content disposition.
     *
     */
    public static class FormDataContentDispositionBuilder
            extends ContentDispositionBuilder<FormDataContentDispositionBuilder, FormDataContentDisposition> {

        private final String name;

        FormDataContentDispositionBuilder(String name) {
            super("form-data");
            this.name = name;
        }

        @Override
        public FormDataContentDisposition build() {
            return new FormDataContentDisposition(type, name, fileName, creationDate, modificationDate, readDate, size);
        }
    }
}
