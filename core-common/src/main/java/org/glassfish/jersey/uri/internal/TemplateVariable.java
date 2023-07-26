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

import java.util.Collection;
import java.util.Map;

/**
 * The Reserved Expansion template variable representation as per RFC6570.
 */
/* package */ class TemplateVariable extends UriPart {

    protected final Position position;
    protected int len = -1; // unlimited
    protected boolean star = false;

    TemplateVariable(String part, Position position) {
        super(part);
        this.position = position;
    }

    /**
     * Choose the template variable type. The
     * @param type Type of the template
     * @param part the template content
     * @param position the position of the variable in the template.
     * @return Subclass of Templatevariable to represent the variable and allowing expansion based on the type of the variable
     */
    static TemplateVariable createTemplateVariable(char type, String part, Position position) {
        TemplateVariable newType;
        switch (type) {
            case '+':
                newType = new TemplateVariable(part, position);
                break;
            case '-': // Not supported by RFC
                newType = new MinusTemplateVariable(part, position);
                break;
            case '#':
                newType = new HashTemplateVariable(part, position);
                break;
            case '.':
                newType = new DotTemplateVariable(part, position);
                break;
            case '/':
                newType = new SlashTemplateVariable(part, position);
                break;
            case ';':
                newType = new MatrixTemplateVariable(part, position);
                break;
            case '?':
                newType = new QueryTemplateVariable(part, position);
                break;
            case '&':
                newType = new QueryContinuationTemplateVariable(part, position);
                break;
            default:
                //'p'
                newType = new PathTemplateVariable(part, position);
                break;
        }
        return newType;
    }

    @Override
    public boolean isTemplate() {
        return true;
    }

    @Override
    public String getGroup() {
        StringBuilder sb = new StringBuilder();
        if (position.isFirst()) {
            sb.append('{');
        } else {
            sb.append(',');
        }
        sb.append(getPart());
        if (position.isLast()) {
            sb.append('}');
        }
        return sb.toString();
    }

    @Override
    public String resolve(Object value, UriComponent.Type type, boolean encode) {
        if (value == null) {
            return "";
        }
        return position.isFirst()
                ? plainResolve(value, type, encode)
                : separator() + plainResolve(value, type, encode);
    }

    protected char separator() {
        return ',';
    }

    protected char keyValueSeparator() {
        return star ? '=' : ',';
    }

    protected String plainResolve(Object value, UriComponent.Type componentType, boolean encode) {
        if (Collection.class.isInstance(value)) {
            return ((Collection<Object>) value).stream()
                    .map(a -> plainResolve(a, componentType, encode))
                    .reduce("", (a, b) -> a + (a.isEmpty() ? b : separator() + b));
        } else if (Map.class.isInstance(value)) {
            return ((Map<?, ?>) value).entrySet().stream()
                    .map(e -> plainResolve(e.getKey(), componentType, encode)
                            + keyValueSeparator()
                            + plainResolve(e.getValue(), componentType, encode))
                    .reduce("", (a, b) -> a + (a.isEmpty() ? b : separator() + b));
        } else {
            return plainResolve(value.toString(), componentType, encode);
        }
    }

    protected String plainResolve(String value, UriComponent.Type componentType, boolean encode) {
        String val = len == -1 ? value : value.substring(0, Math.min(value.length(), len));
        return encode(val, componentType, encode);
    }

    protected String encode(String toEncode, UriComponent.Type componentType, boolean encode) {
        if (componentType == null) {
            componentType = getDefaultType();
        }
        return UriPart.percentEncode(toEncode, componentType, encode);
    }

    protected UriComponent.Type getDefaultType() {
        return UriComponent.Type.PATH;
    }

    void setLength(int len) {
        this.len = len;
    }

    void setStar(boolean b) {
        star = b;
    }

    /**
     * The default UriBuilder template
     */
    private static class PathTemplateVariable extends TemplateVariable {
        protected PathTemplateVariable(String part, Position position) {
            super(part, position);
        }

        @Override
        public boolean throwWhenNoTemplateArg() {
            return true; // The default UriBuilder behaviour
        }

        @Override
        protected UriComponent.Type getDefaultType() {
            return UriComponent.Type.PATH;
        }
    }

    /**
     * The template that works according to RFC 6570, Section 3.2.2.
     * The default Path works as described in Section 3.2.3, as described by RFC 3986.
     */
    private static class MinusTemplateVariable extends TemplateVariable {
        protected MinusTemplateVariable(String part, Position position) {
            super(part, position);
        }

        @Override
        protected String encode(String toEncode, UriComponent.Type componentType, boolean encode) {
            return super.encode(toEncode, UriComponent.Type.QUERY, encode); //Query has the same encoding as Section 3.2.3
        }

        @Override
        protected UriComponent.Type getDefaultType() {
            return UriComponent.Type.QUERY;
        }
    }


    /**
     * Section 3.2.5
     */
    private static class DotTemplateVariable extends MinusTemplateVariable {
        protected DotTemplateVariable(String part, Position position) {
            super(part, position);
        }

        @Override
        public String resolve(Object value, UriComponent.Type type, boolean encode) {
            if (value == null) {
                return "";
            }
            return '.' + plainResolve(value, type, encode);
        }

        @Override
        protected char separator() {
            return star ? '.' : super.separator();
        }
    }

    /**
     * Section 3.2.6
     */
    private static class SlashTemplateVariable extends MinusTemplateVariable {
        protected SlashTemplateVariable(String part, Position position) {
            super(part, position);
        }

        @Override
        public String resolve(Object value, UriComponent.Type type, boolean encode) {
            if (value == null) {
                return "";
            }
            return '/' + plainResolve(value, type, encode);
        }

        @Override
        protected char separator() {
            return star ? '/' : super.separator();
        }
    }

    /**
     * Section 3.2.4
     */
    private static class HashTemplateVariable extends TemplateVariable {
        protected HashTemplateVariable(String part, Position position) {
            super(part, position);
        }

        @Override
        public String resolve(Object value, UriComponent.Type type, boolean encode) {
            return (value == null || !position.isFirst() ? "" : "#") + super.resolve(value, type, encode);
        }

        @Override
        protected UriComponent.Type getDefaultType() {
            return UriComponent.Type.PATH;
        }
    }


    private abstract static class ExtendedVariable extends TemplateVariable {

        private final Character firstSymbol;
        private final char separator;
        protected final boolean appendEmpty;

        protected ExtendedVariable(String part, Position position, Character firstSymbol, char separator, boolean appendEmpty) {
            super(part, position);
            this.firstSymbol = firstSymbol;
            this.separator = separator;
            this.appendEmpty = appendEmpty;
        }

        @Override
        public String resolve(Object value, UriComponent.Type componentType, boolean encode) {
            if (value == null) { // RFC 6570
                return "";
            }
            String sValue = super.plainResolve(value, componentType, encode);
            StringBuilder sb = new StringBuilder();

            if (position.isFirst()) {
                sb.append(firstSymbol);
            } else {
                sb.append(separator);
            }

            if (!star) {
                sb.append(getPart());
                if (appendEmpty || !sValue.isEmpty()) {
                    sb.append('=').append(sValue);
                }
            } else if (!Map.class.isInstance(value)) {
                String[] split = sValue.split(String.valueOf(separator()));
                for (int i = 0; i != split.length; i++) {
                    sb.append(getPart());
                    sb.append('=').append(split[i]);
                    if (i != split.length - 1) {
                        sb.append(separator);
                    }
                }
            } else if (Map.class.isInstance(value)) {
                sb.append(sValue);
            }
            return sb.toString();
        }

        @Override
        protected char separator() {
            return star ? separator : super.separator();
        }
    }

    /**
     * Section 3.2.7
     */
    private static class MatrixTemplateVariable extends ExtendedVariable {
        protected MatrixTemplateVariable(String part, Position position) {
            super(part, position, ';', ';', false);
        }

        @Override
        protected UriComponent.Type getDefaultType() {
            return UriComponent.Type.QUERY; // For matrix, use query encoding per 6570
        }

        @Override
        public String resolve(Object value, UriComponent.Type componentType, boolean encode) {
            return super.resolve(value, getDefaultType(), encode);
        }
    }

    /**
     * Section 3.2.8
     */
    private static class QueryTemplateVariable extends ExtendedVariable {
        protected QueryTemplateVariable(String part, Position position) {
            super(part, position, '?', '&', true);
        }
    }

    /**
     * Section 3.2.9
     */
    private static class QueryContinuationTemplateVariable extends ExtendedVariable {
        protected QueryContinuationTemplateVariable(String part, Position position) {
            super(part, position, '&', '&', true);
        }

        @Override
        protected UriComponent.Type getDefaultType() {
            return UriComponent.Type.QUERY;
        }

        @Override
        public String resolve(Object value, UriComponent.Type componentType, boolean encode) {
            return super.resolve(value, getDefaultType(), encode);
        }
    }

    /**
     * <p>
     *  Position of the template variable. For instance, template {@code {first, middle, last}} would have three arguments, on
     *  {@link Position#FIRST}, {@link Position#MIDDLE}, and {@link Position#LAST} positions.
     *  If only a single argument is in template (most common) e.g. {@code {single}}, the position is {@link Position#SINGLE}.
     * </p>
     * <p>
     *  {@link Position#SINGLE} is first (see {@link Position#isFirst()}) and last (see {@link Position#isLast()}) at the same time.
     * </p>
     */

    /* package */ static enum Position {
        FIRST((byte) 0b1100),
        MIDDLE((byte) 0b1010),
        LAST((byte) 0b1001),
        SINGLE((byte) 0b1111);

        final byte val;

        Position(byte val) {
            this.val = val;
        }

        /**
         * Informs whether the position of the argument is the last in the argument group.
         * @return true when the argument is the last.
         */
        boolean isLast() {
            return (val & LAST.val) == LAST.val;
        }

        /**
         * Informs whether the position of the argument is the first in the argument group.
         * @return true when the argument is the first.
         */
        boolean isFirst() {
            return (val & FIRST.val) == FIRST.val;
        }
    }
}
