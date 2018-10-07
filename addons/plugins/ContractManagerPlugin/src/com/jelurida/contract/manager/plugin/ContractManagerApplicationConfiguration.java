package com.jelurida.contract.manager.plugin;

import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import nxt.util.Convert;
import org.jetbrains.annotations.NotNull;

public class ContractManagerApplicationConfiguration extends ApplicationConfiguration {

    private static ContractManagerConfigurable editor;

    public ContractManagerApplicationConfiguration(String name, Project project, ApplicationConfigurationType applicationConfigurationType) {
        super(name, project, applicationConfigurationType);
    }

    @Override
    @NotNull
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<ApplicationConfiguration> group = new SettingsEditorGroup<>();
        editor = new ContractManagerConfigurable(getProject());
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), editor);
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        group.addEditor(ExecutionBundle.message("logs.tab.title"), new LogConfigurationPanel<>());
        return group;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        super.checkConfiguration();
        if (editor == null) {
            return;
        }
        String cmContractHash = editor.getCmContractHash();
        if (cmContractHash != null && !cmContractHash.equals("")) {
            try {
                byte[] hashBytes = Convert.parseHexString(cmContractHash);
                if (hashBytes.length != 32) {
                    throw new RuntimeConfigurationWarning(ContractManagerBundle.message("invalid.contract.hash"));
                }
            } catch (Exception e) {
                throw new RuntimeConfigurationWarning(ContractManagerBundle.message("cannot.parse.contract.hash"));
            }
        }
        String cmReferenceAccount = editor.getCmReferenceAccount();
        try {
            Convert.parseAccountId(cmReferenceAccount);
        } catch (Throwable t) {
            throw new RuntimeConfigurationWarning(ContractManagerBundle.message("invalid.account.address", t.getMessage()));
        }
    }
}
