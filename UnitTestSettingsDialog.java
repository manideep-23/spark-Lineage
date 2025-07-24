package com.yourplugin.sparklineageplugin.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.yourplugin.sparklineageplugin.settings.UnitTestSettingsState;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class UnitTestSettingsDialog extends DialogWrapper {
 /*   private final JComboBox<String> javaVersionCombo;
    private final JComboBox<String> sparkVersionCombo;
    private final JComboBox<String> mockitoVersionCombo;
    private final JComboBox<String> languageCombo;
    private final JComboBox<String> frameworkCombo;*/
   // private final JPanel panel;
    UnitTestSettingsState state;
    private JComboBox<String> javaVersionCombo;
    private JComboBox<String> sparkVersionCombo;
    private JComboBox<String> mockitoVersionCombo;
    private JComboBox<String> languageCombo;
    private JComboBox<String> frameworkCombo;
    private JPanel contentPane;


   /* public UnitTestSettingsDialog() {
        super(true);
        setTitle("Unit Test Configuration");

        javaVersionCombo = new JComboBox<>(new String[]{"8", "11", "17"});
        sparkVersionCombo = new JComboBox<>(new String[]{"2.4.8", "3.1.2", "3.3.2"});
        mockitoVersionCombo = new JComboBox<>(new String[]{"3.12.4", "4.11.0"});
        languageCombo = new JComboBox<>(new String[]{"Java", "Scala"});
        frameworkCombo = new JComboBox<>(new String[]{"Java", "Java-Spark", "Spring Boot Batch"});

        state = UnitTestSettingsState.getInstance();
        javaVersionCombo.setSelectedItem(state.javaVersion);
        sparkVersionCombo.setSelectedItem(state.sparkVersion);
        mockitoVersionCombo.setSelectedItem(state.mockitoVersion);
        languageCombo.setSelectedItem(state.language);
        frameworkCombo.setSelectedItem(state.framework);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Java Version:"));
        panel.add(javaVersionCombo);
        panel.add(new JLabel("Spark Version:"));
        panel.add(sparkVersionCombo);
        panel.add(new JLabel("Mockito Version:"));
        panel.add(mockitoVersionCombo);
        panel.add(new JLabel("Language:"));
        panel.add(languageCombo);
        panel.add(new JLabel("Framework:"));
        panel.add(frameworkCombo);

        init();
    }*/

    public UnitTestSettingsDialog() {
        super(true);
        setTitle("Unit Test Settings");

        // ✅ initialize state safely before using
        state = UnitTestSettingsState.getInstance();
        if (state == null) {
            state = new UnitTestSettingsState(); // fallback to new if not yet set
        }

        initUI();  // extract this if your constructor was bloated

        init(); // required to initialize DialogWrapper
    }

    private void initUI() {
        contentPane = new JPanel(new GridLayout(5, 2));
        javaVersionCombo = new JComboBox<>(new String[]{"Java 8", "Java 11", "Java 17"});
        sparkVersionCombo = new JComboBox<>(new String[]{"Spark 2.4", "Spark 3.0", "Spark 3.4"});
        mockitoVersionCombo = new JComboBox<>(new String[]{"Mockito 3", "Mockito 4"});
        languageCombo = new JComboBox<>(new String[]{"Java", "Scala"});
        frameworkCombo = new JComboBox<>(new String[]{"JUnit", "TestNG"});

        // Avoid NullPointerException: Only access state after null check or default fallback
        javaVersionCombo.setSelectedItem(state.javaVersion);
        sparkVersionCombo.setSelectedItem(state.sparkVersion);
        mockitoVersionCombo.setSelectedItem(state.mockitoVersion);
        languageCombo.setSelectedItem(state.language);
        frameworkCombo.setSelectedItem(state.framework);

        contentPane.add(new JLabel("Java Version:"));
        contentPane.add(javaVersionCombo);
        contentPane.add(new JLabel("Spark Version:"));
        contentPane.add(sparkVersionCombo);
        contentPane.add(new JLabel("Mockito Version:"));
        contentPane.add(mockitoVersionCombo);
        contentPane.add(new JLabel("Language:"));
        contentPane.add(languageCombo);
        contentPane.add(new JLabel("Framework:"));
        contentPane.add(frameworkCombo);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    protected void doOKAction() {
        UnitTestSettingsState state = UnitTestSettingsState.getInstance();
        state.javaVersion = (String) javaVersionCombo.getSelectedItem();
        state.sparkVersion = (String) sparkVersionCombo.getSelectedItem();
        state.mockitoVersion = (String) mockitoVersionCombo.getSelectedItem();
        state.language = (String) languageCombo.getSelectedItem();
        state.framework = (String) frameworkCombo.getSelectedItem();
        System.out.println("done doOkAction");
        super.doOKAction();
    }
    public UnitTestSettingsState getVersionSettings() {
        return state;
    }

    public String getJavaVersion() {
        return (String) javaVersionCombo.getSelectedItem();
    }

    public String getSparkVersion() {
        return (String) sparkVersionCombo.getSelectedItem();
    }

    public String getMockitoVersion() {
        return (String) mockitoVersionCombo.getSelectedItem();
    }

    public String getLanguage() {
        return (String) languageCombo.getSelectedItem();
    }

    public String getFramework() {
        return (String) frameworkCombo.getSelectedItem();
    }

}
