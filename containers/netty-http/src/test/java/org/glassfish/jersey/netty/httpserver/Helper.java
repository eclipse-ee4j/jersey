/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import org.glassfish.jersey.netty.httpserver.HugeEntityTest.TestEntity;

public class Helper {

  public static final int ONE_MB_IN_BYTES = 1024 * 1024; // 1M
  public static final long TWENTY_GB_IN_BYTES = 20L * 1024L * 1024L * 1024L; // 20G seems sufficient

  public static long drainAndCountInputStream(InputStream in) throws IOException {
      long totalBytesRead = 0L;

      byte[] buffer = new byte[ONE_MB_IN_BYTES];
      int read;
      do {
        read = in.read(buffer);
        if (read > 0) {
          totalBytesRead += read;
        }
      } while (read != -1);

      return totalBytesRead;
  }

  /**
   * Utility writer that generates that many zero bytes as given by the input entity size field.
   */
  public static class TestEntityWriter implements MessageBodyWriter<TestEntity> {

      @Override
      public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
          return type == TestEntity.class;
      }

      @Override
      public long getSize(TestEntity t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
          return -1; // no matter what we return here, the output will get chunk-encoded
      }

      @Override
      public void writeTo(TestEntity t,
                          Class<?> type,
                          Type genericType,
                          Annotation[] annotations,
                          MediaType mediaType,
                          MultivaluedMap<String, Object> httpHeaders,
                          OutputStream entityStream) throws IOException, WebApplicationException {

          final byte[] buffer = new byte[Helper.ONE_MB_IN_BYTES];
          final long bufferCount = t.size / Helper.ONE_MB_IN_BYTES;
          final int remainder = (int) (t.size % Helper.ONE_MB_IN_BYTES);

          for (long b = 0; b < bufferCount; b++) {
              entityStream.write(buffer);
          }

          if (remainder > 0) {
              entityStream.write(buffer, 0, remainder);
          }
      }
  }


}
