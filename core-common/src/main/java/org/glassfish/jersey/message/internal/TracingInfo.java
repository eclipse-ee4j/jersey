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

package org.glassfish.jersey.message.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects tracing messages for a request.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 * @since 2.3
 */
final class TracingInfo {

    private final List<Message> messageList = new ArrayList<Message>();

    /**
     * Format time duration in millis with accurate to 2 decimal places.
     *
     * @param duration time duration in nanos
     * @return Formatted duration in millis.
     */
    public static String formatDuration(final long duration) {
        if (duration == 0) {
            return " ----";
        } else {
            return String.format("%5.2f", (duration / 1000000.0));
        }
    }

    /**
     * Format time duration in millis with accurate to 2 decimal places.
     *
     * @param fromTimestamp start of time interval in nanos
     * @param toTimestamp   end of time interval in nanos
     * @return Formatted duration in millis.
     */
    public static String formatDuration(final long fromTimestamp, final long toTimestamp) {
        return formatDuration(toTimestamp - fromTimestamp);
    }

    /**
     * Format {@code value} from {@code top} value in percent with accurate to 2 decimal places.
     *
     * @param value part value according to top
     * @param top   100% value
     * @return Formatted value in percent.
     */
    public static String formatPercent(final long value, final long top) {
        if (value == 0) {
            return "  ----";
        } else {
            return String.format("%6.2f", 100.0 * value / top);
        }
    }

    /**
     * Returns all collected messages enhanced by time duration data.
     *
     * @return all formatted messages
     */
    public String[] getMessages() {
        // Format: EventCategory [duration / sinceRequestTime | duration/requestTime % ]
        // e.g.:   RI [ 3.88 / 8.93 ms | 1.37 %] message text

        final long fromTimestamp = messageList.get(0).getTimestamp() - messageList.get(0).getDuration();
        final long toTimestamp = messageList.get(messageList.size() - 1).getTimestamp();

        final String[] messages = new String[messageList.size()];

        for (int i = 0; i < messages.length; i++) {
            final Message message = messageList.get(i);
            final StringBuilder textSB = new StringBuilder();
            // event
            textSB.append(String.format("%-11s ", message.getEvent().category()));
            // duration
            textSB.append('[')
                    .append(formatDuration(message.getDuration()))
                    .append(" / ")
                    .append(formatDuration(fromTimestamp, message.getTimestamp()))
                    .append(" ms |")
                    .append(formatPercent(message.getDuration(), toTimestamp - fromTimestamp))
                    .append(" %] ");
            // text
            textSB.append(message.toString());
            messages[i] = textSB.toString();
        }
        return messages;
    }

    /**
     * Add other tracing message.
     *
     * @param message tracing message.
     */
    public void addMessage(final Message message) {
        messageList.add(message);
    }

    /**
     * A trace message.
     * It implements message formatting.
     */
    public static class Message {

        /**
         * Event type.
         */
        private final TracingLogger.Event event;
        /**
         * In nanos.
         */
        private final long duration;
        /**
         * In nanos.
         */
        private final long timestamp;
        /**
         * Already formatted text.
         */
        private final String text;

        /**
         * Create a new trace message.
         *
         * @param event trace event.
         * @param duration event duration.
         * @param args message arguments.
         */
        public Message(final TracingLogger.Event event, final long duration, final String[] args) {
            this.event = event;
            this.duration = duration;

            this.timestamp = System.nanoTime();
            if (event.messageFormat() != null) {
                this.text = String.format(event.messageFormat(), (Object[]) args);
            } else {
                final StringBuilder textSB = new StringBuilder();
                for (final String arg : args) {
                    textSB.append(arg).append(' ');
                }
                this.text = textSB.toString();
            }
        }

        private TracingLogger.Event getEvent() {
            return event;
        }

        private long getDuration() {
            return duration;
        }

        private long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return text;
        }
    }

}
