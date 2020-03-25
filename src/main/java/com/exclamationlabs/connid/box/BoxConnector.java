/*
 * Copyright (C) Exclamation Labs 2019. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */

package com.exclamationlabs.connid.box;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.DeveloperEditionEntityType;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Set;


@ConnectorClass(configurationClass = BoxConfiguration.class, displayNameKey = "Exclamation Labs Box Connector")
public class BoxConnector implements Connector,
        CreateOp, UpdateDeltaOp, DeleteOp, SchemaOp, TestOp, SearchOp<BoxFilter> {

    private static final Log LOG = Log.getLog(BoxConnector.class);

    private BoxConfiguration configuration;

    protected BoxAPIConnection api;

    private Schema schema;

    private BoxConfig boxConfig;

    @Override
    public BoxConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void init(final Configuration configuration) {
        this.configuration = (BoxConfiguration) configuration;
        this.boxConfig = null;
        authenticateResource();

        LOG.ok("Connector {0} successfully inited", getClass().getName());
    }

    protected void authenticateResource() {
        String configFilePath = getConfiguration().getConfigFilePath();

        try (Reader reader = new FileReader(configFilePath)) {
            boxConfig = BoxConfig.readFrom(reader);
        } catch (IOException ex) {
            LOG.error("Error loading Box JWT Auth Config File", ex);
        }

        final BoxDeveloperEditionAPIConnection boxDeveloperEditionAPIConnection;
        try {
            if (StringUtil.isEmpty(getConfiguration().getHttpProxyHost())) {
                boxDeveloperEditionAPIConnection = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig);
            } else {
                // Use HTTP Proxy for Box connection
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getConfiguration().getHttpProxyHost(),
                        getConfiguration().getHttpProxyPort()));
                if (StringUtil.isNotEmpty(getConfiguration().getHttpProxyUser())) {
                    boxDeveloperEditionAPIConnection = new BoxDeveloperEditionAPIConnection(boxConfig.getEnterpriseId(), DeveloperEditionEntityType.ENTERPRISE,
                            boxConfig.getClientId(), boxConfig.getClientSecret(), boxConfig.getJWTEncryptionPreferences());
                    boxDeveloperEditionAPIConnection.setProxyUsername(getConfiguration().getHttpProxyUser());

                    if (getConfiguration().getHttpProxyPassword() != null) {
                        getConfiguration().getHttpProxyPassword().access(new GuardedString.Accessor() {
                            @Override
                            public void access(char[] chars) {
                                boxDeveloperEditionAPIConnection.setProxyPassword(String.valueOf(chars));
                            }
                        });
                    }
                } else {
                    boxDeveloperEditionAPIConnection = new BoxDeveloperEditionAPIConnection(boxConfig.getEnterpriseId(), DeveloperEditionEntityType.ENTERPRISE,
                            boxConfig.getClientId(), boxConfig.getClientSecret(), boxConfig.getJWTEncryptionPreferences());
                    boxDeveloperEditionAPIConnection.setProxy(proxy);
                    boxDeveloperEditionAPIConnection.authenticate();
                }
            }
        } catch (Exception e) {
            throw new ConnectorIOException("Failed to connect", e);
        }
        api = boxDeveloperEditionAPIConnection;
    }

    @Override
    public void dispose() {
        this.api = null;
    }

    @Override
    public Uid create(
            final ObjectClass objectClass,
            final Set<Attribute> createAttributes,
            final OperationOptions options) {

        if (objectClass == null) {
            throw new InvalidAttributeValueException("ObjectClass value not provided");
        }
        LOG.info("CREATE METHOD OBJECTCLASS VALUE: {0}", objectClass);

        if (createAttributes == null) {
            throw new InvalidAttributeValueException("Attributes not provided or empty");
        }

        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            UsersHandler usersHandler = new UsersHandler(api);
            return usersHandler.createUser(createAttributes);

        } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
            GroupsHandler groupsHandler = new GroupsHandler(api);
            return groupsHandler.createGroup(createAttributes);

        } else {
            throw new UnsupportedOperationException("Unsupported object class " + objectClass);
        }

    }

    @Override
    public Set<AttributeDelta> updateDelta(
            final ObjectClass objectClass,
            final Uid uid, Set<AttributeDelta> modifications,
            final OperationOptions options) {

        if (objectClass == null) {
            throw new InvalidAttributeValueException("ObjectClass value not provided");
        }
        LOG.info("UPDATEDELTA METHOD OBJECTCLASS VALUE: {0}", objectClass);

        if (modifications == null) {
            throw new InvalidAttributeValueException("modifications not provided or empty");
        }

        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            UsersHandler usersHandler = new UsersHandler(api);
            return usersHandler.updateUser(uid, modifications);

        } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
            GroupsHandler groupsHandler = new GroupsHandler(api);
            return groupsHandler.updateGroup(uid, modifications);
        }

        throw new UnsupportedOperationException("Unsupported object class " + objectClass);
    }

    @Override
    public void delete(
            final ObjectClass objectClass,
            final Uid uid,
            final OperationOptions options) {


        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            UsersHandler usersHandler = new UsersHandler(api);
            usersHandler.deleteUser(objectClass, uid, options);

        } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
            GroupsHandler groupsHandler = new GroupsHandler(api);
            groupsHandler.deleteGroup(uid);

        } else {
            throw new UnsupportedOperationException("Unsupported object class " + objectClass);
        }

    }

    @Override
    public Schema schema() {
        if (null == schema) {
            SchemaBuilder schemaBuilder = new SchemaBuilder(BoxConnector.class);

            UsersHandler usersHandler = new UsersHandler(api);
            ObjectClassInfo userSchemaInfo = usersHandler.getUserSchema();
            schemaBuilder.defineObjectClass(userSchemaInfo);

            GroupsHandler group = new GroupsHandler(api);
            ObjectClassInfo groupSchemaInfo = group.getGroupSchema();
            schemaBuilder.defineObjectClass(groupSchemaInfo);

            return schemaBuilder.build();
        }
        return this.schema;
    }

    @Override
    public void test() {

        dispose();

        authenticateResource();

        if (!api.canRefresh()) {
            throw new ConnectorIOException("Cannot refresh auth token");
        }

        api.refresh();

    }

    @Override
    public FilterTranslator<BoxFilter> createFilterTranslator(
            final ObjectClass objectClass,
            final OperationOptions options) {

        return new BoxFilterTranslator();
    }

    @Override
    public void executeQuery(
            final ObjectClass objectClass,
            final BoxFilter filter,
            final ResultsHandler handler,
            final OperationOptions options) {

        if (objectClass == null) {
            throw new InvalidAttributeValueException("ObjectClass value not provided");
        }

        LOG.info("EXECUTE_QUERY METHOD OBJECTCLASS VALUE: {0}", objectClass);

        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            UsersHandler usersHandler = new UsersHandler(api);
            usersHandler.query(filter, handler, options);

        } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
            GroupsHandler groupsHandler = new GroupsHandler(api);
            groupsHandler.query(filter, handler, options);

        } else {
            throw new UnsupportedOperationException("Unsupported object class " + objectClass);
        }
    }
}
