/*
 * Copyright (C) Exclamation Labs 2019. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */

package com.exclamationlabs.connid.box;

import com.exclamationlabs.connid.box.testutil.LocalBoxConnector;
import com.exclamationlabs.connid.box.testutil.MockBoxAPIHelper;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.test.common.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hiroyuki Wada
 */
class SchemaTests {

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
    void schema() {
        // Given

        // When
        Schema schema = connector.schema();

        // Then
        assertNotNull(schema);
        assertEquals(2, schema.getObjectClassInfo().size());

        Optional<ObjectClassInfo> user = schema.getObjectClassInfo().stream()
                .filter(o -> o.is(UsersHandler.OBJECT_CLASS_USER.getObjectClassValue()))
                .findFirst();
        Optional<ObjectClassInfo> group = schema.getObjectClassInfo().stream()
                .filter(o -> o.is(GroupsHandler.OBJECT_CLASS_GROUP.getObjectClassValue()))
                .findFirst();

        assertTrue(user.isPresent());
        assertTrue(group.isPresent());
    }
}
