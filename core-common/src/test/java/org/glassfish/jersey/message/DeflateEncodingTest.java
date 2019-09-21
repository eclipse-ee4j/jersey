/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;

import javax.inject.Provider;

import org.junit.Test;

/**
 * @author Martin Matula
 */
public class DeflateEncodingTest extends AbstractEncodingTest {

    private static class DummyConfiguration implements Configuration, Provider<Configuration> {

        private final HashMap<String, Object> properties;

        DummyConfiguration(boolean noZLib) {
            properties = new HashMap<>();
            properties.put(MessageProperties.DEFLATE_WITHOUT_ZLIB, noZLib);
        }

        @Override
        public RuntimeType getRuntimeType() {
            return null;
        }

        @Override
        public Object getProperty(String name) {
            return properties.get(name);
        }

        @Override
        public Collection<String> getPropertyNames() {
            return properties.keySet();
        }

        @Override
        public boolean isEnabled(Feature feature) {
            return false;
        }

        @Override
        public boolean isEnabled(Class<? extends Feature> featureClass) {
            return false;
        }

        @Override
        public boolean isRegistered(Object provider) {
            return false;
        }

        @Override
        public boolean isRegistered(Class<?> providerClass) {
            return false;
        }
        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public Configuration get() {
            return this;
        }

        @Override
        public Map<Class<?>, Integer> getContracts(Class<?> componentClass) {
            return Collections.emptyMap();
        }

        @Override
        public Set<Class<?>> getClasses() {
            return Collections.emptySet();
        }

        @Override
        public Set<Object> getInstances() {
            return Collections.emptySet();
        }
    }

    @Test
    public void testEncodeZLib() throws IOException {
        test(new TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new DeflateEncoder(new DummyConfiguration(false)).encode("deflate", stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) {
                return new InflaterInputStream(stream);
            }
        });
    }

    @Test
    public void testEncodeNoZLib() throws IOException {
        test(new TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new DeflateEncoder(new DummyConfiguration(true)).encode("deflate", stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) {
                return new InflaterInputStream(stream, new Inflater(true));
            }
        });
    }

    @Test
    public void testDecodeZLib() throws IOException {
        test(new TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new DeflaterOutputStream(stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return new DeflateEncoder(new DummyConfiguration(false)).decode("deflate", stream);
            }
        });
    }

    @Test
    public void testDecodeNoZLib() throws IOException {
        test(new TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new DeflaterOutputStream(stream, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return new DeflateEncoder(new DummyConfiguration(false)).decode("deflate", stream);
            }
        });
    }

    @Test
    public void testEncodeDecodeZLib() throws IOException {
        test(new TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new DeflateEncoder(new DummyConfiguration(false)).encode("deflate", stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return new DeflateEncoder(new DummyConfiguration(false)).decode("deflate", stream);
            }
        });
    }

    @Test
    public void testEncodeDecodeNoZLib() throws IOException {
        test(new TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new DeflateEncoder(new DummyConfiguration(true)).encode("deflate", stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return new DeflateEncoder(new DummyConfiguration(false)).decode("deflate", stream);
            }
        });
    }
}
