/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.oauth2;

/**
 * Class that provides methods to build {@link OAuth2CodeGrantFlow} pre-configured for usage
 * with Google provider.
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
public class OAuth2FlowGoogleBuilder extends AbstractAuthorizationCodeGrantBuilder<OAuth2FlowGoogleBuilder> {

    /**
     * Create a new google flow builder.
     */
    OAuth2FlowGoogleBuilder() {
        super(new AuthCodeGrantImpl.Builder());
    }

    /**
     * Set {@code access type} parameter used in Authorization Request.
     * @param accessType access type value.
     * @return a google authorization flow builder.
     */
    public OAuth2FlowGoogleBuilder accessType(AccessType accessType) {
        return property(OAuth2CodeGrantFlow.Phase.AUTHORIZATION, AccessType.getKey(), accessType.getValue());
    }

    /**
     * Set {@code prompt} parameter used in Authorization Request.
     * @param prompt Prompt value.
     * @return a google authorization flow builder.
     */
    public OAuth2FlowGoogleBuilder prompt(Prompt prompt) {
        return property(OAuth2CodeGrantFlow.Phase.AUTHORIZATION, Prompt.getKey(), prompt.getValue());
    }

    /**
     * Set {@code display} parameter used in Authorization Request.
     * @param display display value.
     * @return a google authorization flow builder.
     */
    public OAuth2FlowGoogleBuilder display(Display display) {
        return property(OAuth2CodeGrantFlow.Phase.AUTHORIZATION, Display.getKey(), display.getValue());
    }

    /**
     * Set {@code login hint} parameter used in Authorization Request.
     * @param loginHint login hint value.
     * @return a google authorization flow builder.
     */
    public OAuth2FlowGoogleBuilder loginHint(String loginHint) {
        return property(OAuth2CodeGrantFlow.Phase.AUTHORIZATION, Display.getKey(), loginHint);
    }

    /**
     * Enum that defines values for "access_type" parameter used in
     * Google OAuth flow. Defines whether the offline access is allowed (without
     * user active session).
     */
    public static enum AccessType {
        ONLINE("online"),
        OFFLINE("offline");

        private final String propertyValue;

        private AccessType(String propertyValue) {
            this.propertyValue = propertyValue;
        }

        public String getValue() {
            return propertyValue;
        }

        public static String getKey() {
            return "access_type";
        }
    }

    /**
     * Enum that defines values for "prompt" parameter used in
     * Google OAuth flow.
     */
    public static enum Prompt {

        NONE("none"),
        /**
         * User will be asked for approval each time the authorization is performed.
         */
        CONSENT("consent"),
        SELECT_ACCOUNT("select_account");

        private final String value;

        private Prompt(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static String getKey() {
            return "prompt";
        }

    }

    /**
     * Enum that defines values for "display" parameter used in
     * Google OAuth flow.
     */
    public static enum Display {
        PAGE("page"),
        POPUP("popup"),
        TOUCH("touch"),
        WAP("wap");

        private final String value;

        private Display(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static String getKey() {
            return "display";
        }
    }

    /**
     * Property key that defines values for "login_hint" parameter used in
     * Google OAuth flow.
     */
    public static final String LOGIN_HINT = "login_hint";

}
