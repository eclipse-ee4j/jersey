/*
 * Copyright (c) 2021 Payara Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.grizzly2.httpserver.test.tools;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * Creates a new keystore in memory.
 * This keystore contains just a private key and a self-signed certificate valid for two days.
 *
 * @author David Matejcek
 */
public class KeyStoreManager {

    private static final String KEYSTORE_PASSWORD = "";
    private final KeyStore keyStore;
    private final byte[] keyStoreBytes;

    /**
     * @param hostname - hostname, used for CN value of the self-signed certificate
     */
    public KeyStoreManager(final String hostname) {
        try {
            this.keyStore = KeyStore.getInstance("PKCS12");
            this.keyStore.load(null);
            this.keyStoreBytes = generatePrivateKeyAndCertificate(hostname, this.keyStore);
        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize the keystore.", e);
        }
    }


    /**
     * @return {@link KeyStore}
     */
    public KeyStore getKeyStore() {
        return this.keyStore;
    }


    /**
     * @return the key store serialized to a byte array
     */
    public byte[] getKeyStoreBytes() {
        return this.keyStoreBytes;
    }


    /**
     * @return the key store password
     */
    public String getKeyStorePassword() {
        return KEYSTORE_PASSWORD;
    }


    private static byte[] generatePrivateKeyAndCertificate(final String hostname, final KeyStore keyStore) {
        try {
            final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, SecureRandom.getInstance("SHA1PRNG"));
            final KeyPair keyPair = generator.generateKeyPair();
            final PrivateKey privateKey = keyPair.getPrivate();
            final PublicKey publicKey = keyPair.getPublic();

            final BigInteger serial = new BigInteger(256, new Random(System.currentTimeMillis()));
            final Instant validFrom = Instant.now().minusSeconds(60L);
            final Instant validTo = validFrom.plus(2, ChronoUnit.DAYS);

            final ASN1Sequence pubSeq = ASN1Sequence.getInstance(publicKey.getEncoded());
            final SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(pubSeq.getEncoded());
            final X500Name name = new X500Name(
                "CN=" + hostname + ", OU=Jersey Container, O=Eclipse Foundation, L=Brussels, ST=Belgium, C=BE");
            final X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                name, serial, Date.from(validFrom), Date.from(validTo), name, info);
            final JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA512withRSA");
            final ContentSigner signer = signerBuilder.build(privateKey);

            final X509CertificateHolder cHolder = builder.build(signer);
            final X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(cHolder);

            keyStore.setKeyEntry(hostname, privateKey, KEYSTORE_PASSWORD.toCharArray(),
                new Certificate[] {certificate});
            return toBytes(keyStore);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not initialize the keystore", e);
        }
    }


    private static byte[] toBytes(final KeyStore keyStore) throws Exception {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(1024))  {
            keyStore.store(os, new char[0]);
            return os.toByteArray();
        }
    }
}
