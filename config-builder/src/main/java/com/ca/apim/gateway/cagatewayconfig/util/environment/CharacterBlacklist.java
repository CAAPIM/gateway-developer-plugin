package com.ca.apim.gateway.cagatewayconfig.util.environment;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Contains all blacklisted characters for file names in Windows and UNIX. Purpose of class is to provide all
 * values in a set.
 */
public class CharacterBlacklist {

    // Windows
    public static final char LESS_THAN = '<';
    public static final char GREATER_THAN = '>';
    public static final char COLON = ':';
    public static final char DOUBLE_QUOTE = '\"';
    public static final char FORWARD_SLASH = '/';
    public static final char BACK_SLASH = '\\';
    public static final char PIPE = '|';
    public static final char QUESTION_MARK = '?';
    public static final char ASTERISK = '*';

    // Unix
    public static final char NULL_CHAR = '\0';

    private static final Set<Character> charBlackList = initializeCharBlacklist();

    /**
     * Retrieve set of blacklist characters
     *
     * @return Character Set with all constants in the class. Constants are blacklisted characters.
     */

    public static Set<Character> getCharBlacklist() {
        return charBlackList;
    }

    private static Set<Character> initializeCharBlacklist() {
        Field[] fields = CharacterBlacklist.class.getFields();
        Set<Character> charBlackList = new HashSet<>();

        Stream.of(fields).forEach(field -> {
            try {
                char fieldValue = (Character) field.get(CharacterBlacklist.class);
                charBlackList.add(fieldValue);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Error accessing blacklist character field " + field.getName(), e);
            }
        });

        return charBlackList;
    }

    private CharacterBlacklist() {}
}
