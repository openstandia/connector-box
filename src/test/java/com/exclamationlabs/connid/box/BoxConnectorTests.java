/*
 * Copyright (C) Exclamation Labs 2019. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */

package com.exclamationlabs.connid.box;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxConfig;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.test.common.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class BoxConnectorTests {

    protected BoxConfiguration newConfiguration() {
        return new BoxConfiguration();
    }

    private static final Log LOG = Log.getLog(BoxConnectorTests.class);

    private static ArrayList<ConnectorObject> results = new ArrayList<>();

    private static BoxAPIConnection boxAPIConnection = null;
    private static BoxConfig boxConfig = null;


    @BeforeEach
    public void setup() {

        try(Reader reader = new FileReader("test-config.json")) {
            boxConfig = BoxConfig.readFrom(reader);
        } catch (IOException ex) {
            LOG.error("Error loading test credentials", ex);
        }

        assertNotNull(boxConfig, "Error loading test credentials; boxConfig was null");


        boxAPIConnection = new BoxAPIConnection(boxConfig);
        assertNotNull(boxAPIConnection);
    }


    protected ConnectorFacade newFacade() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration impl = TestHelpers.createTestConfiguration(BoxConnector.class, newConfiguration());
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(true);


        // Even though we already have a connection from setup(), we are creating one through the connector as another test
        LOG.info("Setting client id {0}", boxConfig.getClientId());
        impl.getConfigurationProperties().setPropertyValue("configFilePath", "test-config.json" );

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





}
