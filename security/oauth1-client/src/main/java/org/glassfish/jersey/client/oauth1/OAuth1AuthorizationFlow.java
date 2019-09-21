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

package org.glassfish.jersey.client.oauth1;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Feature;

/**
 * The interface of the OAuth 1 Authorization Flow utility.
 * <p>
 * The implementation of this interface is capable of performing of the user
 * authorization defined in the OAuth1 specification. The result of the authorization
 * is the {@link AccessToken access token}. The user authorization is called also
 * Authorization Flow. The implementation initiates the authorization process with
 * the Authorization server, then provides redirect URI to which the user should
 * be redirected (the URI points to authorization consent page hosted by Service Provider). The user
 * grants an access using this page. Service Provider redirects the user back to the
 * our server and the authorization process is finished using the implementation.
 * </p>
 * <p>
 * To perform the authorization follow these steps:
 * <list>
 * <li>Get the instance of this interface using {@link OAuth1ClientSupport}.</li>
 * <li>Call {@link #start()} method. The method returns redirection uri as a String. Note: the method internally
 * makes a request to the request token uri and gets Request Token which will be used for the authorization process.</li>
 * <li>Redirect user to the redirect uri returned from the {@code start} method. If your application deployment
 * does not allow redirection (for example the app is a console application), then provide the redirection URI
 * to the user in other ways.</li>
 * <li>User should authorize your application on the redirect URI.</li>
 * <li>After authorization the Authorization Server redirects the user back to the URI specified
 * by {@link OAuth1Builder.FlowBuilder#callbackUri(String)} and provide the {@code oauth_verifier} as
 * a request query parameter. Extract this parameter from the request. If your deployment does not support
 * redirection (your app is not a web server) then Authorization Server will provide the user with
 * {@code verifier} in other ways (for example display on the html page). You need to get
 * this verifier from the user.</li>
 * <li>Use the {@code verifier} to finish the authorization process by calling the method
 * {@link #finish(String)} supplying the verifier. The method will internally request
 * the access token from the Authorization Server and return it.</li>
 * <li>You can use {@code AccessToken} together with {@link ConsumerCredentials} to
 * perform the authenticated requests to the Service Provider. You can also call
 * methods {@link #getAuthorizedClient()} to get {@link Client client} already configured with support
 * for authentication from consumer credentials and access token received during authorization process.
 * </li>
 * </list>
 * </p>
 * <p>
 * Important note: one instance of the interface can be used only for one authorization process. The methods
 * must be called exactly in the order specified by the list above. Therefore the instance is also not
 * thread safe and no concurrent access is expected.
 * </p>
 * Instance must be stored between method calls (between {@code start} and {@code finish})
 * for one user authorization process as the instance keeps
 * internal state of the authorization process.
 * </p>
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
public interface OAuth1AuthorizationFlow {

    /**
     * Start the authorization process and return redirection URI on which the user should give a consent
     * for our application to access resources.
     * <p>
     * Note: the method makes a request to the Authorization Server in order to get request token.
     * </p>
     *
     * @return URI to which user should be redirected.
     */
    public String start();

    /**
     * Finish the authorization process and return the {@link AccessToken}. The method must be called on the
     * same instance after the {@link #start()} method was called and user granted access to this application.
     * <p>
     * The method makes a request to the Authorization Server but does not exchange verifier for access token. This method is
     * intended only for some flows/cases in OAuth1.
     * </p>
     *
     * @return Access token.
     * @since 2.7
     */
    public AccessToken finish();

    /**
     * Finish the authorization process and return the {@link AccessToken}. The method must be called on the
     * same instance after the {@link #start()} method was called and user granted access to this application.
     * <p>
     * The method makes a request to the Authorization Server in order to exchange verifier for access token.
     * </p>
     *
     * @param verifier Verifier provided from the user authorization.
     * @return Access token.
     */
    public AccessToken finish(String verifier);

    /**
     * Return the client configured for performing authorized requests to the Service Provider. The
     * authorization process must be successfully finished by instance by calling methods {@link #start()} and
     * {@link #finish(String)}.
     *
     * @return Client configured to add correct {@code Authorization} header to requests.
     */
    public Client getAuthorizedClient();

    /**
     * Return the {@link javax.ws.rs.core.Feature oauth filter feature} that can be used to configure
     * {@link Client client} instances to perform authenticated requests to the Service Provider.
     * <p>
     * The authorization process must be successfully finished by instance by calling methods {@link #start()} and
     * {@link #finish(String)}.
     * </p>
     *
     * @return oauth filter feature configured with received {@code AccessToken}.
     */
    public Feature getOAuth1Feature();
}
