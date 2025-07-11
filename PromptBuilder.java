package com.yourplugin.sparklineageplugin;

public class PromptBuilder {
   /* public static String buildPrompt(String sparkCode) {
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
                .append("Mermaid Diagram which generated should be validated with .md valid files without any erros like open bracket ( or close bracket or any syntax erros related to .md extension  , please validate the file and give me the amazing response with excelllent animations and mermaid should contain all the columns from top df to bottom df with details :\n")
                .append(sparkCode);

        return  prompt.toString();
    }
*/
    public static String buildPrompt(String sparkCode) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You're a data engineering assistant. Analyze the following Apache Spark job and provide a complete data lineage report.\n\n")

                .append("Specifically, your response must include:\n\n")

                .append("1. Identify and list all **source datasets/tables/files** the job reads from.\n")
                .append("2. For each dataset, describe the **schema and any filters applied** when reading.\n")
                .append("3. Trace every transformation applied on the datasets, including:\n")
                .append("   - Filter, map, select, withColumn, drop\n")
                .append("   - Joins (inner, outer, left, right) and explain join keys and types\n")
                .append("   - Aggregations like groupBy or reduceByKey, with grouping columns\n")
                .append("   - Any user-defined functions or custom logic\n")
                .append("4. Show how datasets are **combined, split, or branched** throughout the job.\n")
                .append("5. Identify the **final output dataset(s)** – where the result is stored (e.g., tables, files).\n")
                .append("6. Map every output dataset **back to its original source(s)** and the transformations applied.\n")
                .append("7. Provide **code snippets or pseudo-code** for each major step, to support clarity.\n")
                .append("8. If present, explain the effect of **cache, checkpoint, repartition, etc.**, on lineage.\n")
                .append("9. Summarize the lineage in a **Mermaid diagram** with:\n")
                .append("   - Datasets as nodes\n")
                .append("   - Transformations as arrows (edges)\n")
                .append("   - Show **column-level mappings** from each input to output\n")
                .append("   - Ensure Mermaid syntax is **valid** and renders correctly in `.md` files (no broken brackets or missing graph direction)\n")
                .append("10. Highlight any **data quality or transformation risks** detected in the job.\n\n")

                .append("Additional Instructions:\n")
                .append("- Use **clear, simple language** suitable for Spark developers.\n")
                .append("- **Avoid jargon** unless explained.\n")
                .append("- Number each section clearly.\n")
                .append("- **Include column names** and mapping where possible.\n")
                .append("- If external systems (Hive, Kafka, Delta Lake, etc.) are involved, explain their impact on lineage.\n")
                .append("- Mention assumptions if the code is ambiguous or incomplete.\n\n")

                .append("Please provide the detailed data lineage report as instructed, including the Mermaid diagram for visualization.\n")
                .append("Mermaid should contain **all the column info** as well.\n")
                .append("Give me **detailed columns of each dataset** and **mappings till the final dataset** for all.\n\n")
                .append("very important to note that add column names of datasets also in mermaid diagram")
                .append("Here is the Spark job code to analyze:\n\n")
                .append(sparkCode);

        return prompt.toString();
    }

}