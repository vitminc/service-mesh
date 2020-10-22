package org.istio.sts.config;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ApplicationPasswordEncoder implements PasswordEncoder {
    private final PasswordEncoder delegate = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Override
    public String encode(CharSequence charSequence) {
        return delegate.encode(charSequence);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword!=null && encodedPassword.startsWith("{")) {
            return delegate.matches(rawPassword, encodedPassword);
        } else {
            return (rawPassword == null && encodedPassword == null) ||
                    rawPassword != null && rawPassword.toString().equals(encodedPassword);
        }
    }
}