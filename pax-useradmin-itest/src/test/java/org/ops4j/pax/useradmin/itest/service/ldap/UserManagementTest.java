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

package org.ops4j.pax.useradmin.itest.service.ldap;

import static org.ops4j.pax.exam.CoreOptions.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.useradmin.itest.service.UserManagement;
import org.ops4j.pax.useradmin.provider.ldap.ConfigurationConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;

/**
 * Testing the user-management parts of the preferences service based
 * implementation of the UserAdmin service.
 *
 * @author Matthias Kuespert
 * @since 09.07.2009
 */
@RunWith(JUnit4TestRunner.class)
public class UserManagementTest extends UserManagement {

    @Inject
    private BundleContext m_context;

    protected BundleContext getBundleContext() {
        if (null == m_context) {
            throw new IllegalStateException("No bundle context injected");
        }
        return m_context;
    };

    @Override
    protected String getProviderType() {
        return ConfigurationConstants.STORAGEPROVIDER_TYPE;
    }

    @Configuration
    public static Option[] configure() {
        return options(getBasicFrameworkConfiguration(),
                       FrameworkConfiguration.get());
    }

    @Before
    public void setup() {
        FrameworkConfiguration.setup(getBundleContext(), doEnableSecurity());
    }

    @Test
    public void createAndFindUserOk() {
        super.createAndFindUserOk();
    }

    @Test
    public void createAndFindGroupOk() {
        super.createAndFindGroupOk();
    }

    @Test
    public void createAndRemoveUserOk() {
        super.createAndRemoveUserOk();
    }

    @Test
    public void createAndRemoveUserWithGroupsOk() {
        super.createAndRemoveUserWithGroupsOk();
    }

    @Test
    public void setAndGetStringAttributesOk() {
        super.setAndGetStringAttributesOk();
    }

// TODO: Disabled because the LDAP provider does not handle byte[] attributes correctly
//    @Test
//    public void setAndGetByteAttributesOk() {
//         super.setAndGetByteAttributesOk();
//    }

    @Test
    public void setAndGetUserCredentialsOk() {
        super.setAndGetUserCredentialsOk();
    }

    @Test
    public void setAndRemoveUserCredentialsOk() {
        super.setAndRemoveUserCredentialsOk();
    }

    @Test
    public void setAndGetGroupCredentialsOk() {
        super.setAndGetGroupCredentialsOk();
    }

    @Test
    public void setAndRemoveGroupCredentialsOk() {
        super.setAndRemoveGroupCredentialsOk();
    }
}
