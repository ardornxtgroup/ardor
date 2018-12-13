package nxt.util.security;

import java.security.BasicPermission;

public class BlockchainPermission extends BasicPermission {

    public BlockchainPermission(String name) {
        super(name);
    }

}
