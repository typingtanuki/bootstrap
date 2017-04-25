package com.github.typingtanuki.bootstrap;

/**
 * Created by tanuq on 17/04/25.
 */
public class TestMain {
    public static void main(String... args) {
        System.out.println("woohoo !" + System.getProperty("jean.bob"));
        for (String arg : args) {
            System.out.println(arg);
        }
    }
}
