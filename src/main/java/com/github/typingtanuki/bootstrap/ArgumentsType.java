package com.github.typingtanuki.bootstrap;

/**
 * Controls the style of arguments passed to the main class
 *
 * @author Clerc Mathias
 */
public enum ArgumentsType {
    /**
     * Always use a single dash <code>-a=12 -long=42</code>
     */
    SINGLE_DASH("-", "-"),
    /**
     * Always use a double dash <code>--a=12 --long=42</code>
     */
    DOUBLE_DASH("--", "--"),
    /**
     * Use a single dash for single letter arguments, otherwise use double <code>-a=12 --long=42</code>
     */
    SINGLE_DOUBLE_DASH("-", "--"),
    /**
     * Always use a single slash <code>/a=12 /long=42</code>
     */
    SLASH("/", "/");

    private final String shortArg;
    private final String longArg;

    ArgumentsType(String shortArg, String longArg) {
        this.shortArg = shortArg;
        this.longArg = longArg;
    }

    public String getShortArg() {
        return shortArg;
    }

    public String getLongArg() {
        return longArg;
    }
}
