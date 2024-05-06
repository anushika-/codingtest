package com.oc.codingtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class PasswordStrength {

    private static final Logger log = LoggerFactory.getLogger(PasswordStrength.class);

    public boolean isPasswordPermissible(String password, int maxAllowedRepetitionCount, int maxAllowedSequenceLength) {
        // This method accepts a password (String) and calculates two password strength parameters:
        // Repetition count and Max Sequence length
        if (password == null || password.isEmpty()) {
            return false; // Password is empty, return false
        }
        return getMaxRepetitionCount(password) <= maxAllowedRepetitionCount && getMaxSequenceLen(password) <= maxAllowedSequenceLength;
    }

    /**
     * Repetition count - the number of occurrences of the *most repeated* character within the password
     * eg1: "Melbourne" has a repetition count of 2 - for the 2 non-consecutive "e" characters.
     * eg2: "passwords" has a repetition count of 3 - for the 3 "s" characters
     * eg3: "lucky" has a repetition count of 1 - each character appears only once.
     * eg4: "Elephant" has a repetition count of 1 - as the two "e" characters have different cases (ie one "E", one "e")
     * The repetition count should be case-sensitive.
     *
     * @param password
     * @return
     */
    public int getMaxRepetitionCount(String password) {
        if (password == null || password.isEmpty()) {
            log.warn("Null or empty password provided. Returning 0 for max repetition count.");
            return 0;
        }

        try {
            Map<Character, Long> charFrequencyMap = password.chars().mapToObj(c -> (char) c).collect(groupingBy(c -> c, counting()));

            Optional<Long> maxFrequency = charFrequencyMap.values().stream().max(Long::compare);
            log.info("Max repetition count for given password is: {}", maxFrequency);

            return maxFrequency.map(Math::toIntExact).orElse(0);
        } catch (ArithmeticException e) {
            log.error("Arithmetic exception occurred while converting Long to int: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Max Sequence length - The length of the longest ascending/descending sequence of alphabetical or numeric characters
     * eg: "4678" and "4321" would both have sequence length of 4
     * eg2: "cdefgh" would have a sequence length of 6
     * eg3: "password123" would have a max. sequence length of 3 - for the sequence of "123".
     * eg3a: "1pass2word3" would have a max. sequence length of 0 - as there is no sequence.
     * eg3b: "passwordABC" would have a max. sequence length of 3 - for the sequence of "ABC".
     * eg4: "AbCdEf" would have a sequence length of 6, even though it is mixed case.
     * eg5: "ABC_DEF" would have a sequence length of 3, because the special character breaks the progression
     * Check the supplied password.  Return true if the repetition count and sequence length are below or equal to the
     * specified maximum.  Otherwise, return false.
     *
     * @param password
     * @return
     */
    public int getMaxSequenceLen(String password) {
        if (password == null || password.isEmpty()) {
            log.warn("Null or empty password provided. Returning 0 for max sequence length.");
            return 0;
        }

        int maxSequenceLength = 0;
        int currentSequenceLength = 1;
        boolean isAscending = false;
        boolean isDescending = false;

        try {
            for (int i = 1; i < password.length(); i++) {
                char currentChar = password.charAt(i);
                char previousChar = password.charAt(i - 1);

                // Check for ascending sequence
                if (isSequence(currentChar, previousChar, true)) {
                    if (isDescending) {
                        currentSequenceLength = 1;
                        isDescending = false;
                    }
                    isAscending = true;
                    currentSequenceLength++;
                }
                // Check for descending sequence
                else if (isSequence(currentChar, previousChar, false)) {
                    if (isAscending) {
                        currentSequenceLength = 1;
                        isAscending = false;
                    }
                    isDescending = true;
                    currentSequenceLength++;
                } else {
                    maxSequenceLength = Math.max(maxSequenceLength, currentSequenceLength);
                    currentSequenceLength = 1;
                    isAscending = false;
                    isDescending = false;
                }
            }

            // Check for the last sequence
            maxSequenceLength = Math.max(maxSequenceLength, currentSequenceLength);
            log.info("Max sequence length for given password is: {}", maxSequenceLength);
            return maxSequenceLength;
        } catch (Exception e) {
            log.error("Error occurred while calculating max sequence length: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Checks if the given characters form a sequence.
     *
     * A sequence is defined as a series of consecutive alphanumeric characters
     * either in ascending or descending order.
     *
     * @param currentChar The current character.
     * @param previousChar The previous character.
     * @param ascending Specifies whether the sequence should be checked in ascending order (true) or descending order (false).
     * @return true if the characters form a sequence according to the specified order, false otherwise.
     */
    private boolean isSequence(char currentChar, char previousChar, boolean ascending) {
        char currentLower = Character.toLowerCase(currentChar);
        char previousLower = Character.toLowerCase(previousChar);

        if (isAlphabetOrNumeric(currentChar) && isAlphabetOrNumeric(previousChar)) {
            return ascending ? currentLower == previousLower + 1 : currentLower == previousLower - 1;
        }

        return false;
    }

    /**
     * Checks if the given character is an alphabet or numeric character.
     *
     * Returns false if the character is null.
     *
     * @param c The character to be checked.
     * @return true if the character is an alphabet or numeric character, false otherwise or if the character is null.
     */
    private boolean isAlphabetOrNumeric(Character c) {
        if (c == null) {
            log.warn("Null character provided. Returning false for isAlphabetOrNumeric.");
            return false;
        }

        try {
            return Character.isLetterOrDigit(c);
        } catch (Exception e) {
            log.error("Error occurred while checking if character is alphabet or numeric: {}", e.getMessage());
            return false;
        }
    }
}
