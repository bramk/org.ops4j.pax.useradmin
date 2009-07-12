/*
 * Copyright 2009 Matthias Kuespert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.useradmin.service.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;

/**
 * 
 * @author Matthias Kuespert
 * @since  02.07.2009
 */
public class GroupImpl extends UserImpl implements Group {

    protected GroupImpl(String name,
                        UserAdminImpl admin,
                        Map<String, String> properties,
                        Map<String, String> credentials) {
        super(name, admin, properties, credentials);
    }

    public boolean addMember(Role role) {
        if (role != null) {
            try {
                getAdmin().checkAdminPermission();
                StorageProvider storageProvider = getAdmin().getStorageProvider();
                return storageProvider.addMember(this, role);
                // TODO: verify that we really don't need to fire an event here
                // - the spec doesn't mention anything
            } catch (StorageException e) {
                getAdmin().logMessage(this,
                                        "error when adding basic member to group '" + getName() + "':"
                                      + e.getMessage(), LogService.LOG_ERROR);
            }
        }
        return false;
    }

    public boolean addRequiredMember(Role role) {
        if (role != null) {
            try {
                getAdmin().checkAdminPermission();
                StorageProvider storageProvider = getAdmin().getStorageProvider();
                return storageProvider.addRequiredMember(this, role);
            } catch (StorageException e) {
                getAdmin().logMessage(this,
                                        "error when adding required member to group '" + getName() + "':"
                                      + e.getMessage(), LogService.LOG_ERROR);
            }
        }
        return false;
    }

    public boolean removeMember(Role role) {
        if (role != null) {
            try {
                getAdmin().checkAdminPermission();
                StorageProvider storageProvider = getAdmin().getStorageProvider();
                return storageProvider.removeMember(this, role);
            } catch (StorageException e) {
                getAdmin().logMessage(this,
                                        "error when removing member from group '" + getName() + "':"
                                      + e.getMessage(), LogService.LOG_ERROR);
            }
        }
        return false;
    }

    public Role[] getMembers() {
        try {
            StorageProvider storageProvider = getAdmin().getStorageProvider();
            Collection<Role> roles = storageProvider.getMembers(getAdmin(), this);
            if (!roles.isEmpty()) {
                 return roles.toArray(new Role[0]);
            }
        } catch (StorageException e) {
            getAdmin().logMessage(this,
                                   "error when retrieving basic members of group '" + getName() + "':"
                                  + e.getMessage(), LogService.LOG_ERROR);
        }
        return null;
    }

    public Role[] getRequiredMembers() {
        try {
            StorageProvider storageProvider = getAdmin().getStorageProvider();
            Collection<Role> roles = storageProvider.getRequiredMembers(getAdmin(), this);
            if (!roles.isEmpty()) {
                return roles.toArray(new Role[0]);
            }
        } catch (StorageException e) {
            getAdmin().logMessage(this,
                                    "error when retrieving required members of group '" + getName()
                                  + "':" + e.getMessage(), LogService.LOG_ERROR);
        }
        return null;
    }

    public int getType() {
        return Role.GROUP;
    }

    protected boolean isImpliedBy(Role role, Collection<String> checkedRoles) {
        if (checkedRoles.contains(getName())) {
            return (false);
        }
        checkedRoles.add(getName());
        if (getName().equals(role.getName())) {
            return (true);
        }
        //
        Collection<String> requiredCheckedRoles = new ArrayList<String>(checkedRoles);
        Collection<String> basicCheckedRoles = new ArrayList<String>(checkedRoles);
        Role[] members = getRequiredMembers();
        if (null != members) {
            for (Role member : members) {
                if (!((RoleImpl) member).isImpliedBy(role, requiredCheckedRoles)) {
                    return false;
                }
            }
        }
        
        members = getMembers();
        if (null != members) {
            for (Role member : members) {
                if (((RoleImpl) member).isImpliedBy(role, basicCheckedRoles)) {
                    return true;
                }
            }
        }
        return false;
    }
}