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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Miroslav Fuksa
 */
public class OAuthBase64Test {

    @Test
    public void test() throws IOException {
        final String str = "Hello World123456789jhfsljkh347uweihd7834yfoiuhef5re4g54es35gf474w5/";
        final String encoded = Base64.encode(str.getBytes());
        final String encodedUtil = new String(java.util.Base64.getEncoder().encode(str.getBytes()));
        final String encodedUtilStr = java.util.Base64.getEncoder().encodeToString(str.getBytes());

        System.out.println(encoded);
        Assert.assertEquals(encoded, encodedUtil);
        Assert.assertEquals(encoded, encodedUtilStr);

        final String decoded = new String(Base64.decode(encoded));
        final String decodedUtil = new String(java.util.Base64.getDecoder().decode(encoded.getBytes()));
        final String decodedUtilStr = new String(java.util.Base64.getDecoder().decode(encoded.getBytes()));
        final String decodedUtilStr2 = new String(java.util.Base64.getDecoder().decode(encoded));

        Assert.assertEquals(decoded, decodedUtil);
        Assert.assertEquals(decoded, decodedUtilStr);
        Assert.assertEquals(decoded, decodedUtilStr2);
        Assert.assertEquals(decoded, str);
    }
}
