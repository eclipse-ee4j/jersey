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

/**
 * <h3>JAX-RS Integration with MIME MultiPart Message Formats</h3>
 *
 * <p>The classes in this package provide for integration of
 * <code>multipart/*</code> request
 * and response bodies in a JAX-RS runtime environment.  The set of registered
 * providers is leveraged, in that the content type for a body part of such a
 * message reuses the same MessageBodyReader/MessageBodyWriter implementations
 * as would be used for that content type as a standalone entity.</p>
 *
 * <p>For more information on the syntax and semantics of MIME multipart streams,
 * see the following RFCs:</p>
 * <ul>
 * <li><a href="http://www.ietf.org/rfc/rfc2045.txt">Multipurpose Internet Mail
 *     Extensions (MIME) Part One:  Format of Internet Message Bodies</a>
 *     (RFC 2045)</li>
 * <li><a href="http://www.ietf.org/rfc/rfc2046.txt">Multipurpose Internet Mail
 *     Extensions (MIME) Part Two:  Media Types</a> (RFC 2046)</li>
 * <li><a href="http://www.ietf.org/rfc/rfc2047.txt">Multipurpose Internet Mail
 *     Extensions (MIME) Part Three:  Message Header Extensions for Non-ASCII
 *     Text</a> (RFC 2047)</li>
 * <li><a href="http://www.ietf.org/rfc/rfc2048.txt">Multipurpose Internet Mail
 *     Extensions (MIME) Part Four:  Registration Procedures</a> (RFC 2048)</li>
 * <li><a href="http://www.ietf.org/rfc/rfc2048.txt">Multipurpose Internet Mail
 *     Extensions (MIME) Part Five:  Conformance Criteria and Examples</a>
 *     (RFC 2049)</li>
 * </ul>
 *
 * <h4>Implementation Notes</h4>
 *
 * <p>The following notes describe significant aspects of the implementation
 * of the MIME MultiPart APIs in this (and related) packages:</p>
 * <ul>
 * <li>Although packaged as a Jersey extension, the runtime code in this
 *     library should be portable to any compliant JAX-RS implementation.
 *     Jersey is only required for execution of the unit tests.</li>
 * </ul>
 *
 * <h4>Supported MIME Multipart Capabilities</h4>
 *
 * <p>The following list of general MIME MultiPart features is currently
 * supported:</p>
 * <ul>
 * <li>The <code>MIME-Version: 1.0</code> HTTP header is included on generated
 *     responses.  It is accepted, but not required, on processed requests.</li>
 * <li>A <code>MessageBodyReader</code> implementation for consuming MIME
 *     MultiPart entities.  See below for usage restrictions.</li>
 * <li>A <code>MessageBodyWriter</code> implementation for producing MIME
 *     MultiPart entities.  The appropriate <code>Provider</code> is used to
 *     serialize each body part, based on its media type.</li>
 * <li>Optional creation of an appropriate <code>boundary</code> parameter on a
 *     generated <code>Content-Type</code> header, if not already present.</li>
 * <li>Top level content type of <code>multipart</code>, with the following
 *     supported subtypes:  <code>alternative</code>, <code>digest</code>,
 *     <code>mixed</code>, and <code>parallel</code>.</li>
 * </ul>
 *
 * <p>At present, the <code>MessageBodyReader</code> implementation exhibits a
 * usability issue.  It is not currently possible to know ahead of time what
 * Java class the application would prefer to use for each individual body part,
 * so an appropriate <code>Provider</code> cannot be selected.  Currently, the
 * unparsed content of each body part is returned (as a byte array) in the
 * <code>entity</code> property of the returned <code>BodyPart}</code> instance, and
 * the application can decide what further steps are needed based on the
 * headers included in that body part.  The simplest technique is to examine
 * the received <code>BodyPart</code>, and then call the <code>getEntityAs()</code>
 * method once you know which implementation class you would prefer.</p>
 *
 * <h4>Not (Yet) Supported MIME Multipart Capabilities</h4>
 *
 * <p>The following list of general MIME MultiPart features is NOT (yet, in
 * most cases) supported:</p>
 * <ul>
 * <li>The <code>charset</code> parameter on the <code>Content-Type</code> header
 *     field.  Currently, the hard coded charset is FIXME.  [RFC2045#5.2]</li>
 * <li>The <code>Content-Transfer-Encoding</code> header field.  [RFC2045#6]</li>
 * <li>The <code>message/*</code> family of content types.</li>
 * </ul>
 */
package org.glassfish.jersey.media.multipart;
