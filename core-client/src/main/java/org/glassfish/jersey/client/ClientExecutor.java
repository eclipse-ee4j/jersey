/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Executor for client async processing and background task scheduling.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 * @since 2.26
 */
public interface ClientExecutor {
    /**
     * Submits a value-returning task for execution and returns a {@link Future} representing the pending results of the task.
     * The Future's {@code get()} method will return the task's result upon successful completion.
     *
     * @param task task to submit
     * @param <T>  task's return type
     * @return a {@code Future} representing pending completion of the task
     * @throws {@link java.util.concurrent.RejectedExecutionException} if the task cannot be scheduled for execution
     * @throws {@link NullPointerException} if the task is null
     */
    <T> Future<T> submit(Callable<T> task);

    /**
     * Submits a {@link Runnable} task for execution and returns a {@link Future} representing that task. The Future's {@code
     * get()} method will return the given result upon successful completion.
     *
     * @param task the task to submit
     * @return a  {@code Future} representing pending completion of the task
     * @throws {@link java.util.concurrent.RejectedExecutionException} if the task cannot be scheduled for execution
     * @throws {@link NullPointerException} if the task is null
     */
    Future<?> submit(Runnable task);

    /**
     * Submits a {@link Runnable} task for execution and returns a {@link Future} representing that task. The Future's {@code
     * get()} method will return the given result upon successful completion.
     *
     * @param task   the task to submit
     * @param result the result to return
     * @param <T>    result type
     * @return a {@code Future} representing pending completion of the task
     * @throws {@link java.util.concurrent.RejectedExecutionException} if the task cannot be scheduled for execution
     * @throws {@link NullPointerException} if the task is null
     */
    <T> Future<T> submit(Runnable task, T result);

    /**
     * Creates and executes a {@link ScheduledFuture} that becomes enabled after the given delay.
     *
     * @param callable the function to execute
     * @param delay    the time from now to delay execution
     * @param unit     the time unit of the delay parameter
     * @param <T>      return type of the function
     * @return a {@code ScheduledFuture} that can be used to extract result or cancel
     * @throws {@link java.util.concurrent.RejectedExecutionException} if the task cannot be scheduled for execution
     * @throws {@link NullPointerException} if callable is null
     */
    <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit unit);

    /**
     * Creates and executes a one-shot action that becomes enabled after the given delay.
     *
     * @param command the task to execute
     * @param delay   the time from now to delay execution
     * @param unit    the time unit of the daly parameter
     * @return a scheduledFuture representing pending completion of the task and whose {@code get()} method will return {@code
     * null} upon completion
     */
    ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);


}
