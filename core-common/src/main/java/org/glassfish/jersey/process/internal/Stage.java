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

package org.glassfish.jersey.process.internal;

import java.util.function.Function;

/**
 * Data processing stage that can be used to create dynamic data processing chains.
 * <p>
 * An stage is a stateless data processing unit that returns a
 * {@link Continuation processing continuation}.
 * </p>
 *
 * @param <DATA> processed data type.
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface Stage<DATA> {

    /**
     * Data processing stage continuation.
     * <p>
     * A continuation of a processing stage is a processing result represented by an
     * ({@link #hasNext() optional}) stage that should be invoked {@link #next() next}
     * and the processed data {@link #result() result}.
     * </p>
     *
     * @param <DATA> processed data type.
     */
    public static final class Continuation<DATA> {
        private final DATA result;
        private final Stage<DATA> next;

        /**
         * Create a new continuation instance.
         *
         * @param result processed data.
         * @param next   next processing stage.
         */
        Continuation(final DATA result, final Stage<DATA> next) {
            this.result = result;
            this.next = next;
        }

        /**
         * Create a continuation from the processed data result and the stage
         * to be invoked next.
         *
         * @param <DATA> processed data type.
         * @param result data processing result.
         * @param next   stage to be invoked next.
         * @return a continuation with the supplied stage to be invoked
         *         {@link Stage.Continuation#next() next} in the processing chain
         *         and the supplied processing result.
         */
        public static <DATA> Continuation<DATA> of(final DATA result, Stage<DATA> next) {
            return new Continuation<DATA>(result, next);
        }

        /**
         * Create a terminal continuation from the processed data result.
         *
         * @param <DATA> processed data type.
         * @param result data processing result.
         * @return terminal continuation with no {@link Stage.Continuation#next() next}
         *         stage in the processing chain and the supplied processing result.
         */
        public static <DATA> Continuation<DATA> of(final DATA result) {
            return new Continuation<DATA>(result, null);
        }

        /**
         * Get the data processing result.
         *
         * @return data processing result.
         */
        public DATA result() {
            return result;
        }

        /**
         * Get the stage to be invoked next or {@code null} if no next stage is
         * {@link #hasNext() present}.
         *
         * @return the stage to be invoked next or {@code null} if not present.
         */
        public Stage<DATA> next() {
            return next;
        }

        /**
         * Check if there is a next stage present in the continuation.
         * <p>
         * The absence of a next stage in the continuation indicates that the data processing
         * reached a terminal stage and the {@link #result() result} of the continuation represents
         * the final result of the whole processing chain.
         * </p>
         *
         * @return {@code true} if there is a next stage present in the continuation,
         *         {@code false} otherwise.
         */
        public boolean hasNext() {
            return next != null;
        }
    }

    /**
     * Linear stage chain builder.
     *
     * @param <DATA> processed data type.
     */
    public static interface Builder<DATA> {

        /**
         * Add a transformation function as a next stage to the stage chain.
         * <p>
         * The order of the {@code to(...)} method invocations matches the order
         * of the stage execution at runtime.
         * </p>
         *
         * @param transformation a transformation function to be added as a next
         *                       stage to the stage chain.
         * @return updated builder instance.
         */
        public Builder<DATA> to(Function<DATA, DATA> transformation);

        /**
         * Add a new {@link ChainableStage chainable stage} as a next stage to the
         * stage chain.
         * <p>
         * The order of the {@code to(...)} method invocations matches the order
         * of the stage execution at runtime.
         * A subsequent call to a {@code to(...)} method  will automatically invoke the
         * {@link ChainableStage#setDefaultNext(Stage)} method on the chainable
         * stage.
         * </p>
         *
         * @param stage a chainable stage to be added as a next
         *                 stage to the stage chain.
         * @return updated builder instance.
         */
        public Builder<DATA> to(ChainableStage<DATA> stage);

        /**
         * Build a stage chain.
         *
         * @return built acceptor chain.
         */
        public Stage<DATA> build();

        /**
         * Add a terminal stage to the stage chain and build the chain.
         *
         * @param terminal last stage to be added to the stage chain.
         * @return built stage chain.
         */
        public Stage<DATA> build(Stage<DATA> terminal);
    }

    /**
     * Performs a data processing task and returns the processed data together with
     * a {@link Continuation processing continuation}.
     *
     * @param data data to be transformed.
     * @return a processing continuation.
     */
    public Continuation<DATA> apply(DATA data);
}
