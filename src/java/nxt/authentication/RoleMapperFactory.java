/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2019 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */
package nxt.authentication;

import nxt.Constants;
import nxt.util.security.BlockchainPermission;

import java.util.EnumSet;

/**
 * Create a role mapper for a permissioned blockchain
 */
public class RoleMapperFactory {

    private RoleMapperFactory() {}

    private static final RoleMapper roleMapper;
    static {
        if (Constants.isPermissioned) {
            try {
                Class<?> roleMapperClass = Class.forName("com.jelurida.blockchain.authentication.BlockchainRoleMapper");
                roleMapper = (RoleMapper)roleMapperClass.getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        } else {
            roleMapper = new NullRoleMapper();
        }
    }

    /**
     * Get the role mapper
     *
     * A null role mapper will be returned if the blockchain is not permissioned
     *
     * @return                      Role mapper
     */
    public static RoleMapper getRoleMapper() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("authentication"));
        }
        return roleMapper;
    }

    /**
     * Dummy role mapper for a non-permissioned blockchain
     */
    public static class NullRoleMapper implements RoleMapper {

        @Override
        public EnumSet<Role> getUserRoles(String rsAccount) {
            return EnumSet.noneOf(Role.class);
        }

        @Override
        public boolean isValidRoleSetter(long setterId) {
            return false;
        }

        @Override
        public boolean isUserInRole(long accountId, Role role) {
            return false;
        }

        @Override
        public EnumSet<Role> parseRoles(String value) {
            return EnumSet.noneOf(Role.class);
        }
    }
}
