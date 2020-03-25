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
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.test.common.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.exclamationlabs.connid.box.UsersHandler.OBJECT_CLASS_USER;
import static com.exclamationlabs.connid.box.testutil.TestUtils.enc;
import static com.exclamationlabs.connid.box.testutil.TestUtils.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Hiroyuki Wada
 */
class UserSearchTests {

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
    void searchAllUser_1() {
        // Given
        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return ok("user-list-1.json");
        });
        mockAPI.push(req -> {
            return ok("user-membership-0.json");
        });

        List<ConnectorObject> users = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            users.add(connectorObject);
            return true;
        };

        // When
        connector.search(OBJECT_CLASS_USER,
                null,
                handler,
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(1, users.size());
        assertEquals(OBJECT_CLASS_USER, users.get(0).getObjectClass());
        assertEquals("11446498", users.get(0).getUid().getUidValue());
        assertEquals("ceo@example.com", users.get(0).getName().getNameValue());
        assertEquals("Aaron Levie", users.get(0).getAttributeByName("name").getValue().get(0));
    }

    @Test
    void searchAllUser_2() {
        // Given
        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return ok("user-list-2.json");
        });
        mockAPI.push(req -> {
            return ok("user-membership-0.json");
        });
        mockAPI.push(req -> {
            return ok("user-membership-0.json");
        });

        List<ConnectorObject> users = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            users.add(connectorObject);
            return true;
        };

        // When
        connector.search(OBJECT_CLASS_USER,
                null,
                handler,
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(2, users.size());
        assertEquals(OBJECT_CLASS_USER, users.get(0).getObjectClass());
        assertEquals("11446498", users.get(0).getUid().getUidValue());
        assertEquals("ceo@example.com", users.get(0).getName().getNameValue());
        assertEquals("Aaron Levie", users.get(0).getAttributeByName("name").getValue().get(0));
        assertEquals(OBJECT_CLASS_USER, users.get(1).getObjectClass());
        assertEquals("12345678", users.get(1).getUid().getUidValue());
        assertEquals("foo@example.com", users.get(1).getName().getNameValue());
        assertEquals("Foo Bar", users.get(1).getAttributeByName("name").getValue().get(0));
    }

    @Test
    void searchAllUser_empty() {
        // Given
        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return ok("user-list-0.json");
        });

        List<ConnectorObject> users = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            users.add(connectorObject);
            return true;
        };

        // When
        connector.search(OBJECT_CLASS_USER,
                null,
                handler,
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(0, users.size());
    }

    @Test
    void searchUserByName() throws UnsupportedEncodingException {
        // Given
        String uid = "11446498";
        String login = "ceo@example.com";

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return ok("user-list-1.json");
        });
        mockAPI.push(req -> {
            return ok("user-membership-0.json");
        });

        List<ConnectorObject> users = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            users.add(connectorObject);
            return true;
        };

        // When
        connector.search(OBJECT_CLASS_USER,
                new EqualsFilter(new Name(login)),
                handler,
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(String.format("filter_term=%s&limit=1000&offset=0", enc(login)), request.get().getUrl().getQuery());
        assertEquals(1, users.size());
        assertEquals(OBJECT_CLASS_USER, users.get(0).getObjectClass());
        assertEquals("11446498", users.get(0).getUid().getUidValue());
        assertEquals(login, users.get(0).getName().getNameValue());
        assertEquals("Aaron Levie", users.get(0).getAttributeByName("name").getValue().get(0));
    }
}
