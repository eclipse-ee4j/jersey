/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */
package org.glassfish.jersey.test.artifacts;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

class TestResult {
    private List<String> oks = new LinkedList<>();
    private List<String> exceptions = new LinkedList<>();

    MessageBuilder ok() {
        return new MessageBuilder(oks);
    }

    MessageBuilder exception() {
        return new MessageBuilder(exceptions);
    }
    boolean result() {
        for (String ok : oks) {
            System.out.append("(pass) ").print(ok);
        }

        for (String exception : exceptions) {
            System.out.append("\u001b[31;1m(FAIL) ").append(exception).print("\u001b[0m");
        }

        return exceptions.isEmpty();
    }

    public TestResult append(TestResult result) throws IOException {
        oks.addAll(result.oks);
        exceptions.addAll(result.exceptions);
        return this;
    }

    class MessageBuilder implements Appendable {
        final List<String> list;
        final StringBuilder builder = new StringBuilder();

        MessageBuilder(List<String> list) {
            this.list = list;
        }

        @Override
        public MessageBuilder append(CharSequence csq) throws IOException {
            builder.append(csq);
            return this;
        }

        public MessageBuilder append(int i) throws IOException {
            builder.append(i);
            return this;
        }

        @Override
        public MessageBuilder append(CharSequence csq, int start, int end) throws IOException {
            builder.append(csq, start, end);
            return this;
        }

        @Override
        public MessageBuilder append(char c) throws IOException {
            builder.append(c);
            return this;
        }

        public TestResult println(String message) {
            builder.append(message).append('\n');
            list.add(builder.toString());
            return TestResult.this;
        }
    }
}
