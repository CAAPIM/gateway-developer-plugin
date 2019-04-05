package com.ca.apim.gateway.cagatewayconfig.util.environment;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.stream.Stream;

public class CharacterBlacklist {

    public static final char LESS_THAN = '<';
    public static final char GREATER_THAN = '>';
    public static final char COLON = ':';
    public static final char DOUBLE_QUOTE = '\'';
    public static final char FORWARD_SLASH = '/';
    public static final char BACK_SLASH = '\\';
    public static final char PIPE = '|';
    public static final char QUESTION_MARK = '?';
    public static final char ASTERISK = '*';
    public static final char NULL_CHAR = '\0';

    public static HashSet<Character> getCharBlacklist() {
        Field[] fields = CharacterBlacklist.class.getFields();
        CharacterBlacklist cblInstance = new CharacterBlacklist();
        HashSet<Character> charBlackList = new HashSet<>();

        Stream.of(fields).forEach(field -> {
            try {
                char fieldValue = (Character) field.get(cblInstance);
                charBlackList.add(fieldValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        return charBlackList;
    }

    private CharacterBlacklist() {
        //
    }
}
