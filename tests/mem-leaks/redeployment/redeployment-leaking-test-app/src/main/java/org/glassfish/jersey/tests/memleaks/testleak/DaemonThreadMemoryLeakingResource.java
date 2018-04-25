/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.memleaks.testleak;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import javax.inject.Singleton;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Resource that causes {@link OutOfMemoryError} exception upon repetitive call of {@link #invoke(int)} of an application that is
 * being redeployed.
 * <p/>
 * The purpose of this resource (and the app) is to test whether the memory leaking infrastructure for redeployment scenarios
 * works.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
@Path("/")
@Singleton
public class DaemonThreadMemoryLeakingResource {

    @POST
    @Path("invoke")
    public String invoke(@DefaultValue("1048576") @QueryParam("size") final int size) {

        final Future<?> future = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).build())
                .submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("Running a Thread!");
                            final int mbytes = size / (1024 * 1024);
                            final byte[][] bytes = new byte[mbytes][];
                            for (int i = 1; i <= mbytes; ++i) {
                                bytes[i - 1] = new byte[1024 * 1024];
                                System.out.println("Allocated: " + i + "MB!");
                            }

                            System.out.println("Memory allocated! Total: " + mbytes + "MB! Sleeping...");
                            for (int i = 0; i < 1000000; ++i) {
                                System.out.println("Thread " + Thread.currentThread() + " sleeping!");
                                Thread.sleep(10000);
                            }
                            System.out.println("Freeing: " + size + " of bytes. " + bytes);
                        } catch (InterruptedException e) {
                            throw new IllegalStateException("Thread Interrupted!", e);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            throw e;
                        }

                    }
                });

        System.out.println("Trying to allocate bytes from the thread itself.");
        final byte[] bytes = new byte[size];
        return "Future submitted: " + future + " bytes allocated: " + bytes;
    }

    @GET
    @Path("hello")
    @Produces("text/plain")
    public String helloWorld() {
        System.out.println("HELLO WORLD!");
        return "HELLO WORLD!";
    }

}
