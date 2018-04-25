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

package org.glassfish.jersey.jdk.connector.internal;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes and reads data to and from a socket. Only one {@link #write(ByteBuffer,
 * org.glassfish.jersey.jdk.connector.internal.CompletionHandler)}
 * method call can be processed at a time. Only one {@link #_read(ByteBuffer)} operation is supported at a time,
 * another one is started only after the previous one has completed. Blocking in {@link #onRead(Object)}
 * or {@link #onConnect()} method will result in data not being read from a socket until these methods have completed.
 *
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class TransportFilter extends Filter<ByteBuffer, ByteBuffer, Void, ByteBuffer> {

    private static final Logger LOGGER = Logger.getLogger(TransportFilter.class.getName());
    private static final AtomicInteger openedConnections = new AtomicInteger(0);
    private static final ScheduledExecutorService connectionCloseScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("jdk-connector-container-idle-timeout");
        thread.setDaemon(true);
        return thread;
    });

    private static volatile AsynchronousChannelGroup channelGroup;
    private static volatile ScheduledFuture<?> closeWaitTask;

    /**
     * {@link ThreadPoolConfig} current {@link #channelGroup} has been created with.
     */
    private static volatile ThreadPoolConfig currentThreadPoolConfig;
    /**
     * Idle timeout that will be used when closing current {@link #channelGroup}
     */
    private static volatile Integer currentContainerIdleTimeout;

    private final int inputBufferSize;
    private final ThreadPoolConfig threadPoolConfig;
    private final int containerIdleTimeout;

    private volatile AsynchronousSocketChannel socketChannel;

    /**
     * Constructor.
     * <p/>
     * If the channel group is not active (all connections have been closed and the shutdown timeout is running) and a new
     * transport is created with tread pool configuration different from the one of the current thread pool, the current
     * thread pool will be shut down and a new one created with the new configuration.
     *
     * @param inputBufferSize      size of buffer to be allocated for reading data from a socket.
     * @param threadPoolConfig     thread pool configuration used for creating thread pool.
     * @param containerIdleTimeout idle time after which the shared thread pool will be destroyed.
     */
    TransportFilter(int inputBufferSize, ThreadPoolConfig threadPoolConfig, int containerIdleTimeout) {
        super(null);
        this.inputBufferSize = inputBufferSize;
        this.threadPoolConfig = threadPoolConfig;
        this.containerIdleTimeout = containerIdleTimeout;
    }

    @Override
    void write(ByteBuffer data,
               final org.glassfish.jersey.jdk.connector.internal.CompletionHandler<ByteBuffer> completionHandler) {
        socketChannel.isOpen();
        socketChannel.write(data, data, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                if (buffer.hasRemaining()) {
                    write(buffer, completionHandler);
                    return;
                }
                completionHandler.completed(buffer);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
                completionHandler.failed(exc);
            }
        });
    }

    @Override
    void close() {
        if (socketChannel == null || !socketChannel.isOpen()) {
            return;
        }
        try {
            socketChannel.close();
        } catch (IOException e) {
            LOGGER.log(Level.INFO, LocalizationMessages.TRANSPORT_CONNECTION_NOT_CLOSED(), e);
        }
        synchronized (TransportFilter.class) {
            openedConnections.decrementAndGet();
            if (openedConnections.get() == 0 && channelGroup != null) {
                scheduleClose();
            }
        }
    }

    @Override
    void startSsl() {
        onSslHandshakeCompleted();
    }

    @Override
    public void handleConnect(SocketAddress serverAddress, Filter upstreamFilter) {
        this.upstreamFilter = upstreamFilter;

        try {
            synchronized (TransportFilter.class) {
                updateThreadPoolConfig();
                initializeChannelGroup();
                socketChannel = AsynchronousSocketChannel.open(channelGroup);
                openedConnections.incrementAndGet();
            }
        } catch (IOException e) {
            onError(e);
            return;
        }

        socketChannel.connect(serverAddress, null, new CompletionHandler<Void, Void>() {

            @Override
            public void completed(Void result, Void nothing) {
                final ByteBuffer inputBuffer = ByteBuffer.allocate(inputBufferSize);
                onConnect();
                _read(inputBuffer);
            }

            @Override
            public void failed(Throwable exc, Void nothing) {
                onError(exc);

                try {
                    socketChannel.close();
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, LocalizationMessages.TRANSPORT_CONNECTION_NOT_CLOSED(), exc.getMessage());
                }
            }
        });
    }

    private void updateThreadPoolConfig() {

        // the channel group is active, no change in configuration
        if (openedConnections.get() != 0) {
            return;
        }

        // check if the new configuration is different from the one of the current container
        if (!threadPoolConfig.equals(currentThreadPoolConfig) || containerIdleTimeout != currentContainerIdleTimeout) {

            currentThreadPoolConfig = threadPoolConfig;
            currentContainerIdleTimeout = containerIdleTimeout;

            if (channelGroup == null) {
                // the channel group has not been initialized (this is a first client) - no need to shut it down
                return;
            }

            closeWaitTask.cancel(true);
            closeWaitTask = null;
            channelGroup.shutdown();
            channelGroup = null;
        }
    }

    private void initializeChannelGroup() throws IOException {
        if (closeWaitTask != null) {
            closeWaitTask.cancel(true);
            closeWaitTask = null;
        }

        if (channelGroup == null) {
            ThreadFactory threadFactory = threadPoolConfig.getThreadFactory();
            if (threadFactory == null) {
                threadFactory = new TransportThreadFactory(threadPoolConfig);
            }

            ExecutorService executor;
            if (threadPoolConfig.getQueue() != null) {
                executor = new QueuingExecutor(threadPoolConfig.getCorePoolSize(), threadPoolConfig.getMaxPoolSize(),
                        threadPoolConfig.getKeepAliveTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS,
                        threadPoolConfig.getQueue(), false, threadFactory);
            } else {
                int taskQueueLimit = threadPoolConfig.getQueueLimit();
                if (taskQueueLimit == -1) {
                    taskQueueLimit = Integer.MAX_VALUE;
                }

                executor = new QueuingExecutor(threadPoolConfig.getCorePoolSize(), threadPoolConfig.getMaxPoolSize(),
                        threadPoolConfig.getKeepAliveTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS,
                        new LinkedBlockingDeque<>(taskQueueLimit), true, threadFactory);
            }

            // Thread pool is owned by the channel group and will be shut down when channel group is shut down
            channelGroup = AsynchronousChannelGroup.withCachedThreadPool(executor, threadPoolConfig.getCorePoolSize());
        }
    }

    private void _read(final ByteBuffer inputBuffer) {
        /**
         * It must be checked that the channel has not been closed by {@link #close()} method.
         */
        if (!socketChannel.isOpen()) {
            return;
        }

        socketChannel.read(inputBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer bytesRead, Void result) {
                // connection closed by the server
                if (bytesRead == -1) {
                    // close will set TransportFilter.this.upstreamFilter to null
                    Filter upstreamFilter = TransportFilter.this.upstreamFilter;
                    close();
                    upstreamFilter.onConnectionClosed();
                    return;
                }

                inputBuffer.flip();
                onRead(inputBuffer);
                inputBuffer.compact();
                _read(inputBuffer);
            }

            @Override
            public void failed(Throwable exc, Void result) {
                /**
                 * Reading from the channel will fail if it is closing. In such cases {@link java.nio.channels
                 * .AsynchronousCloseException}
                 * is thrown. This should not be logged and no action undertaken.
                 */
                if (exc instanceof AsynchronousCloseException) {
                    return;
                }

                onError(exc);
            }
        });
    }

    private void scheduleClose() {
        closeWaitTask = connectionCloseScheduler.schedule(() -> {
            synchronized (TransportFilter.class) {
                if (closeWaitTask == null) {
                    return;
                }
                channelGroup.shutdown();
                channelGroup = null;
                closeWaitTask = null;
            }
        }, currentContainerIdleTimeout, TimeUnit.MILLISECONDS);
    }

    /**
     * A default thread factory that gets used if {@link ThreadPoolConfig#getThreadFactory()}
     * is not specified.
     */
    private static class TransportThreadFactory implements ThreadFactory {

        private static final String THREAD_NAME_BASE = "jdk-connector-";
        private static final AtomicInteger threadCounter = new AtomicInteger(0);

        private final ThreadPoolConfig threadPoolConfig;

        TransportThreadFactory(ThreadPoolConfig threadPoolConfig) {
            this.threadPoolConfig = threadPoolConfig;
        }

        @Override
        public Thread newThread(Runnable r) {
            final Thread thread = new Thread(r);
            thread.setName(THREAD_NAME_BASE + threadCounter.incrementAndGet());
            thread.setPriority(threadPoolConfig.getPriority());
            thread.setDaemon(threadPoolConfig.isDaemon());

            try {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        if (threadPoolConfig.getInitialClassLoader() == null) {
                            thread.setContextClassLoader(this.getClass().getClassLoader());
                        } else {
                            thread.setContextClassLoader(threadPoolConfig.getInitialClassLoader());
                        }
                        return null;
                    }
                });
            } catch (Throwable t) {
                // just log - client still can work without setting context class loader
                LOGGER.log(Level.WARNING, LocalizationMessages.TRANSPORT_SET_CLASS_LOADER_FAILED(), t);
            }

            return thread;
        }
    }

    /**
     * A thread pool executor that prefers creating new worker threads over queueing tasks until the maximum poll size
     * has been reached, after which it will start queueing tasks.
     */
    private static class QueuingExecutor extends ThreadPoolExecutor {

        private final Queue<Runnable> taskQueue;
        private final boolean threadSafeQueue;

        /**
         * Constructor.
         *
         * @param threadSafeQueue indicates if {@link #taskQueue} is thread safe or not.
         */
        QueuingExecutor(int corePoolSize,
                        int maximumPoolSize,
                        long keepAliveTime,
                        TimeUnit unit,
                        Queue<Runnable> taskQueue,
                        boolean threadSafeQueue,
                        ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new HandOffQueue(taskQueue, threadSafeQueue),
                    threadFactory);
            this.taskQueue = taskQueue;
            this.threadSafeQueue = threadSafeQueue;
        }

        /**
         * Submit a task for execution, if the maximum thread limit has been reached and all the threads are occupied,
         * enqueue the task. The task is not executed by the current thread, but by a thread from the thread pool.
         *
         * @param task to be executed.
         */
        @Override
        public void execute(Runnable task) {
            try {
                super.execute(task);
            } catch (RejectedExecutionException e) {

                /* execution has been rejected either because the executor has been shut down or all worker threads are
                 * busy - check the former one
                 */
                if (isShutdown()) {
                    throw new RejectedExecutionException(LocalizationMessages.TRANSPORT_EXECUTOR_CLOSED(), e);
                }

                /* All threads are occupied, try enqueuing the task.
                 * Each worker thread checks the queue after it has finished executing its task.
                 */
                if (threadSafeQueue) {
                    if (!taskQueue.offer(task)) {
                        throw new RejectedExecutionException(LocalizationMessages.TRANSPORT_EXECUTOR_QUEUE_LIMIT_REACHED(), e);
                    }
                } else {
                    synchronized (taskQueue) {
                        if (!taskQueue.offer(task)) {
                            throw new RejectedExecutionException(LocalizationMessages.TRANSPORT_EXECUTOR_QUEUE_LIMIT_REACHED(),
                                    e);
                        }
                    }
                }

                /**
                 * There is a small time interval between a worker thread checks {@link #taskQueue} and it starts to block
                 * waiting for a new tasks to be submitted (Ideally checking that the {@link #taskQueue} is empty and starting to
                 * block at the task hand off queue would be atomic). This can be detected by the situation when a thread
                 * submitting
                 * a new tasks has been rejected, but not all worker threads are active (However this does not indicate
                 * exclusively
                 * the problematic situation).
                 */
                if (getActiveCount() < getMaximumPoolSize()) {
                    /*
                     * There is no guarantee that the same tasks that has been enqueued above will be dequeued,
                     * but trying to execute one arbitrary task by everyone in this situation is enough to clear the queue.
                     */
                    Runnable dequeuedTask;
                    if (threadSafeQueue) {
                        dequeuedTask = taskQueue.poll();
                    } else {
                        synchronized (taskQueue) {
                            dequeuedTask = taskQueue.poll();
                        }
                    }

                    // check if the task has not been consumed by a worker thread after all
                    if (dequeuedTask != null) {
                        execute(dequeuedTask);
                    }
                }
            }
        }

        /**
         * Synchronous queue that tries to empty {@link #taskQueue} before it blocks waiting for new tasks to be submitted.
         * It is passed to {@link ThreadPoolExecutor}, where it is used used to hand off tasks from task-submitting
         * thread to worker threads.
         */
        private static class HandOffQueue extends SynchronousQueue<Runnable> {

            private static final long serialVersionUID = -1607064661828834847L;
            private final Queue<Runnable> taskQueue;
            private final boolean threadSafeQueue;

            private HandOffQueue(Queue<Runnable> taskQueue, boolean threadSafeQueue) {
                this.taskQueue = taskQueue;
                this.threadSafeQueue = threadSafeQueue;
            }

            @Override
            public Runnable take() throws InterruptedException {
                // try to empty the task queue
                Runnable task;
                if (threadSafeQueue) {
                    task = taskQueue.poll();
                } else {
                    synchronized (taskQueue) {
                        task = taskQueue.poll();
                    }
                }
                if (task != null) {
                    return task;
                }

                // block and wait for a task to be submitted
                return super.take();
            }

            @Override
            public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
                // try to empty the task queue
                Runnable task;
                if (threadSafeQueue) {
                    task = taskQueue.poll();
                } else {
                    synchronized (taskQueue) {
                        task = taskQueue.poll();
                    }
                }
                if (task != null) {
                    return task;
                }

                // block and wait for a task to be submitted
                return super.poll(timeout, unit);
            }
        }
    }
}
