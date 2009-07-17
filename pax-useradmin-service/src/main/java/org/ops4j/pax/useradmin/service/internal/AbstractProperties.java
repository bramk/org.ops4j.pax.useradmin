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

import java.util.Map;
import java.util.Hashtable;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.UserAdminEvent;

/**
 * Abstract base class for properties that need to synchronize and communicate
 * changes.
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
@SuppressWarnings(value = "unchecked")
public abstract class AbstractProperties extends Hashtable {

    private static final long serialVersionUID = 1L;

    private Role              m_role           = null;

    private UserAdminUtil     m_util           = null;

    protected Role getRole() {
        return m_role;
    }

    /**
     * @return The UserAdminUtil implementation used here.
     */
    protected UserAdminUtil getUtil() {
        return m_util;
    }

    protected abstract void store(StorageProvider storageProvider, String key, String value)
    throws StorageException;

    protected abstract void store(StorageProvider storageProvider, String key, byte[] value)
    throws StorageException;

    protected abstract void remove(StorageProvider storageProvider, String key)
        throws StorageException;

    protected abstract void clear(StorageProvider storageProvider) throws StorageException;

    public AbstractProperties(Role role, UserAdminUtil util, Map<String, String> properties) {
        if (null == role) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_ROLE);
        }
        if (null == util) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_USERADMIN);
        }
        m_role = role;
        m_util = util;
        //
        // initialize from storage
        //
        if (null != properties) {
            for (String key : properties.keySet()) {
                String value = (String) properties.get(key);
                super.put(key, value);
            }
        }
    }
    
    /**
     * To be overridden by implementations that need security checks on the get
     * method(s), e.g. for access to credentials.
     * 
     * @param key The key for permission checks.
     */
    protected void checkGetPermission(String key) {
    }

    @Override
    public synchronized Object get(Object key) {
        if (null == key) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY);
        }
        if (!(key instanceof String)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY_TYPE);
        }
        checkGetPermission((String) key);
        return super.get(key);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        if (null == key) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY);
        }
        if (!(key instanceof String)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY_TYPE);
        }
        if (null == key) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_VALUE);
        }
        if (!(value instanceof String || value instanceof byte[])) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_VALUE_TYPE);
        }
        try {
            StorageProvider storageProvider = m_util.getStorageProvider();
            if (value instanceof String) {
                store(storageProvider, (String) key, (String) value);
            } else {
                store(storageProvider, (String) key, (byte[]) value);
            }
            m_util.fireEvent(UserAdminEvent.ROLE_CHANGED, m_role);
            return super.put(key, value);
        } catch (StorageException e) {
            m_util.logMessage(this, e.getMessage(), LogService.LOG_ERROR);
        }
        return null;
    }

    @Override
    public synchronized Object remove(Object key) {
        if (key == null) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY);
        }
        if (!(key instanceof String)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY_TYPE);
        }
        try {
            StorageProvider storageProvider = m_util.getStorageProvider();
            remove(storageProvider, (String) key);
            m_util.fireEvent(UserAdminEvent.ROLE_CHANGED, m_role);
            return super.remove(key);
        } catch (StorageException e) {
            m_util.logMessage(this, e.getMessage(), LogService.LOG_ERROR);
        }
        return null;
    }

    @Override
    public synchronized void clear() {
        try {
            StorageProvider storageProvider = m_util.getStorageProvider();
            clear(storageProvider);
            m_util.fireEvent(UserAdminEvent.ROLE_CHANGED, m_role);
            super.clear();
        } catch (StorageException e) {
            m_util.logMessage(this, e.getMessage(), LogService.LOG_ERROR);
        }
    }
}
