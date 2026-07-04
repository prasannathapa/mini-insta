package com.miniinsta.search;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pulls {@code #hashtags} out of a caption, lower-cased and de-duplicated. */
public final class Hashtags {

    private static final Pattern TAG = Pattern.compile("#(\\w+)");

    private Hashtags() {
    }

    public static Set<String> extract(String text) {
        Set<String> tags = new LinkedHashSet<>();
        if (text != null) {
            Matcher matcher = TAG.matcher(text);
            while (matcher.find()) {
                tags.add(matcher.group(1).toLowerCase());
            }
        }
        return tags;
    }
}
