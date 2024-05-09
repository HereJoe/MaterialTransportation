package com.adl.genius.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String encode(String s) {
        return encoder.encode(s);
    }

    public static boolean match(String raw, String encoded) {
        return encoder.matches(raw, encoded);
    }
}
