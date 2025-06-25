package com.yourplugin.sparklineageplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineageResultPanel extends DialogWrapper {
    private final String content;

    protected LineageResultPanel(Project project, String content) {
        super(project);
        this.content = content;
        init();
        setTitle("Spark Lineage Report");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea textArea = new JTextArea(content);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        JButton copyButton = new JButton("Copy Mermaid Diagram");
        copyButton.addActionListener(e -> {
            String mermaid = extractMermaid(content);
            if (mermaid != null) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new java.awt.datatransfer.StringSelection(mermaid), null);
                JOptionPane.showMessageDialog(panel, "Mermaid diagram copied to clipboard.");
            } else {
                JOptionPane.showMessageDialog(panel, "No Mermaid diagram found.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(copyButton, BorderLayout.SOUTH);
        return panel;
    }

    private static String extractMermaid(String text) {
        Pattern pattern = Pattern.compile("```mermaid\\n([\\s\\S]*?)```", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static void show(Project project, String result) {
        LineageResultPanel panel = new LineageResultPanel(project, result);
        panel.show();
    }
}