/*
 * Copyright (C) Exclamation Labs 2019. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */

package com.exclamationlabs.connid.box;

import com.box.sdk.BoxAPIRequest;
import com.exclamationlabs.connid.box.testutil.LocalBoxConnector;
import com.exclamationlabs.connid.box.testutil.MockBoxAPIHelper;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.test.common.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static com.exclamationlabs.connid.box.testutil.TestUtils.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Hiroyuki Wada
 */
class TestTests {

    ConnectorFacade connector;
    MockBoxAPIHelper mockAPI;

    @BeforeEach
    void setup() {
        connector = newFacade();
        mockAPI = MockBoxAPIHelper.instance();
        mockAPI.init();
    }

    protected ConnectorFacade newFacade() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration impl = TestHelpers.createTestConfiguration(LocalBoxConnector.class, new BoxConfiguration());
        impl.getResultsHandlerConfiguration().setEnableAttributesToGetSearchResultsHandler(false);
        impl.getResultsHandlerConfiguration().setEnableNormalizingResultsHandler(false);
        impl.getResultsHandlerConfiguration().setEnableFilteredResultsHandler(false);
        return factory.newInstance(impl);
    }

    @Test
    void test() {
        // Given
        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);
            return ok("oauth2-token.json");
        });

        // When
        connector.test();

        // Then
        assertNotNull(request.get());
        assertEquals("/oauth2/token", request.get().getUrl().getPath());
    }
}
