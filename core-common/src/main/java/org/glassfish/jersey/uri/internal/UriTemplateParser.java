/*
 * Copyright (c) 2010, 2023 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.uri.UriComponent;

/**
 * A URI template parser that parses JAX-RS specific URI templates.
 *
 * @author Paul Sandoz
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
public class UriTemplateParser {

    /* package */ static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final Set<Character> RESERVED_REGEX_CHARACTERS = initReserved();
    private static final String[] HEX_TO_UPPERCASE_REGEX = initHexToUpperCaseRegex();

    private static Set<Character> initReserved() {
        char[] reserved = {
                '.', '^', '&', '!',
                '?', '-', ':', '<',
                '(', '[', '$', '=',
                ')', ']', ',', '>',
                '*', '+', '|'};

        Set<Character> s = new HashSet<Character>(reserved.length);
        for (char c : reserved) {
            s.add(c);
        }
        return s;
    }

    /**
     * Default URI template value regexp pattern.
     */
    public static final Pattern TEMPLATE_VALUE_PATTERN = Pattern.compile("[^/]+");
    public static final Pattern TEMPLATE_VALUE_PATTERN_MULTI = Pattern.compile("[^,/]+");
    public static final Pattern MATCH_NUMBER_OF_MAX_LENGTH_4 = Pattern.compile("[1-9][0-9]{0,3}");

    private final String template;
    private final StringBuffer regex = new StringBuffer();
    private final StringBuffer normalizedTemplate = new StringBuffer();
    private final StringBuffer literalCharactersBuffer = new StringBuffer();
    private final Pattern pattern;
    private final List<UriPart> names = new ArrayList<>();
    private final List<UriPart> parts = new ArrayList<>();
    private final List<Integer> groupCounts = new ArrayList<Integer>();
    private final Map<String, Pattern> nameToPattern = new HashMap<String, Pattern>();
    private int numOfExplicitRegexes;
    private int skipGroup;

    private int literalCharacters;

    /**
     * Parse a template.
     *
     * @param template the template.
     * @throws IllegalArgumentException if the template is null, an empty string
     *                                  or does not conform to a JAX-RS URI template.
     */
    public UriTemplateParser(final String template) throws IllegalArgumentException {
        if (template == null || template.isEmpty()) {
            throw new IllegalArgumentException("Template is null or has zero length");
        }

        this.template = template;
        parse(new CharacterIterator(template));
        try {
            pattern = Pattern.compile(regex.toString());
        } catch (PatternSyntaxException ex) {
            throw new IllegalArgumentException("Invalid syntax for the template expression '"
                    + regex + "'",
                    ex
            );
        }
    }

    /**
     * Get the template.
     *
     * @return the template.
     */
    public final String getTemplate() {
        return template;
    }

    /**
     * Get the pattern.
     *
     * @return the pattern.
     */
    public final Pattern getPattern() {
        return pattern;
    }

    /**
     * Get the normalized template.
     * <p>
     * A normalized template is a template without any explicit regular
     * expressions.
     *
     * @return the normalized template.
     */
    public final String getNormalizedTemplate() {
        return normalizedTemplate.toString();
    }

    /**
     * Get the map of template names to patterns.
     *
     * @return the map of template names to patterns.
     */
    public final Map<String, Pattern> getNameToPattern() {
        return nameToPattern;
    }

    /**
     * Get the list of template names.
     *
     * @return the list of template names.
     */
    public final List<UriPart> getNames() {
        return names;
    }

    /**
     * Get a collection of uri parts (static strings and dynamic arguments) as parsed by the parser.
     * Can be used to compose the uri. This collection is usually a superset of {@link #getNames() names}
     * and other parts that do not have a template.
     *
     * @return List of parts of the uri.
     */
    public List<UriPart> getUriParts() {
        return parts;
    }

    /**
     * Get the capturing group counts for each template variable.
     *
     * @return the capturing group counts.
     */
    public final List<Integer> getGroupCounts() {
        return groupCounts;
    }

    /**
     * Get the group indexes to capturing groups.
     * <p>
     * Any nested capturing groups will be ignored and the
     * the group index will refer to the top-level capturing
     * groups associated with the templates variables.
     *
     * @return the group indexes to capturing groups.
     */
    public final int[] getGroupIndexes() {
        if (names.isEmpty()) {
            return EMPTY_INT_ARRAY;
        }

        int[] indexes = new int[names.size()];
        indexes[0] = 0 + groupCounts.get(0);
        for (int i = 1; i < indexes.length; i++) {
            indexes[i] = indexes[i - 1] + groupCounts.get(i);
        }

        return indexes;
    }

    /**
     * Get the number of explicit regular expressions.
     *
     * @return the number of explicit regular expressions.
     */
    public final int getNumberOfExplicitRegexes() {
        return numOfExplicitRegexes;
    }

    /**
     * Get the number of regular expression groups
     *
     * @return the number of regular expressions groups
     *
     * @since 2.9
     */
    public final int getNumberOfRegexGroups() {
        if (groupCounts.isEmpty()) {
            return 0;
        } else {
            int[] groupIndex = getGroupIndexes();
            return groupIndex[groupIndex.length - 1] + skipGroup;
        }
    }

    /**
     * Get the number of literal characters.
     *
     * @return the number of literal characters.
     */
    public final int getNumberOfLiteralCharacters() {
        return literalCharacters;
    }

    /**
     * Encode literal characters of a template.
     *
     * @param characters the literal characters
     * @return the encoded literal characters.
     */
    protected String encodeLiteralCharacters(final String characters) {
        return characters;
    }

    private void parse(final CharacterIterator ci) {
        try {
            while (ci.hasNext()) {
                char c = ci.next();
                if (c == '{') {
                    processLiteralCharacters();
                    skipGroup = parseName(ci, skipGroup);
                } else {
                    literalCharactersBuffer.append(c);
                }
            }
            processLiteralCharacters();
        } catch (NoSuchElementException ex) {
            throw new IllegalArgumentException(LocalizationMessages.ERROR_TEMPLATE_PARSER_INVALID_SYNTAX_TERMINATED(
                    template), ex);
        }
    }

    private void processLiteralCharacters() {
        if (literalCharactersBuffer.length() > 0) {
            literalCharacters += literalCharactersBuffer.length();

            String s = encodeLiteralCharacters(literalCharactersBuffer.toString());

            normalizedTemplate.append(s);
            parts.add(new UriPart(s));

            // Escape if reserved regex character
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (RESERVED_REGEX_CHARACTERS.contains(c)) {
                    regex.append("\\");
                    regex.append(c);
                } else if (c == '%') {
                    final char c1 = s.charAt(i + 1);
                    final char c2 = s.charAt(i + 2);
                    if (UriComponent.isHexCharacter(c1) && UriComponent.isHexCharacter(c2)) {
                        regex.append("%").append(HEX_TO_UPPERCASE_REGEX[c1]).append(HEX_TO_UPPERCASE_REGEX[c2]);
                        i += 2;
                    }
                } else {
                    regex.append(c);
                }
            }
            literalCharactersBuffer.setLength(0);
        }
    }

    private static String[] initHexToUpperCaseRegex() {
        String[] table = new String[0x80];
        for (int i = 0; i < table.length; i++) {
            table[i] = String.valueOf((char) i);
        }

        for (char c = 'a'; c <= 'f'; c++) {
            // initialize table values: table[a] = ([aA]) ...
            table[c] = "[" + c + (char) (c - 'a' + 'A') + "]";
        }

        for (char c = 'A'; c <= 'F'; c++) {
            // initialize table values: table[A] = ([aA]) ...
            table[c] = "[" + (char) (c - 'A' + 'a') + c + "]";
        }
        return table;
    }

    private int parseName(final CharacterIterator ci, int skipGroup) {
        Variables variables = new Variables();
        variables.parse(ci, template);

        Pattern namePattern;
        // Make sure we display something useful
        String name = variables.getName();
        int argIndex = 0;
        try {
            switch (variables.paramType) {
            case '?':
            case ';':
            case '&':
                // Build up the regex for each of these properties
                StringBuilder regexBuilder = new StringBuilder();
                String separator = null;
                switch (variables.paramType) {
                    case '?':
                        separator = "\\&";
                        regexBuilder.append("\\?"); // first symbol
                        break;
                    case '&':
                        separator = "\\&";
                        regexBuilder.append("\\&"); // first symbol
                        break;
                    case ';':
                        separator = ";/\\?";
                        regexBuilder.append(";"); // first symbol
                        break;
                }


                // Start a group because each parameter could repeat
                //                names.add("__" + (paramType == '?' ? "query" : "matrix"));

                regexBuilder.append('(');
                for (String subName : variables.names) {

                    TemplateVariable.Position position = determinePosition(variables.separatorCount, argIndex);
                    TemplateVariable templateVariable =
                            TemplateVariable.createTemplateVariable(variables.paramType, subName, position);
                    templateVariable.setStar(variables.explodes(argIndex));

                    regexBuilder.append("(&?");
                    regexBuilder.append(subName);
                    regexBuilder.append("(=([^");
                    regexBuilder.append(separator);
                    regexBuilder.append(']');
                    if (variables.hasLength(argIndex)) {
                        regexBuilder.append('{').append(variables.getLength(argIndex)).append('}');
                        templateVariable.setLength(variables.getLength(argIndex));
                    } else {
                        regexBuilder.append('*');
                    }
                    regexBuilder.append("))?");
                    regexBuilder.append(')');
                    if (argIndex != 0) {
                        regexBuilder.append('|');
                    }

                    names.add(templateVariable);
                    parts.add(templateVariable);

                    groupCounts.add(
                            argIndex == 0 ? 5 : 3);
                    argIndex++;
                }

                //                groupCounts.add(1);
                skipGroup = 1;

                // Knock of last bar
                regexBuilder.append(")*");

                namePattern = Pattern.compile(regexBuilder.toString());

                // Make sure we display something useful
                break;
            default:
                if (variables.separatorCount == 0) {
                    if (variables.hasRegexp(0)) {
                        numOfExplicitRegexes++;
                    }

                    TemplateVariable templateVariable = TemplateVariable
                            .createTemplateVariable(variables.paramType, variables.getName(0), TemplateVariable.Position.SINGLE);
                    templateVariable.setStar(variables.explodes(0));
                    names.add(templateVariable);
                    parts.add(templateVariable);
                    //               groupCounts.add(1 + skipGroup);

                    if (variables.hasLength(0)) {
                        int len = TEMPLATE_VALUE_PATTERN.pattern().length() - 1;
                        String pattern = TEMPLATE_VALUE_PATTERN.pattern().substring(0, len) + '{' + variables.getLength(0) + '}';
                        namePattern = Pattern.compile(pattern);
                        templateVariable.setLength(variables.getLength(0));
                    } else {
                        namePattern = (!variables.hasRegexp(0))
                                ? TEMPLATE_VALUE_PATTERN : Pattern.compile(variables.regexp(0));
                    }
                    if (nameToPattern.containsKey(name)) {
                        if (!nameToPattern.get(name).equals(namePattern)) {
                            throw new IllegalArgumentException(
                                   LocalizationMessages.ERROR_TEMPLATE_PARSER_NAME_MORE_THAN_ONCE(name, template));
                        }
                    } else {
                        nameToPattern.put(name, namePattern);
                    }

                    // Determine group count of pattern
                    Matcher m = namePattern.matcher("");
                    int g = m.groupCount();
                    groupCounts.add(1 + skipGroup);
                    skipGroup = g;
                } else {
                    argIndex = 0;
                    regexBuilder = new StringBuilder();

                    for (String subName : variables.names) {
                        if (argIndex != 0) {
                            regexBuilder
                                    .append('(')
                                    .append(',');
                        }
                        TemplateVariable.Position position = determinePosition(variables.separatorCount, argIndex);
                        TemplateVariable templateVariable
                                = TemplateVariable.createTemplateVariable(variables.paramType, subName, position);
                        templateVariable.setStar(variables.explodes(argIndex));
                        names.add(templateVariable);
                        parts.add(templateVariable);

                        if (variables.hasLength(argIndex)) {
                            int len = TEMPLATE_VALUE_PATTERN_MULTI.pattern().length() - 1;
                            String pattern = TEMPLATE_VALUE_PATTERN_MULTI.pattern()
                                    .substring(0, len) + '{' + variables.getLength(argIndex) + '}';
                            namePattern = Pattern.compile(pattern);
                            templateVariable.setLength(variables.getLength(argIndex));
                        } else {
                            namePattern = (!variables.hasRegexp(argIndex))
                                    ? TEMPLATE_VALUE_PATTERN_MULTI : Pattern.compile(variables.regexp(argIndex));
                        }
//                      TODO breaks RFC 6570 --backward compatibility with default pattern
                        if (nameToPattern.containsKey(subName) && variables.paramType == 'p') {
                            if (!nameToPattern.get(subName).equals(namePattern)) {
                                throw new IllegalArgumentException(
                                        LocalizationMessages.ERROR_TEMPLATE_PARSER_NAME_MORE_THAN_ONCE(name, template));
                            }
                        } else {
                            nameToPattern.put(subName, namePattern);
                        }

                        regexBuilder
                                .append('(')
                                .append(namePattern)
                                .append(')');

                        if (argIndex != 0) {
                            regexBuilder.append(")");
                        }
                        regexBuilder.append("{0,1}");

                        argIndex++;
                        groupCounts.add(2);
                    }
                    namePattern = Pattern.compile(regexBuilder.toString());
                }
                break;
            }

            regex.append('(')
                    .append(namePattern)
                    .append(')');

            normalizedTemplate.append('{')
                    .append(name)
                    .append('}');
        } catch (PatternSyntaxException ex) {
            throw new IllegalArgumentException(LocalizationMessages
                    .ERROR_TEMPLATE_PARSER_INVALID_SYNTAX(variables.regexp(argIndex), variables.name, template), ex);
        }

        // Tell the next time through the loop how many to skip
        return skipGroup;
    }

    private static TemplateVariable.Position determinePosition(int separatorCount, int argIndex) {
        TemplateVariable.Position position = separatorCount == 0
                ? TemplateVariable.Position.SINGLE
                : argIndex == 0
                    ? TemplateVariable.Position.FIRST
                    : argIndex == separatorCount ? TemplateVariable.Position.LAST : TemplateVariable.Position.MIDDLE;
        return position;
    }

    private static class Variables {
        private char paramType = 'p';
        private List<String> names = new ArrayList<>(); // names
        private List<Boolean> explodes = new ArrayList<>(); // *
        private List<String> regexps = new ArrayList<>();  // : regexp
        private List<Integer> lengths = new ArrayList<>(); // :1-9999
        private int separatorCount = 0;
        private StringBuilder name = new StringBuilder();

        private int getCount() {
            return names.size();
        }

        private boolean explodes(int index) {
            return !explodes.isEmpty() && explodes.get(index);
        }

        private boolean hasRegexp(int index) {
            return !regexps.isEmpty() && regexps.get(index) != null;
        }

        private String regexp(int index) {
            return regexps.get(index);
        }

        private boolean hasLength(int index) {
            return !lengths.isEmpty() && lengths.get(index) != null;
        }

        private Integer getLength(int index) {
            return lengths.get(index);
        }

        private char getParamType() {
            return paramType;
        }

        private int getSeparatorCount() {
            return separatorCount;
        }

        private String getName() {
            return name.toString();
        }

        private String getName(int index) {
            return names.get(index);
        }

        private void parse(CharacterIterator ci, String template) {
            name.append('{');

            char c = consumeWhiteSpace(ci);

            StringBuilder nameBuilder = new StringBuilder();

            // Look for query or matrix types
            if (c == '?' || c == ';' || c == '.' || c == '+' || c == '#' || c == '/' || c == '&') {
                paramType = c;
                c = ci.next();
                name.append(paramType);
            }

            if (Character.isLetterOrDigit(c) || c == '_') {
                // Template name character
                nameBuilder.append(c);
                name.append(c);
            } else {
                throw new IllegalArgumentException(LocalizationMessages.ERROR_TEMPLATE_PARSER_ILLEGAL_CHAR_START_NAME(c, ci.pos(),
                        template));
            }

            StringBuilder regexBuilder = new StringBuilder();
            State state = State.TEMPLATE;
            boolean star = false;
            boolean whiteSpace = false;
            boolean ignoredLastComma = false;
            int bracketDepth = 1;  // {
            int regExpBracket = 0; // [
            int regExpRound = 0;   // (
            boolean reqExpSlash = false; // \
            while ((state.value & (State.ERROR.value | State.EXIT.value)) == 0) {
                c = ci.next();
                // "\\{(\\w[-\\w\\.]*)
                if (Character.isLetterOrDigit(c)) {
                    // Template name character
                    append(c, state, nameBuilder, regexBuilder);
                    state = state.transition(State.TEMPLATE.value | State.REGEXP.value);
                } else switch (c) {
                    case '_':
                    case '-':
                    case '.':
                        // Template name character
                        append(c, state, nameBuilder, regexBuilder);
                        state = state.transition(State.TEMPLATE.value | State.REGEXP.value);
                        break;
                    case ',':
                        switch (state) {
                            case REGEXP:
                                if (bracketDepth == 1 && !reqExpSlash && regExpBracket == 0 && regExpRound == 0) {
                                    state = State.COMMA;
                                } else {
                                    regexBuilder.append(c);
                                }
                                break;
                            case TEMPLATE:
                            case STAR:
                                state = State.COMMA;
                                break;
                        }
                        separatorCount++;
                        break;
                    case ':':
                        if (state == State.REGEXP) {
                            regexBuilder.append(c);
                        }
                        state = state.transition(State.TEMPLATE.value | State.REGEXP.value | State.STAR.value, State.REGEXP);
                        break;
                    case '*':
                        state = state.transition(State.TEMPLATE.value | State.REGEXP.value);
                        if (state == State.TEMPLATE) {
                            star = true;
                            state = State.STAR;
                        } else if (state == State.REGEXP){
                            regexBuilder.append(c);
                        }
                        break;
                    case '}':
                        bracketDepth--;
                        if (bracketDepth == 0) {
                            state = State.BRACKET;
                        } else {
                            regexBuilder.append(c);
                        }
                        break;
                    case '{':
                        if (state == State.REGEXP) {
                            bracketDepth++;
                            regexBuilder.append(c);
                        } else {
                            state = State.ERROR; // Error multiple parenthesis
                        }
                        break;
                    default:
                        if (!Character.isWhitespace(c)) {
                            if (state != State.REGEXP) {
                                state = State.ERROR; // Error - unknown symbol
                            } else {
                                switch (c) {
                                    case '(' :
                                        regExpRound++;
                                        break;
                                    case ')':
                                        regExpRound--;
                                        break;
                                    case '[':
                                        regExpBracket++;
                                        break;
                                    case ']':
                                        regExpBracket--;
                                        break;
                                }
                                if (c == '\\') {
                                    reqExpSlash = true;
                                } else {
                                    reqExpSlash = false;
                                }
                                regexBuilder.append(c);
                            }
                        }
                        whiteSpace = true;
                        break;
                }

                // Store parsed name, and associated star, regexp, and length
                switch (state) {
                    case COMMA:
                    case BRACKET:
                        if (nameBuilder.length() == 0 && regexBuilder.length() == 0 && !star
                                && name.charAt(name.length() - 1) == ',' /* ignore last comma */) {
                            if (ignoredLastComma) { // Do not ignore twice
                                state = State.ERROR;
                            } else {
                                name.setLength(name.length() - 1);
                                ignoredLastComma = true;
                            }
                            break;
                        }
                        if (regexBuilder.length() != 0) {
                            String regex = regexBuilder.toString();
                            Matcher matcher = MATCH_NUMBER_OF_MAX_LENGTH_4.matcher(regex);
                            if (matcher.matches()) {
                                lengths.add(Integer.parseInt(regex));
                                regexps.add(null);
                            } else {
                                if (paramType != 'p') {
                                    state = State.ERROR; // regular expressions allowed just on path by the REST spec
                                    c = regex.charAt(0); // display proper error values
                                    ci.setPosition(ci.pos() - regex.length());
                                    break;
                                }
                                lengths.add(null);
                                regexps.add(regex);
                            }
                        } else {
                            regexps.add(null);
                            lengths.add(null);
                        }

                        names.add(nameBuilder.toString());
                        explodes.add(star);

                        nameBuilder.setLength(0);
                        regexBuilder.setLength(0);
                        star = false;
                        ignoredLastComma = false;
                        break;
                }

                if (!whiteSpace) {
                    name.append(c);
                }
                whiteSpace = false;

                // switch state back or exit
                switch (state) {
                    case COMMA:
                        state = State.TEMPLATE;
                        break;
                    case BRACKET:
                        state = State.EXIT;
                        break;
                }
            }

            if (state == State.ERROR) {
                throw new IllegalArgumentException(
                        LocalizationMessages.ERROR_TEMPLATE_PARSER_ILLEGAL_CHAR_AFTER_NAME(c, ci.pos(), template));
            }
        }

        private static void append(char c, State state, StringBuilder templateSb, StringBuilder regexpSb) {
            if (state == State.TEMPLATE) {
                templateSb.append(c);
            } else { // REGEXP
                regexpSb.append(c);
            }
        }

        private static char consumeWhiteSpace(final CharacterIterator ci) {
            char c;
            do {
                c = ci.next();
            } while (Character.isWhitespace(c));

            return c;
        }

        private enum State {
            TEMPLATE/**/(0b000000001), // Template name, before '*', ':', ',' or '}'
            REGEXP/*  */(0b000000010), // Regular expression inside template, after :
            STAR/*    */(0b000000100), // *
            COMMA/*   */(0b000001000), // ,
            BRACKET/* */(0b000010000), // }
            EXIT/*    */(0b001000000), // quit parsing
            ERROR/*   */(0b100000000); // error when parsing
            private final int value;
            State(int value) {
                this.value = value;
            }

            /**
             * Return error state when in not any of allowed states represented by their combined values
             * @param allowed The combined values of states (state1.value | state2.value) not to return error level
             * @return this state if in allowed state or {@link State#ERROR} if not
             */
            State transition(int allowed) {
                return ((value & allowed) != 0) ? this : State.ERROR;
            }

            /**
             * Return error state when in not any of allowed states represented by their combined values
             * @param allowed The combined values of states (state1.value | state2.value) not to return error level
             * @param next the next state to transition
             * @return next state if in allowed state or {@link State#ERROR} if not
             */
            State transition(int allowed, State next) {
                return ((value & allowed) != 0) ? next : State.ERROR;
            }
        }
    }
}
