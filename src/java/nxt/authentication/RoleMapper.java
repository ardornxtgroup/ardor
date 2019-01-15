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

import java.util.EnumSet;

/**
 * Map Nxt account to authentication role
 */
public interface RoleMapper {

    /**
     * Get the user roles for the supplied Nxt account
     *
     * @param   rsAccount           Nxt account
     * @return                      Set of user roles
     */
    EnumSet<Role> getUserRoles(String rsAccount);

    /**
     * Parse the account property string containing the user roles.
     * Multiple roles can be specified separated by commas.
     *
     * @param   value               User roles
     * @return                      Set of user roles
     */
    EnumSet<Role> parseRoles(String value);

    /**
     * Check if the supplied Nxt account is allowed to set user roles.
     * An account must have the ADMIN role in order to set user roles.
     *
     * @param   setterId            Account setting the user roles
     * @return                      TRUE if the account is allowed to set user roles
     */
    boolean isValidRoleSetter(long setterId);

    /**
     * Check if the supplied Nxt account has the specified user role
     *
     * @param   accountId           Account identifier
     * @param   role                User role
     * @return                      TRUE if the account has the specified user role
     */
    boolean isUserInRole(long accountId, Role role);
}
