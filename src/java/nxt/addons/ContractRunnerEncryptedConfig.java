package nxt.addons;

import nxt.http.APITag;
import org.json.simple.JSONStreamAware;

import java.io.BufferedReader;

public class ContractRunnerEncryptedConfig extends AbstractEncryptedConfig {
    private ContractRunner contractRunner;

    ContractRunnerEncryptedConfig(ContractRunner contractRunner) {
        this.contractRunner = contractRunner;
    }

    @Override
    protected String getAPIRequestName() {
        return "ContractRunner";
    }

    @Override
    protected APITag getAPITag() {
        return null;
    }

    @Override
    protected String getDataParameter() {
        return "contractRunner";
    }

    @Override
    protected JSONStreamAware processDecrypted(BufferedReader reader) {
        return contractRunner.parseConfig(reader);
    }
}
