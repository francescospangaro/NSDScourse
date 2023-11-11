package it.polimi.middleware.kafka.Lab1.ES4;

import java.util.Random;

public class StringGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String generateRandomString(int length) {
        if (length <= 0) {
            length = 1;
        }

        StringBuilder randomString = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALPHABET.length());
            char randomChar = ALPHABET.charAt(randomIndex);
            randomString.append(randomChar);
        }

        return randomString.toString();
    }

}

