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

package org.glassfish.jersey.oauth1.signature;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.oauth1.signature.internal.LocalizationMessages;

/**
 * An OAuth signature method that implements RSA-SHA1.
 *
 * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public final class RsaSha1Method implements OAuth1SignatureMethod {

    private static final Logger LOGGER = Logger.getLogger(RsaSha1Method.class.getName());

    public static final String NAME = "RSA-SHA1";

    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    private static final String KEY_TYPE = "RSA";

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE";

    @Override
    public String name() {
        return NAME;
    }

    /**
     * Generates the RSA-SHA1 signature of OAuth request elements.
     *
     * @param baseString the combined OAuth elements to sign.
     * @param secrets the secrets object containing the private key for generating the signature.
     * @return the OAuth signature, in base64-encoded form.
     * @throws InvalidSecretException if the supplied secret is not valid.
     */
    @Override
    public String sign(final String baseString, final OAuth1Secrets secrets) throws InvalidSecretException {

        final Signature signature;
        try {
            signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        } catch (final NoSuchAlgorithmException nsae) {
            throw new IllegalStateException(nsae);
        }

        byte[] decodedPrivateKey;
        try {
            decodedPrivateKey = Base64.decode(secrets.getConsumerSecret());
        } catch (final IOException ioe) {
            throw new InvalidSecretException(LocalizationMessages.ERROR_INVALID_CONSUMER_SECRET(ioe));
        }

        final KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(KEY_TYPE);
        } catch (final NoSuchAlgorithmException nsae) {
            throw new IllegalStateException(nsae);
        }

        final EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);

        final RSAPrivateKey rsaPrivateKey;
        try {
            rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (final InvalidKeySpecException ikse) {
            throw new IllegalStateException(ikse);
        }

        try {
            signature.initSign(rsaPrivateKey);
        } catch (final InvalidKeyException ike) {
            throw new IllegalStateException(ike);
        }

        try {
            signature.update(baseString.getBytes());
        } catch (final SignatureException se) {
            throw new IllegalStateException(se);
        }

        final byte[] rsasha1;

        try {
            rsasha1 = signature.sign();
        } catch (final SignatureException se) {
            throw new IllegalStateException(se);
        }

        return Base64.encode(rsasha1);
    }

    /**
     * Verifies the RSA-SHA1 signature of OAuth request elements.
     *
     * @param elements OAuth elements signature is to be verified against.
     * @param secrets the secrets object containing the public key for verifying the signature.
     * @param signature base64-encoded OAuth signature to be verified.
     * @throws InvalidSecretException if the supplied secret is not valid.
     */
    @Override
    public boolean verify(final String elements, final OAuth1Secrets secrets, final String signature)
            throws InvalidSecretException {

        final Signature sig;

        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        } catch (final NoSuchAlgorithmException nsae) {
            throw new IllegalStateException(nsae);
        }

        RSAPublicKey rsaPubKey = null;

        final String tmpkey = secrets.getConsumerSecret();
        if (tmpkey.startsWith(BEGIN_CERT)) {
            try {
                Certificate cert = null;
                final ByteArrayInputStream bais = new ByteArrayInputStream(tmpkey.getBytes());
                final BufferedInputStream bis = new BufferedInputStream(bais);
                final CertificateFactory certfac = CertificateFactory.getInstance("X.509");
                while (bis.available() > 0) {
                    cert = certfac.generateCertificate(bis);
                }
                rsaPubKey = (RSAPublicKey) cert.getPublicKey();
            } catch (final Exception ex) {
                LOGGER.log(Level.SEVERE, LocalizationMessages.ERROR_CANNOT_OBTAIN_PUBLIC_KEY(), ex);
                return false;
            }
        }

        final byte[] decodedSignature;
        try {
            decodedSignature = Base64.decode(signature);
        } catch (final IOException e) {
            return false;
        }

        try {
            sig.initVerify(rsaPubKey);
        } catch (final InvalidKeyException ike) {
            throw new IllegalStateException(ike);
        }

        try {
            sig.update(elements.getBytes());
        } catch (final SignatureException se) {
            throw new IllegalStateException(se);
        }

        try {
            return sig.verify(decodedSignature);
        } catch (final SignatureException se) {
            throw new IllegalStateException(se);
        }
    }
}
