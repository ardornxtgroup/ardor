package com.jelurida.contract.manager.plugin;

import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@State(name = "ContractManager", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class ContractManagerConfigurationType extends ApplicationConfigurationType implements ConfigurationType {

    private final ConfigurationFactory myFactory;

    public ContractManagerConfigurationType() {
        myFactory = new ContractManagerConfigurationFactory(this) {
            @NotNull
            @Override
            public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
                return new ContractManagerApplicationConfiguration("", project, ContractManagerConfigurationType.this);
            }
        };
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myFactory};
    }

    @Override
    public String getConfigurationTypeDescription() {
        return ContractManagerBundle.message("application.configuration.description");
    }

    public String getDisplayName() {
        return ContractManagerBundle.message("display.name");
    }

    public Icon getIcon() {
        return IconLoader.getIcon("ContractManager.png");
    }

    @NotNull
    public String getId() {
        return "#contractmanager";
    }

}