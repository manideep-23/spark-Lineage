package com.yourplugin.sparklineageplugin.settings;

import com.intellij.openapi.components.*;

@State(
        name = "UnitTestSettingsState",
        storages = {@Storage("UnitTestSettings.xml")}
)
public class UnitTestSettingsState implements PersistentStateComponent<UnitTestSettingsState> {

    public String javaVersion = "11";
    public String sparkVersion = "3.3.2";
    public String mockitoVersion = "4.11.0";
    public String language = "Java";
    public String framework = "Java";

    public static UnitTestSettingsState getInstance() {
        return ServiceManager.getService(UnitTestSettingsState.class);
    }

    @Override
    public UnitTestSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(UnitTestSettingsState state) {
        this.javaVersion = state.javaVersion;
        this.sparkVersion = state.sparkVersion;
        this.mockitoVersion = state.mockitoVersion;
        this.language = state.language;
        this.framework = state.framework;
    }
}
