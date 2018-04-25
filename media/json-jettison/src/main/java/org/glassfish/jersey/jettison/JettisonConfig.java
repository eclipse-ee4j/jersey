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

package org.glassfish.jersey.jettison;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An immutable configuration of JSON notation and options. {@code JettisonConfig}
 * instance can be used for configuring the JSON notation on {@link JettisonJaxbContext}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class JettisonConfig {

    /**
     * Enumeration of supported JSON notations.
     */
    public enum Notation {
        /**
         * The mapped Jettison JSON notation.
         * <p>Example JSON expression:<pre>
         * {"userTable":{"columns":[{"id":"userid","label":"UserID"},{"id":"name","label":"User Name"}],"rows":{"userid":1621,"name":"Grotefend"}}}
         * </pre>
         */
        MAPPED_JETTISON,
        /**
         * The mapped Badgerfish JSON notation.
         * <p>Example JSON expression:<pre>
         * {"userTable":{"columns":[{"id":{"$":"userid"},"label":{"$":"UserID"}},{"id":{"$":"name"},"label":{"$":"User Name"}}],"rows":{"userid":{"$":"1621"},"name":{"$":"Grotefend"}}}}
         * </pre>
         */
        BADGERFISH
    }

    private final Notation notation;
    private final Map<String, String> jsonXml2JsonNs;
    private final List<String> serializeAsArray;

    /**
     * Builder class for constructing {@link JettisonConfig} options
     */
    public static class Builder {

        private final Notation notation;
        protected Map<String, String> jsonXml2JsonNs = new HashMap<String, String>(0);
        protected List<String> serializeAsArray = new LinkedList<String>();

        private Builder(final Notation notation) {
            this.notation = notation;
        }

        /**
         * Constructs a new immutable {@link JettisonConfig} object based on options set on this Builder
         *
         * @return a non-null {@link JettisonConfig} instance
         */
        public JettisonConfig build() {
            return new JettisonConfig(this);
        }

        private void copyAttributes(final JettisonConfig jc) {
            jsonXml2JsonNs.putAll(jc.getXml2JsonNs());
            serializeAsArray.addAll(jc.getArrayElements());
        }
    }

    /**
     * Builder class for constructing {@link JettisonConfig} options
     * for the {@link JettisonConfig.Notation#MAPPED_JETTISON} convention.
     */
    public static class MappedJettisonBuilder extends Builder {

        private MappedJettisonBuilder(final Notation notation) {
            super(notation);
        }

        /**
         * Setter for XML to JSON namespace mapping.
         * This property is valid for the {@link JettisonConfig.Notation#MAPPED_JETTISON}
         * notation only.
         * <p>
         * The value is a map with zero or more
         * key/value pairs, where the key is an XML namespace and the value
         * is the prefix to use as the replacement for the XML namespace.
         * <p>
         * The default value is a map with zero key/value pairs.
         *
         * @param jsonXml2JsonNs XML to JSON namespace map.
         * @return updated builder instance.
         */
        public MappedJettisonBuilder xml2JsonNs(final Map<String, String> jsonXml2JsonNs) {
            this.jsonXml2JsonNs = jsonXml2JsonNs;
            return this;
        }

        /**
         * Add element names to be treated as arrays.
         * This property is valid for the {@link JettisonConfig.Notation#MAPPED_JETTISON}
         * notation only.
         * <p>
         * Property value is a list of element names that should be treated
         * as arrays even if only a single item is present.
         * <p>
         * The default value is an empty list.
         *
         * @param arrays names to be serialized as arrays.
         * @return updated builder instance.
         */
        public MappedJettisonBuilder serializeAsArray(final String... arrays) {
            return serializeAsArray(Arrays.asList(arrays));
        }

        /**
         * Add element names to be treated as arrays.
         * This property is valid for the {@link JettisonConfig.Notation#MAPPED_JETTISON}
         * notation only.
         * <p>
         * Property value is a list of element names that should be treated
         * as arrays even if only a single item is present.
         * <p>
         * The default value is an empty list.
         *
         * @param arrays of element names to be serialized as arrays.
         * @return updated builder instance.
         */
        public MappedJettisonBuilder serializeAsArray(final List<String> arrays) {
            this.serializeAsArray.addAll(arrays);
            return this;
        }
    }

    private JettisonConfig(final Builder b) {
        notation = b.notation;
        jsonXml2JsonNs = b.jsonXml2JsonNs;
        serializeAsArray = b.serializeAsArray;
    }

    /**
     * A static method for obtaining {@link JettisonConfig} instance with humanReadableFormatting
     * set according to formatted parameter.
     *
     * @param c original instance of {@link JettisonConfig}, can't be null
     * @return copy of provided {@link JettisonConfig} with humanReadableFormatting set to formatted.
     * @throws IllegalArgumentException when provided {@code JettisonConfig} is null.
     */
    public static JettisonConfig createJSONConfiguration(final JettisonConfig c) throws IllegalArgumentException {
        if (c == null) {
            throw new IllegalArgumentException("JettisonConfig can't be null");
        }

        final Builder b = copyBuilder(c);

        return b.build();
    }

    /**
     * The default {@code JettisonConfig} uses {@link JettisonConfig.Notation#MAPPED_JETTISON}
     * notation with root unwrapping option set to true.
     */
    public static final JettisonConfig DEFAULT = mappedJettison().build();

    /**
     * A static method for obtaining a builder of {@link JettisonConfig} instance, which will use {@link JettisonConfig.Notation#MAPPED_JETTISON} JSON notation.
     * After getting the builder, you can set configuration options on it and finally get an immutable {@code JettisonConfig}
     * instance using the {@link JettisonConfig.Builder#build() } method.
     *
     * @return a builder for {@code JettisonConfig} instance
     */
    public static MappedJettisonBuilder mappedJettison() {
        return new MappedJettisonBuilder(Notation.MAPPED_JETTISON);
    }

    /**
     * A static method for obtaining a builder of {@link JettisonConfig} instance, which will use {@link JettisonConfig.Notation#BADGERFISH} JSON notation.
     * After getting the builder, you can set configuration options on it and finally get an immutable {@code JettisonConfig}
     * instance using the {@link JettisonConfig.Builder#build() } method.
     *
     * @return a builder for {@code JettisonConfig} instance
     */
    public static Builder badgerFish() {
        return new Builder(Notation.BADGERFISH);
    }

    public static Builder copyBuilder(final JettisonConfig jc) {

        Builder result = new Builder(jc.getNotation());

        switch (jc.notation) {
            case BADGERFISH:
                result = new Builder(jc.getNotation());
                break;
            case MAPPED_JETTISON:
                result = new MappedJettisonBuilder(jc.getNotation());
                break;
        }

        result.copyAttributes(jc);

        return result;
    }

    /**
     * Returns JSON notation selected for this configuration
     * @return JSON notation
     */
    public Notation getNotation() {
        return notation;
    }

    /**
     * Returns a map for XML to JSON namespace mapping
     * This property is valid for the {@link JettisonConfig.Notation#MAPPED_JETTISON}
     * notation only.
     * @return a map for XML to JSON namespace mapping.
     * @see JettisonConfig.MappedJettisonBuilder#xml2JsonNs(java.util.Map)
     */
    public Map<String, String> getXml2JsonNs() {
        return (jsonXml2JsonNs != null) ? Collections.unmodifiableMap(jsonXml2JsonNs) : null;
    }

    /**
     * Returns a list of elements to be treated as arrays. I.e. these elements will be serialized
     * as arrays even if only a single element is included.
     * This property is valid for the {@link JettisonConfig.Notation#MAPPED_JETTISON}
     * notation only.
     * @return a list of elements representing arrays.
     * @see JettisonConfig.MappedJettisonBuilder#serializeAsArray(java.util.List)
     */
    public List<String> getArrayElements() {
        return Collections.unmodifiableList(serializeAsArray);
    }

    @Override
    public String toString() {
        return String.format("{notation:%s}", notation);
    }
}
