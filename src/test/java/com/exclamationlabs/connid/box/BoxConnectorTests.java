/*
 * Copyright (C) Exclamation Labs 2019. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */

package com.exclamationlabs.connid.box;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxJSONResponse;
import com.eclipsesource.json.JsonObject;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.test.common.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.exclamationlabs.connid.box.TestUtils.createOK;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class BoxConnectorTests {

    private MockBoxAPIConnection mockApi;

    protected BoxConfiguration newConfiguration() {
        return new BoxConfiguration();
    }

    private static final Log LOG = Log.getLog(BoxConnectorTests.class);

    private static ArrayList<ConnectorObject> results = new ArrayList<>();

    private static BoxAPIConnection boxAPIConnection = null;
    private static BoxConfig boxConfig = null;


    @BeforeEach
    public void setup() {
//        try (Reader reader = new FileReader("test-config.json")) {
//            boxConfig = BoxConfig.readFrom(reader);
//        } catch (IOException ex) {
//            LOG.error("Error loading test credentials", ex);
//        }
//
//        assertNotNull(boxConfig, "Error loading test credentials; boxConfig was null");
//
//        boxAPIConnection = new BoxAPIConnection(boxConfig);
//        assertNotNull(boxAPIConnection);

        mockApi = MockBoxAPIConnection.instance();
    }

    protected ConnectorFacade newFacade() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration impl = TestHelpers.createTestConfiguration(TestBoxConnector.class, newConfiguration());
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(true);

        // Even though we already have a connection from setup(), we are creating one through the connector as another test
//        LOG.info("Setting client id {0}", boxConfig.getClientId());
//        impl.getConfigurationProperties().setPropertyValue("configFilePath", "test-config.json" );

        return factory.newInstance(impl);
    }

    public static SearchResultsHandler handler = new SearchResultsHandler() {
        @Override
        public boolean handle(ConnectorObject connectorObject) {
            results.add(connectorObject);
            return true;
        }

        @Override
        public void handleResult(SearchResult result) {
            LOG.info("Im handling {0}", result.getRemainingPagedResults());
        }
    };

    @Test
    public void schema() {
        Schema schema = newFacade().schema();
        assertNotNull(schema);
    }

    @Test
    public void test() {
        newFacade().test();
        assertTrue(true);
    }

    @Test
    public void validate() {
        newFacade().validate();
        assertTrue(true);
    }

    @Test
    void createUser() {
        // Given
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new Name("test@example.com"));
        attributes.add(AttributeBuilder.build("name", "test"));

        mockApi.mock(req -> {
            return createOK("user-create.json");
        });

        // When
        Uid uid = newFacade().create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build());

        // Then
    }
}
