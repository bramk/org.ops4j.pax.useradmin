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

package org.ops4j.pax.useradmin.command.spi;

import org.ops4j.pax.useradmin.command.CommandException;

/**
 * Interface which abstracts reading UserAdmin data.
 * 
 * @author Matthias Kuespert
 * @since  05.08.2009
 */
public interface UserAdminDataReader {

    /**
     * Copies data from the source using the given UserAdminDataWriter instance.
     * 
     * @param sourceId
     * @param targetWriter
     * @throws CommandException
     */
    void copy(String sourceId, UserAdminDataWriter targetWriter) throws CommandException;
}
