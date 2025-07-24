package com.yourplugin.sparklineageplugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeExtractor {

    public static String extractJavaCode(String llmResponse) {
        Pattern pattern = Pattern.compile("```java\\s*(.*?)\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(llmResponse);
        if (matcher.find()) {
            return matcher.group(1).trim(); // Java code inside the block
        } else {
            return llmResponse.trim(); // fallback: return everything if no ```java found
        }
    }

    // Example usage
    public static void main(String[] args) {
        String llmResponse = "Here is your code:\n```java\npublic class Test {\n  void run() {}\n}\n```";
        String javaCode = extractJavaCode(llmResponse);
        System.out.println(javaCode);
    }
}
