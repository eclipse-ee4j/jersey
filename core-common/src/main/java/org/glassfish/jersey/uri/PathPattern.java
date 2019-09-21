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

package org.glassfish.jersey.uri;

import java.util.Comparator;

/**
 * A path pattern that is a regular expression generated from a URI path
 * template.
 * <p>
 * The path pattern is normalized by removing a terminating "/" if present.
 * <p>
 * The path pattern is post-fixed with a right hand pattern that consists of either
 * a matching group that matches zero or more path segments,
 * see {@link RightHandPath#capturingZeroOrMoreSegments}, or zero path
 * segments, see {@link RightHandPath#capturingZeroSegments}.
 *
 * @author Paul Sandoz
 */
public final class PathPattern extends PatternWithGroups {

    /**
     * Empty path pattern matching only empty string.
     */
    public static final PathPattern EMPTY_PATTERN = new PathPattern();
    /**
     * Path pattern matching the end of a URI path. Can be either empty {@code ""}
     * or contain a trailing slash {@code "/"}.
     */
    public static final PathPattern END_OF_PATH_PATTERN = new PathPattern("", PathPattern.RightHandPath.capturingZeroSegments);
    /**
     * Path pattern matching the any URI path.
     */
    public static final PathPattern OPEN_ROOT_PATH_PATTERN = new PathPattern("", RightHandPath.capturingZeroOrMoreSegments);
    /**
     * Path pattern comparator that defers to {@link UriTemplate#COMPARATOR comparing
     * the templates} associated with the patterns.
     */
    public static final Comparator<PathPattern> COMPARATOR = new Comparator<PathPattern>() {

        @Override
        public int compare(PathPattern o1, PathPattern o2) {
            return UriTemplate.COMPARATOR.compare(o1.template, o2.template);
        }
    };

    /**
     * The set of right hand path patterns that may be appended to a path
     * pattern.
     */
    public static enum RightHandPath {

        /**
         * A capturing group that matches zero or more path segments and
         * keeps the matching path template open.
         */
        capturingZeroOrMoreSegments("(/.*)?"),
        /**
         * A capturing group that matches zero segments and effectively
         * closes the matching path template.
         */
        capturingZeroSegments("(/)?");
        //
        private final String regex;

        private RightHandPath(String regex) {
            this.regex = regex;
        }

        private String getRegex() {
            return regex;
        }
    }

    /**
     * Return a new path pattern with a same path template but
     * a {@link RightHandPath#capturingZeroSegments closed} right hand path.
     *
     * @param pattern an (open) path pattern to convert to a closed pattern.
     * @return closed path pattern for the same path template.
     */
    public static PathPattern asClosed(PathPattern pattern) {
        return new PathPattern(pattern.getTemplate().getTemplate(), RightHandPath.capturingZeroSegments);
    }

    //
    private final UriTemplate template;

    private PathPattern() {
        super();
        this.template = UriTemplate.EMPTY;
    }

    /**
     * Create a path pattern and post fix with
     * {@link RightHandPath#capturingZeroOrMoreSegments}.
     *
     * @param template the path template.
     * @see #PathPattern(String, PathPattern.RightHandPath)
     */
    public PathPattern(String template) {
        this(new PathTemplate(template));
    }

    /**
     * Create a path pattern and post fix with
     * {@link RightHandPath#capturingZeroOrMoreSegments}.
     *
     * @param template the path template
     * @see #PathPattern(PathTemplate, PathPattern.RightHandPath)
     */
    public PathPattern(PathTemplate template) {
        super(postfixWithCapturingGroup(template.getPattern().getRegex()),
                addIndexForRightHandPathCapturingGroup(template.getNumberOfRegexGroups(),
                        template.getPattern().getGroupIndexes()));

        this.template = template;
    }

    /**
     * Create a path pattern and post fix with a right hand path pattern.
     *
     * @param template the path template.
     * @param rhpp     the right hand path pattern postfix.
     */
    public PathPattern(String template, RightHandPath rhpp) {
        this(new PathTemplate(template), rhpp);
    }

    /**
     * Create a path pattern and post fix with a right hand path pattern.
     *
     * @param template the path template.
     * @param rhpp     the right hand path pattern postfix.
     */
    public PathPattern(PathTemplate template, RightHandPath rhpp) {
        super(postfixWithCapturingGroup(template.getPattern().getRegex(), rhpp),
                addIndexForRightHandPathCapturingGroup(template.getNumberOfRegexGroups(),
                        template.getPattern().getGroupIndexes()));

        this.template = template;
    }

    public UriTemplate getTemplate() {
        return template;
    }

    private static String postfixWithCapturingGroup(String regex) {
        return postfixWithCapturingGroup(regex, RightHandPath.capturingZeroOrMoreSegments);
    }

    private static String postfixWithCapturingGroup(String regex, RightHandPath rhpp) {
        if (regex.endsWith("/")) {
            regex = regex.substring(0, regex.length() - 1);
        }

        return regex + rhpp.getRegex();
    }

    private static int[] addIndexForRightHandPathCapturingGroup(int numberOfGroups, int[] indexes) {
        if (indexes.length == 0) {
            return indexes;
        }

        int[] cgIndexes = new int[indexes.length + 1];
        System.arraycopy(indexes, 0, cgIndexes, 0, indexes.length);

        cgIndexes[indexes.length] = numberOfGroups + 1;
        return cgIndexes;
    }
}
