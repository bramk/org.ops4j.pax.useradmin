/**
 * Copyright 2009 OPS4J
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ops4j.pax.useradmin.service.spi;

import java.util.Map;

import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.User;

/**
 * An interface which provides methods to create implementation objects. This
 * interface is intended to be used only by implementations of the
 * <code>StorageProvider</code> interface.
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public interface UserAdminFactory {

    /**
     * Create a <code>User</code> instance.
     * 
     * @param name The name of the user.
     * @param properties The properties of the user.
     * @param credentials The credentials of the user.
     * @return A new <code>User</code> instance.
     */
    User createUser(String name, Map<String, Object> properties, Map<String, Object> credentials);

    /**
     * Create a <code>Group</code> instance.
     * 
     * @param name The name of the group.
     * @param properties The properties of the group.
     * @param credentials The credentials of the group.
     * @return A new <code>Group</code> instance.
     */
    Group createGroup(String name, Map<String, Object> properties, Map<String, Object> credentials);
}
