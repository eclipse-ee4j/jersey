/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.util.runner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;

import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;

import org.glassfish.jersey.Beta;

/**
 * Custom implementation of a JUnit {@link Runner} that allows parameterized
 * tests to run in parallel. This runner will probably
 * be merged into {@link ConcurrentRunner} in the future.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Beta
public class ConcurrentParameterizedRunner extends BlockJUnit4ClassRunner {

    public final int FINISH_WAIT_CYCLE_MS = 2000;
    public final int TEST_THREADS = 124;

    private static final Logger LOGGER = Logger.getLogger(ConcurrentParameterizedRunner.class.getName());

    private final ExecutorService executor = Executors.newFixedThreadPool(TEST_THREADS);

    /**
     * Create a new runner for given test class.
     *
     * @param clazz test class
     * @throws Throwable
     */
    public ConcurrentParameterizedRunner(Class<?> clazz) throws Throwable {
        super(clazz);
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
    }

    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        notifier.fireTestStarted(describeChild(method));

        final Object testInstance;
        try {

            // get the test parameter iterator first
            final List<FrameworkMethod> parameterMethods = getTestClass().getAnnotatedMethods(Parameterized.Parameters.class);
            final Iterable<Object[]> parameters = (Iterable<Object[]>) parameterMethods.get(0).getMethod().invoke(null);

            // then create the test instance
            testInstance = super.createTest();

            // now run the before methods
            List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(Before.class);
            for (FrameworkMethod before : befores) {
                before.getMethod().invoke(testInstance);
            }

            // and launch as meny test method invocations as many parameters is available
            final Iterator<Object[]> paramIterator = parameters.iterator();
            final Method javaTestMethod = method.getMethod();

            final AtomicInteger submitted = new AtomicInteger(0);

            while (paramIterator.hasNext()) {

                final Object[] javaMethodArgs = paramIterator.next();
                submitted.incrementAndGet();
                executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            javaTestMethod.invoke(testInstance, javaMethodArgs);
                        } catch (IllegalAccessException ex) {
                            notifier.fireTestFailure(new Failure(describeChild(method), ex));
                        } catch (IllegalArgumentException ex) {
                            notifier.fireTestFailure(new Failure(describeChild(method), ex));
                        } catch (InvocationTargetException ex) {
                            notifier.fireTestFailure(new Failure(describeChild(method), ex));
                        } finally {
                            submitted.decrementAndGet();
                        }
                    }
                });
            }

            // wait until everything is done
            while (submitted.intValue() > 0) {
                LOGGER.info(String.format("Waiting for %d requests to finish...%n", submitted.intValue()));
                try {
                    Thread.sleep(FINISH_WAIT_CYCLE_MS);
                } catch (InterruptedException e) {
                }
            }

            // and launch the after party..
            List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(After.class);
            for (FrameworkMethod after : afters) {
                after.getMethod().invoke(testInstance);
            }
        } catch (Exception ex) {
            notifier.fireTestFailure(new Failure(describeChild(method), ex));
            return;
        }
        notifier.fireTestFinished(describeChild(method));
    }
}
