package org.glassfish.jersey.message;

import com.oracle.brotli.decoder.BrotliInputStream;
import com.oracle.brotli.encoder.BrotliOutputStream;
import org.glassfish.jersey.spi.ContentEncoder;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Brotli encoding support. Interceptor that encodes the output or decodes the input if
 * {@link HttpHeaders#CONTENT_ENCODING Content-Encoding header} value equals to {@code br}.
 */
@Priority(Priorities.ENTITY_CODER)
public class BrotliEncoder extends ContentEncoder {

    /**
     * Initialize BrotliEncoder.
     */
    public BrotliEncoder() {
        super("br");
    }

    @Override
    public InputStream decode(String contentEncoding, InputStream encodedStream) throws IOException {
        return BrotliInputStream.builder().inputStream(encodedStream).build();
    }

    @Override
    public OutputStream encode(String contentEncoding, OutputStream entityStream) throws IOException {
        return BrotliOutputStream.builder().outputStream(entityStream).build();
    }
}
