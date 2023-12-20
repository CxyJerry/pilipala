package com.jerry.pilipala.domain.common.template;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateResolver {
    private final Pattern pattern = Pattern.compile("\\{(.*?)\\}");

    public List<String> extractVariables(String text) {
        Matcher matcher = pattern.matcher(text);
        List<String> placeholder = new ArrayList<>();
        while (matcher.find()) {
            String group = matcher.group(1);
            placeholder.add(group);
        }
        return placeholder;
    }

    public String fillVariable(String text, Map<String, String> params) {
        Matcher matcher = pattern.matcher(text);

        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = params.getOrDefault(placeholder, placeholder);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }
}
