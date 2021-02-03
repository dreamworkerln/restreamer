package ru.kvanttelecom.tv.restreamer.utils;

/**
 * Extends org.springframework.util.StringUtils to apache.commons.lang3.StringUtils.isBlank
 */
public class StringUtils extends org.springframework.util.StringUtils {

    /**
     * Checks if a String is null or empty ("") or whitespace only.
     * <br>Like org.apache.commons.lang3.StringUtils.isBlank
     * <br>(against - Spring StringUtils.isEmpty doesn't trim whitespaces)
     */
    public static boolean isBlank(String s) {
        return !org.springframework.util.StringUtils.hasLength(org.springframework.util.StringUtils.trimWhitespace(s));
    }

    public static boolean notBlank(String s) {
        return org.springframework.util.StringUtils.hasLength(org.springframework.util.StringUtils.trimWhitespace(s));
    }


//    public boolean isNullOrEmpty(Object object) {
//        return object == null || object.getClass() == String.class && ((String)object).trim().isEmpty();
//    }


//    public String getNullIfEmpty(String s) {
//
//        if (s != null && s.trim().isEmpty())
//            s = null;
//
//        return s;
//    }
//
//    public String getEmptyIfNull(String s) {
//
//        if (s == null)
//            s = "";
//
//        return s;
//    }
}
