package org.glassfish.jersey.examples.security.authentication;

import org.glassfish.jersey.examples.security.model.response.Token;

public interface Authenticator<T> {
    /**
     *
     * @param credentials
     * @return
     */
    Token authenticates(T credentials);
}
