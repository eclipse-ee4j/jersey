/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.sse.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.fail;

import org.glassfish.jersey.internal.jsr166.Flow;
import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.SubscriberWhiteboxVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.TestException;
import org.testng.annotations.Test;

public class JerseyEventSinkWhiteBoxSubscriberTckTest extends SubscriberWhiteboxVerification<Object> {

    static final TestEnvironment env = new TestEnvironment(250);

    public JerseyEventSinkWhiteBoxSubscriberTckTest() {
        super(env);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void noopOnNextAfterClose() throws InterruptedException {
        WhiteboxTestStage stage = new WhiteboxTestStage(env, true);
        SubscriberPuppet puppet = stage.puppet();
        WhiteboxSubscriberProbe<Object> probe = stage.probe;
        JerseyEventSink eventSink = (JerseyEventSink)
                ((JerseyFlowAdapters.AdaptedSubscriber<Object>) stage.sub()).jerseySubscriber;
        puppet.triggerRequest(2);
        stage.expectRequest();
        probe.expectNext(stage.signalNext());
        probe.expectNext(stage.signalNext());

        puppet.triggerRequest(3000);
        eventSink.close();
        stage.expectCancelling();
        stage.signalNext();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void noopOnCompleteAfterClose() throws InterruptedException {
        WhiteboxTestStage stage = new WhiteboxTestStage(env, true);
        SubscriberPuppet puppet = stage.puppet();
        WhiteboxSubscriberProbe<Object> probe = stage.probe;
        JerseyEventSink eventSink = (JerseyEventSink)
                ((JerseyFlowAdapters.AdaptedSubscriber<Object>) stage.sub()).jerseySubscriber;
        puppet.triggerRequest(2);
        stage.expectRequest();
        probe.expectNext(stage.signalNext());
        probe.expectNext(stage.signalNext());

        puppet.triggerRequest(3000);
        eventSink.close();
        stage.sendCompletion();
        probe.expectCompletion();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void noopOnErrorAfterClose() throws InterruptedException {
        WhiteboxTestStage stage = new WhiteboxTestStage(env, true);
        SubscriberPuppet puppet = stage.puppet();
        WhiteboxSubscriberProbe<Object> probe = stage.probe;
        JerseyEventSink eventSink = (JerseyEventSink)
                ((JerseyFlowAdapters.AdaptedSubscriber<Object>) stage.sub()).jerseySubscriber;
        puppet.triggerRequest(2);
        stage.expectRequest();
        probe.expectNext(stage.signalNext());
        probe.expectNext(stage.signalNext());

        puppet.triggerRequest(3000);
        eventSink.close();

        TestException testException = new TestException("BOOM JERSEY!");

        stage.sendError(testException);
        probe.expectError(testException);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void cancelSubscriptionAfterClose() throws InterruptedException {
        WhiteboxTestStage stage = new WhiteboxTestStage(env, true);
        SubscriberPuppet puppet = stage.puppet();
        WhiteboxSubscriberProbe<Object> probe = stage.probe;
        JerseyEventSink eventSink = (JerseyEventSink)
                ((JerseyFlowAdapters.AdaptedSubscriber<Object>) stage.sub()).jerseySubscriber;
        puppet.triggerRequest(2);
        stage.expectRequest();
        probe.expectNext(stage.signalNext());
        probe.expectNext(stage.signalNext());

        puppet.triggerRequest(3000);
        eventSink.close();

        stage.expectCancelling();

        CompletableFuture<Void> cancelled2ndSubscription = new CompletableFuture<>();

        eventSink.onSubscribe(new Flow.Subscription() {
            @Override
            public void request(final long n) {

            }

            @Override
            public void cancel() {
                cancelled2ndSubscription.complete(null);
            }
        });

        try {
            cancelled2ndSubscription.get(env.defaultTimeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException | TimeoutException e) {
            fail("Cancel is expected on subscription on closed JerseyEventSink");
        }
    }

    @Override
    public Subscriber<Object> createSubscriber(final WhiteboxSubscriberProbe<Object> probe) {
        JerseyEventSink jerseyEventSink = new JerseyEventSink(null) {
            @Override
            public void onSubscribe(final Flow.Subscription subscription) {
                super.onSubscribe(subscription);
                probe.registerOnSubscribe(new SubscriberPuppet() {
                    @Override
                    public void triggerRequest(final long elements) {
                        subscription.request(elements);
                    }

                    @Override
                    public void signalCancel() {
                        subscription.cancel();
                    }
                });
            }

            @Override
            public void onNext(final Object item) {
                super.onNext(item);
                probe.registerOnNext(item);
            }

            @Override
            public void onError(final Throwable throwable) {
                super.onError(throwable);
                probe.registerOnError(throwable);
            }

            @Override
            public void onComplete() {
                super.onComplete();
                probe.registerOnComplete();
            }
        };
        return JerseyFlowAdapters.toSubscriber(jerseyEventSink);
    }

    @Override
    public String createElement(final int i) {
        return "test" + i;
    }
}
