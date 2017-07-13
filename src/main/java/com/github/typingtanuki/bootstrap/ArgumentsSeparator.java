package com.github.typingtanuki.bootstrap;

/**
 * @author clerc
 * @since 2017/07/13
 */
public enum ArgumentsSeparator {
    EQUALS("="),
    SPACE(" ");

    private final String separator;

    ArgumentsSeparator(String separator) {
        this.separator = separator;
    }

    public String getSeparator() {
        return separator;
    }
}
