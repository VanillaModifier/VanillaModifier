package org.vanillamodifier.util;

public class RandomStringUtil {

    private static final char[] randomString = "qwertyuiopasdfghjklzxcvbnm".toCharArray();

    public static String getRandomString (int length) {
        java.util.Random random = new java.util.Random();
        char[] array = new char[ length ];
        for (int i = 0; i < length; i++) {
            array[ i ] = randomString[ random.nextInt(randomString.length) ];
        }
        return new String(array);
    }
}

