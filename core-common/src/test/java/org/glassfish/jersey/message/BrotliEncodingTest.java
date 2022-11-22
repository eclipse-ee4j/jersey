package org.glassfish.jersey.message;

import com.oracle.brotli.decoder.BrotliInputStream;
import com.oracle.brotli.encoder.BrotliOutputStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BrotliEncodingTest extends AbstractEncodingTest {

    @Test
    public void testEncode() throws IOException {
        test(new AbstractEncodingTest.TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new BrotliEncoder().encode("gzip", stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return BrotliInputStream.builder().inputStream(stream).build();
            }
        });
    }

    @Test
    public void testDecode() throws IOException {
        test(new AbstractEncodingTest.TestSpec() {
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
        test(new AbstractEncodingTest.TestSpec() {
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
}
