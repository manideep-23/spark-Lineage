package com.yourplugin.sparklineageplugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MermaidValidator {

    public static String extractMermaid(String markdown) {
        Pattern pattern = Pattern.compile("```mermaid\\s+([\\s\\S]*?)\\s+```", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(markdown);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    public static String repairMermaid(String rawDiagram) {
        if (rawDiagram == null || rawDiagram.isEmpty()) return null;

        String[] lines = rawDiagram.split("\\r?\\n");
        StringBuilder repaired = new StringBuilder();

        boolean graphDeclared = lines.length > 0 && lines[0].trim().startsWith("graph");
        if (!graphDeclared) {
            repaired.append("graph TD\n");
        }

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // Fix inline conditionals (e.g., -- state == "CA" --> to -->|state == "CA"| )
            trimmed = trimmed.replaceAll("--\\s*([a-zA-Z0-9_\\s=><!\"']+)\\s*-->", "-->|$1|-->");

            // Escape double quotes inside labels: [This is a "label"] → [This is a 'label']
            trimmed = trimmed.replaceAll("\\[([^\\]]*?)\"([^\\]]*?)\\]", "[$1'$2]");
            trimmed = trimmed.replaceAll("\\{\"([^\"]*?)\"\\}", "{\"$1\"}"); // Keep braces correct

            // Ensure valid format for labels and decisions
            trimmed = trimmed.replaceAll("([\\w\\d]+)\\[(.+?)\\]", "$1[\"$2\"]");
            trimmed = trimmed.replaceAll("([\\w\\d]+)\\{(.+?)\\}", "$1{\"$2\"}");

            repaired.append("    ").append(trimmed).append("\n");
        }

        return repaired.toString().trim();
    }


    public static String extractAndRepair(String markdown) {
        return repairMermaid(extractMermaid(markdown));
    }
}
