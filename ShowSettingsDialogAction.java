package com.yourplugin.sparklineageplugin;

import com.yourplugin.sparklineageplugin.settings.UnitTestSettingsState;
import com.yourplugin.sparklineageplugin.ui.UnitTestSettingsDialog;
import com.intellij.openapi.actionSystem.*;
public class ShowSettingsDialogAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        UnitTestSettingsDialog dialog = new UnitTestSettingsDialog();
        if (dialog.showAndGet()) {
            UnitTestSettingsState settings = UnitTestSettingsState.getInstance();
            settings.javaVersion = dialog.getJavaVersion();
            settings.sparkVersion = dialog.getSparkVersion();
            settings.mockitoVersion = dialog.getMockitoVersion();
        }
    }
}
