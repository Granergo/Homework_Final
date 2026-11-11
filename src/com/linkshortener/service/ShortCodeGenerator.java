package com.linkshortener.service;

import java.security.SecureRandom;

public class ShortCodeGenerator {

    private static final String ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final SecureRandom random = new SecureRandom();
    private final int codeLength;

    public ShortCodeGenerator(int codeLength) {
        if (codeLength <= 0) {
            throw new IllegalArgumentException("Длина кода должна быть положительной.");
        }

        this.codeLength = codeLength;
    }

    public String generate() {
        StringBuilder code = new StringBuilder(codeLength);

        for (int i = 0; i < codeLength; i++) {
            int index = random.nextInt(ALPHABET.length());
            code.append(ALPHABET.charAt(index));
        }

        return code.toString();
    }
}
