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
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.Assert;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.ldapserver.apacheds.ApacheDSConfiguration;
import org.ops4j.pax.ldapserver.apacheds.ApacheDSServer;
import org.ops4j.pax.useradmin.Utilities;
import org.ops4j.pax.useradmin.itest.service.CopyFilesEnvironmentCustomizer;
import org.ops4j.pax.useradmin.provider.ldap.ConfigurationConstants;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Provides configuration specific to the LDAP based variant of
 * the UserAdmin service.
 * 
 * @author Matthias Kuespert
 * @since 14.07.2009
 */
public class FrameworkConfiguration {
    
    /**
     * @return The additional configuration needed for testing the LDAP
     *         service based variant of the UserAdmin service
     */
    protected static Option get() {
        return composite(rawPaxRunnerOption("bootDelegation", "javax.naming.ldap.*"),
                         new CopyFilesEnvironmentCustomizer().sourceDir("src/test/resources")
                                                             .sourceFilter(".*.ldif")
                                                             .targetDir("/server-data"),
                         mavenBundle().groupId("org.ops4j.pax.ldapserver")
                                      .artifactId("pax-ldapserver-apacheds")
                                      .versionAsInProject().startLevel(4),
                         mavenBundle().groupId("org.ops4j.pax.useradmin")
                                      .artifactId("pax-useradmin-provider-ldap")
                                      .versionAsInProject().startLevel(5));
    }
    
    /**
     * Standard initialization for all tests: update configuration for the StorageProvider bundle
     * 
     * @param context The <code>BundleContext</code> of the bundle containing the test.
     * @param enableSecurity True if security should be enabled.
     */
    @SuppressWarnings(value = "unchecked")
    protected static void setup(BundleContext context, boolean enableSecurity) {
        
        ServiceReference refConfigAdmin = context.getServiceReference(ConfigurationAdmin.class.getName());
        Assert.assertNotNull("No ConfigurationAdmin service reference found", refConfigAdmin);
        ConfigurationAdmin configAdmin = (ConfigurationAdmin) context.getService(refConfigAdmin);
        Assert.assertNotNull("No ConfigurationAdmin service found", configAdmin);
        //
        // configure Pax Logging
        //
        ServiceReference refLog = context.getServiceReference(LogService.class.getName());
        Assert.assertNotNull("No LogService reference found", refLog);
        //
        try {
            Configuration config = configAdmin.getConfiguration("org.ops4j.pax.logging",
                                                                refLog.getBundle().getLocation());
            Dictionary<String, String> properties = config.getProperties();
            if (null == properties) {
                properties = new Hashtable<String, String>();
            }
            properties.put("log4j.rootLogger",                           "INFO, A1");
            properties.put("log4j.appender.A1",                          "org.apache.log4j.ConsoleAppender");
            properties.put("log4j.appender.A1.layout",                   "org.apache.log4j.PatternLayout");
            properties.put("log4j.appender.A1.layout.ConversionPattern", "%d [%t] %-5p %c - %m%n");
            properties.put("log4j.logger.org.ops4j",                     "DEBUG");
            config.update(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
        // clean ApacheDS working directory
        //
        File workDir = new File("./server-work");
        if (workDir.exists() && workDir.isDirectory()) {
            Utilities.delete(workDir, true);
        }
        //
        // clean/initialize data directory
        //
//        File dataDir = new File("./server-data");
//        if (dataDir.exists() && dataDir.isDirectory()) {
//            Utilities.delete(dataDir, false);
//        } else {
//            dataDir.mkdir();
//        }
//        Utilities.copyResourceToFile("/ldaptree-test.ldif", dataDir);
        //
        // configure ApacheDS LDAP server
        //
        try {
            ServiceReference refLDAPServer = context.getServiceReference(ApacheDSServer.class.getName());
            Assert.assertNotNull("No LDAP server service reference found", refLDAPServer);
            //
            Configuration config = configAdmin.getConfiguration(ApacheDSConfiguration.SERVICE_PID,
                                                                refLDAPServer.getBundle().getLocation());
            Dictionary<String, String> properties = config.getProperties();
            if (null == properties) {
                properties = new Hashtable<String, String>();
            }
            properties.put(ApacheDSConfiguration.PROP_DATA_DIR,
                           "server-data"); // dataDir.getPath());
            properties.put(ApacheDSConfiguration.PROP_LDAP_SERVER_PORT,
                           ApacheDSConfiguration.DEFAULT_PORT_LDAP);
            properties.put(ApacheDSConfiguration.PROP_PARTITIONS,
                           "ops4j");
            properties.put(ApacheDSConfiguration.PROP_PARTITION_DN_STUB + "ops4j",
                           "dc=ops4j,dc=org");
            config.update(properties);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        //
        // wait for ApacheDS to be ready ...
        //
        ServiceTracker apacheDSTracker = new ServiceTracker(context,
                                                            context.getServiceReference(ApacheDSServer.class.getName()),
                                                            null);
        apacheDSTracker.open();
        try {
            apacheDSTracker.waitForService(5000);
        } catch (InterruptedException e) {
            // ignore
        }
        ApacheDSServer server = (ApacheDSServer) apacheDSTracker.getService();
        long waitTime = 50000;
        long interval = 500;
        while (!server.isAvailable()) {
            try {
                Thread.sleep(interval);
                waitTime -= interval;
                System.err.println("... waiting for ApacheDS ...");
                if (waitTime < 0) {
                    Assert.fail("... timeout: ApacheDS still not ready.");
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
                break;
            }
        }
        //
        // configure LDAP storage provider
        //
        ServiceReference refProvider = context.getServiceReference(StorageProvider.class.getName());
        Assert.assertNotNull("No StorageProvider service reference found", refProvider);
        //
        try {
            Configuration config = configAdmin.getConfiguration(ConfigurationConstants.SERVICE_PID,
                                                                refProvider.getBundle().getLocation());
            Dictionary<String, String> properties = config.getProperties();
            if (null == properties) {
                properties = new Hashtable<String, String>();
            }
            properties.put(ConfigurationConstants.PROP_LDAP_SERVER_URL,
                           ConfigurationConstants.DEFAULT_LDAP_SERVER_URL);
            properties.put(ConfigurationConstants.PROP_LDAP_SERVER_PORT,
                           ConfigurationConstants.DEFAULT_LDAP_SERVER_PORT);
            properties.put(ConfigurationConstants.PROP_LDAP_ROOT_DN,
                           ConfigurationConstants.DEFAULT_LDAP_ROOT_DN);
            config.update(properties);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
        //
        // configure UserAdmin service
        //
        /*
        ServiceReference refAdmin = context.getServiceReference(UserAdmin.class.getName());
        Assert.assertNotNull("No UserAdmin service reference found", refAdmin);
        //
        try {
            Configuration config = configAdmin.getConfiguration(UserAdminConstants.SERVICE_PID + "." + ConfigurationConstants.STORAGEPROVIDER_TYPE,
                                                                refAdmin.getBundle().getLocation());
            Dictionary<String, String> properties = config.getProperties();
            if (null == properties) {
                properties = new Hashtable<String, String>();
            }
            properties.put(UserAdminConstants.PROP_SECURITY, enableSecurity ? "true" : "false");
            config.update(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
    
    protected static void cleanup() {
    }
}
