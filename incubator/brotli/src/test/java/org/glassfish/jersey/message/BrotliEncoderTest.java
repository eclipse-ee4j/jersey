package org.glassfish.jersey.message;

import com.oracle.brotli.decoder.BrotliInputStream;
import com.oracle.brotli.encoder.BrotliOutputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrotliEncoderTest {

    @Test
    public void testEncode() throws IOException {
        test(new TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new BrotliEncoder().encode("br", stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return BrotliInputStream.builder().inputStream(stream).build();
            }
        });
    }

    @Test
    public void testDecode() throws IOException {
        test(new TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return BrotliOutputStream.builder().outputStream(stream).build();
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return new BrotliEncoder().decode("br", stream);
            }
        });
    }

    @Test
    public void testEncodeDecode() throws IOException {
        test(new TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new BrotliEncoder().encode("br", stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return new BrotliEncoder().decode("br", stream);
            }
        });
    }

    void test(TestSpec testSpec) throws IOException {
        byte[] entity = "Hello world!".getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream encoded = testSpec.getEncoded(baos);
        encoded.write(entity);
        encoded.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        byte[] result = new byte[entity.length];
        InputStream decoded = testSpec.getDecoded(bais);
        int len = decoded.read(result);
        assertEquals(-1, decoded.read());
        decoded.close();
        assertEquals(entity.length, len);
        assertArrayEquals(entity, result);
    }

    interface TestSpec {
        /**
         * Returns encoded stream.
         *
         * @param stream Original stream.
         * @return Encoded stream.
         * @throws IOException I/O exception.
         */
        OutputStream getEncoded(OutputStream stream) throws IOException;

        /**
         * Returns decoded stream.
         *
         * @param stream Original stream.
         * @return Decoded stream.
         * @throws IOException I/O exception.
         */
        InputStream getDecoded(InputStream stream) throws IOException;
    }
}
