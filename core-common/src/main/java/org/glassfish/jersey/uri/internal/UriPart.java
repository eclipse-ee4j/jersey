/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.uri.internal;

import org.glassfish.jersey.uri.UriComponent;

/**
 * <p>
 *  This class represents a part of the uri as parsed by the UriTemplateParser.
 * </p>
 * <p>
 *  The UriTemplate parser can produce multiple UriParts, each representing a part of the Uri. One part can represent either
 *  a static uri part without a template or a template with a single variable. The template with multiple variables generates
 *  multiple UriParts, each for a single variable.
 * </p>
 */
public class UriPart {
    private final String part;

    UriPart(String part) {
        this.part = part;
    }

    /**
     * Return the string value representing this UriPart. It can either be static content or a template.
     * @return string value representing this UriPart
     */
    public String getPart() {
        return part;
    }

    /**
     * Return the matching group of the template represented by this {@link UriPart}
     * @return the matching group
     */
    public String getGroup() {
        return part;
    }

    /**
     * Returns true when this {@link UriPart} is a template with a variable
     * @return true when a template
     */
    public boolean isTemplate() {
        return false;
    }

    /**
     * Returns the resolved template variable when the value object is passed
     * @param value the value object to be used to resolve the template variable
     * @param componentType the component type that can be used to determine the encoding os special characters
     * @param encode the hint whether to encode or not
     * @return the resolved template
     */
    public String resolve(Object value, UriComponent.Type componentType, boolean encode) {
        return part;
    }

    /**
     * Informs whether throw {@link IllegalArgumentException} when no object value matches the template argument
     * @return {@code true} when when no object value matches the template argument and
     * {@link IllegalArgumentException} is to be thrown
     */
    public boolean throwWhenNoTemplateArg() {
        return false;
    }

    /**
     * Percent encode the given text
     * @param toEncode the given text to encode
     * @param componentType the component type to encode
     * @param encode toEncode or contextualEncode
     * @return the encoded text
     */
    public static String percentEncode(String toEncode, UriComponent.Type componentType, boolean encode) {
        if (encode) {
            toEncode = UriComponent.encode(toEncode, componentType);
        } else {
            toEncode = UriComponent.contextualEncode(toEncode, componentType);
        }
        return toEncode;
    }
}
