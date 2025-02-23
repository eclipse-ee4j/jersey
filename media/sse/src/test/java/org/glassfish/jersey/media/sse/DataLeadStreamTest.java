/*
 * Copyright (c) 2025 Markus KARG
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

package org.glassfish.jersey.media.sse;

import java.io.ByteArrayOutputStream;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Basic set of unit tests for {@link DataLeadStream}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 */
public class DataLeadStreamTest {

    @Test
    public void shouldDetectEolOnWrite() throws Exception {
        // given
        final var outputStream = new ByteArrayOutputStream();
        final var dataLeadStream = new OutboundEventWriter.DataLeadStream(outputStream);

        // when
        dataLeadStream.write('A');
        dataLeadStream.write('\r');
        dataLeadStream.write('B');
        dataLeadStream.write('\n');
        dataLeadStream.write('C');
        dataLeadStream.write('\r');
        dataLeadStream.write('\n');
        dataLeadStream.write('D');
        dataLeadStream.write('\r');
        dataLeadStream.write('\r');
        dataLeadStream.write('E');
        dataLeadStream.write('\n');
        dataLeadStream.write('\n');
        dataLeadStream.write('F');
        dataLeadStream.write('\r');
        dataLeadStream.write('\n');
        dataLeadStream.write('\r');
        dataLeadStream.write('\n');
        dataLeadStream.write('G');
        dataLeadStream.write("H".getBytes(UTF_8));
        dataLeadStream.write("IJ".getBytes(UTF_8));
        dataLeadStream.write("KLM".getBytes(UTF_8));
        dataLeadStream.write("N\rO\nP\r\nQ\n\nR\r\rS\r\n\r\nT".getBytes(UTF_8));
        dataLeadStream.write('\r');
        dataLeadStream.write("U".getBytes(UTF_8));
        dataLeadStream.write('\r');
        dataLeadStream.write("\nV".getBytes(UTF_8));
        dataLeadStream.write('\r');
        dataLeadStream.write("\rW".getBytes(UTF_8));
        dataLeadStream.write('\n');
        dataLeadStream.write("X".getBytes(UTF_8));
        dataLeadStream.write('\n');
        dataLeadStream.write("\nY".getBytes(UTF_8));
        dataLeadStream.write('\n');
        dataLeadStream.write("\rZ".getBytes(UTF_8));
        dataLeadStream.write("a\r".getBytes(UTF_8));
        dataLeadStream.write('b');
        dataLeadStream.write("c\n".getBytes(UTF_8));
        dataLeadStream.write('d');
        dataLeadStream.write("e\r".getBytes(UTF_8));
        dataLeadStream.write('\r');
        dataLeadStream.write("f\n".getBytes(UTF_8));
        dataLeadStream.write('\n');
        dataLeadStream.write("g\r".getBytes(UTF_8));
        dataLeadStream.write('\n');
        dataLeadStream.write("h\n".getBytes(UTF_8));
        dataLeadStream.write('\r');
        dataLeadStream.finish();

        // then
        assertEquals(
                "data: A\ndata: B\ndata: C\ndata: D\ndata: \ndata: E\ndata: \ndata: F\ndata: \ndata: G"
              + "H"
              + "IJ"
              + "KLM"
              + "N\ndata: O\ndata: P\ndata: Q\ndata: \ndata: R\ndata: \ndata: S\ndata: \ndata: T"
              + "\ndata: U"
              + "\ndata: V"
              + "\ndata: \ndata: W"
              + "\ndata: X"
              + "\ndata: \ndata: Y"
              + "\ndata: \ndata: Z"
              + "a\ndata: b"
              + "c\ndata: d"
              + "e\ndata: \ndata: "
              + "f\ndata: \ndata: "
              + "g\ndata: "
              + "h\ndata: \ndata: ",
                outputStream.toString(UTF_8));
    }
}
