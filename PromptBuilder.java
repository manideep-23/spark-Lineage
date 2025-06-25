package com.yourplugin.sparklineageplugin;

public class PromptBuilder {
    public static String buildPrompt(String sparkCode) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following Apache Spark code and generate a full data lineage report.\n\n")
                .append("Your goals:\n")
                .append("1. Identify all source datasets (Hive tables, S3 files, etc.).\n")
                .append("2. Describe each dataset’s schema and filters.\n")
                .append("3. Trace all transformations:\n")
                .append("   - Filter, map, select, drop, withColumn, etc.\n")
                .append("   - Joins and join types\n")
                .append("   - Aggregations (groupBy, reduceByKey)\n")
                .append("   - UDFs\n")
                .append("4. Explain how data splits or merges during execution.\n")
                .append("5. Identify the final output/sink.\n")
                .append("6. Map outputs back to their original sources and transformations.\n")
                .append("7. Provide Spark code examples where possible.\n")
                .append("8. Generate a Mermaid diagram for visualization.\n")
                .append("9. Mention any data quality or transformation risks.\n")
                .append("10. If Kafka, Hive, Delta Lake are involved, explain their lineage impact.\n\n")
                .append("Use clear language, number each section, and include Mermaid code at the end.\n\n")
                .append("Code:\n")
                .append(sparkCode);

        return  prompt.toString();
    }
}