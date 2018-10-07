package com.jelurida.contract.manager.plugin;

import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * This class might be used in the future to extend the configuration of the internal ApplicationConfiguration
 * object but right now the IntelliJ API does not provide access to the options object.
 * Need to revisit in the future once this object can access the options class
 */
public abstract class ContractManagerConfigurationFactory extends ConfigurationFactoryEx {
    protected ContractManagerConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public void onNewConfigurationCreated(@NotNull RunConfiguration configuration) {
        ((ModuleBasedConfiguration)configuration).onNewConfigurationCreated();
    }
}
