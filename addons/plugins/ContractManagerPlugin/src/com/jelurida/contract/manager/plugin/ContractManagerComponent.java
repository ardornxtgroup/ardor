package com.jelurida.contract.manager.plugin;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;

@State(name = "ContractManager", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class ContractManagerComponent {
}
