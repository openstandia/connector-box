/*
 * Copyright (C) Exclamation Labs 2019. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */

package com.exclamationlabs.connid.box;

import com.box.sdk.*;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.*;
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

    protected BoxAPIConnection boxAPI;

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
        boxAPI = boxDeveloperEditionAPIConnection;
    }

    @Override
    public void dispose() {
        this.boxAPI = null;
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

        try {
            if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
                UsersHandler usersHandler = new UsersHandler(boxAPI);
                return usersHandler.createUser(createAttributes);

            } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
                GroupsHandler groupsHandler = new GroupsHandler(boxAPI);
                return groupsHandler.createGroup(createAttributes);
            }
        } catch (RuntimeException e) {
            throw processRuntimeException(e);
        }

        throw new UnsupportedOperationException("Unsupported object class " + objectClass);
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

        try {
            if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
                UsersHandler usersHandler = new UsersHandler(boxAPI);
                return usersHandler.updateUser(uid, modifications);

            } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
                GroupsHandler groupsHandler = new GroupsHandler(boxAPI);
                return groupsHandler.updateGroup(uid, modifications);
            }
        } catch (RuntimeException e) {
            throw processRuntimeException(e);
        }

        throw new UnsupportedOperationException("Unsupported object class " + objectClass);
    }

    @Override
    public void delete(
            final ObjectClass objectClass,
            final Uid uid,
            final OperationOptions options) {

        try {
            if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
                UsersHandler usersHandler = new UsersHandler(boxAPI);
                usersHandler.deleteUser(objectClass, uid, options);
                return;

            } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
                GroupsHandler groupsHandler = new GroupsHandler(boxAPI);
                groupsHandler.deleteGroup(uid);
                return;
            }
        } catch (RuntimeException e) {
            throw processRuntimeException(e);
        }

        throw new UnsupportedOperationException("Unsupported object class " + objectClass);
    }

    @Override
    public Schema schema() {
        if (null == schema) {
            SchemaBuilder schemaBuilder = new SchemaBuilder(BoxConnector.class);

            UsersHandler usersHandler = new UsersHandler(boxAPI);
            ObjectClassInfo userSchemaInfo = usersHandler.getUserSchema();
            schemaBuilder.defineObjectClass(userSchemaInfo);

            GroupsHandler group = new GroupsHandler(boxAPI);
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

        if (!boxAPI.canRefresh()) {
            throw new ConnectorIOException("Cannot refresh auth token");
        }

        try {
            boxAPI.refresh();
        } catch (RuntimeException e) {
            throw processRuntimeException(e);
        }
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

        try {
            if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
                UsersHandler usersHandler = new UsersHandler(boxAPI);
                usersHandler.query(filter, handler, options);
                return;

            } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
                GroupsHandler groupsHandler = new GroupsHandler(boxAPI);
                groupsHandler.query(filter, handler, options);
                return;
            }
        } catch (RuntimeException e) {
            throw processRuntimeException(e);
        }

        throw new UnsupportedOperationException("Unsupported object class " + objectClass);
    }

    protected ConnectorException processRuntimeException(RuntimeException e) {
        if (e instanceof ConnectorException) {
            return (ConnectorException) e;
        }
        if (e instanceof BoxAPIResponseException) {
            return processBoxAPIResponseException((BoxAPIResponseException) e);

        } else if (e instanceof BoxAPIException) {
            return new ConnectorIOException(e);
        }
        return new ConnectorException(e);
    }

    private ConnectorException processBoxAPIResponseException(BoxAPIResponseException e) {
        // https://developer.box.com/guides/api-calls/permissions-and-errors/common-errors/

        switch (e.getResponseCode()) {
            case 400:
                return new InvalidAttributeValueException(e);
            case 401:
                return new ConnectorSecurityException(e);
            case 403:
                return new PermissionDeniedException(e);
            case 404:
                return new UnknownUidException(e);
            case 405:
                return new InvalidAttributeValueException(e);
            case 409:
                return new AlreadyExistsException(e);
            case 410:
                return new ConnectorSecurityException(e);
            case 411:
                return new InvalidAttributeValueException(e);
            case 412:
                return RetryableException.wrap(e.getMessage(), e);
            case 413:
                return new InvalidAttributeValueException(e);
            case 415:
                return new InvalidAttributeValueException(e);
            case 429:
                return RetryableException.wrap(e.getMessage(), e);
            case 500:
                return RetryableException.wrap(e.getMessage(), e);
            case 502:
                return RetryableException.wrap(e.getMessage(), e);
            case 503:
                return RetryableException.wrap(e.getMessage(), e);
            default:
                return new ConnectorIOException(e);
        }
    }
}
