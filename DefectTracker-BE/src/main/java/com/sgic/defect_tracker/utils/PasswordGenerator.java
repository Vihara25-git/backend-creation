package com.sgic.defect_tracker.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "!@#$%&*";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;

    private static final int PASSWORD_LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        List<Character> chars = new ArrayList<>();

        // guarantee at least one of each character type
        chars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        chars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        chars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        chars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // fill the remaining length randomly from the full pool
        for (int i = chars.size(); i < PASSWORD_LENGTH; i++) {
            chars.add(ALL.charAt(random.nextInt(ALL.length())));
        }

        // shuffle so guaranteed chars aren't always in positions 0-3
        Collections.shuffle(chars, random);

        StringBuilder password = new StringBuilder();
        chars.forEach(password::append);
        return password.toString();
    }
}