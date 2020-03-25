/*
 * Copyright (C) Exclamation Labs 2019. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */

package com.exclamationlabs.connid.box;

import com.box.sdk.BoxAPIRequest;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.test.common.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.exclamationlabs.connid.box.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;


class UserTest {

    ConnectorFacade connector;
    MockBoxAPIConnection mockApi;

    @BeforeEach
    void setup() {
        connector = newFacade();
        mockApi = MockBoxAPIConnection.instance();
        mockApi.init();
    }

    protected ConnectorFacade newFacade() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration impl = TestHelpers.createTestConfiguration(TestBoxConnector.class, new BoxConfiguration());
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(true);
        return factory.newInstance(impl);
    }

    @Test
    void createUser() {
        // Given
        String login = "ceo@example.com";
        String name = "Aaron Levie";

        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new Name(login));
        attributes.add(AttributeBuilder.build("name", name));

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockApi.mock(req -> {
            request.set(req);

            return createOK("user-create.json");
        });

        // When
        Uid uid = connector.create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(login, getJsonAttr(request.get(), "login"));
        assertEquals(name, getJsonAttr(request.get(), "name"));
        assertEquals("11446498", uid.getUidValue());
        assertEquals(login, uid.getNameHintValue());
    }

    @Test
    void updateUser() {
        // Given
        String login = "ceo@example.com";
        String name = "Aaron Levie";

        Set<AttributeDelta> modifications = new HashSet<>();
        modifications.add(AttributeDeltaBuilder.build("job_title", "CTO"));

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockApi.mock(req -> {
            request.set(req);

            return updateOK("user-update.json");
        });

        // When
        Set<AttributeDelta> sideEffects = connector.updateDelta(ObjectClass.ACCOUNT,
                new Uid("11446498", new Name(login)),
                modifications, new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals("CTO", getJsonAttr(request.get(), "job_title"));
        assertNull(sideEffects);
    }

    @Test
    void deleteUser() {
        // Given
        String uid = "11446498";
        String login = "ceo@example.com";

        Set<AttributeDelta> modifications = new HashSet<>();
        modifications.add(AttributeDeltaBuilder.build("job_title", "CTO"));

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockApi.mock(req -> {
            request.set(req);

            return deleteOK();
        });

        // When
        connector.delete(ObjectClass.ACCOUNT,
                new Uid(uid, new Name(login)),
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals("DELETE", request.get().getMethod());
        assertEquals("/2.0/users/" + uid, request.get().getUrl().getPath());
        assertNull(request.get().getBody());
    }

    @Test
    void getUser() {
        // Given
        String uid = "11446498";
        String login = "ceo@example.com";
        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockApi.mock(req -> {
            request.set(req);

            return getOK("user-get.json");
        });
        mockApi.mock(req -> {
            return getOK("user-membership-0.json");
        });

        // When
        ConnectorObject result = connector.getObject(ObjectClass.ACCOUNT,
                new Uid(uid, new Name(login)),
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals("/2.0/users/" + uid ,request.get().getUrl().getPath());
        assertEquals(ObjectClass.ACCOUNT, result.getObjectClass());
        assertEquals(uid, result.getUid().getUidValue());
        assertEquals(login, result.getName().getNameValue());
        assertNotNull(result.getAttributeByName("role"));
        assertEquals("admin", result.getAttributeByName("role").getValue().get(0));
    }

    @Test
    void getAllUsers() {
        // Given
        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockApi.mock(req -> {
            request.set(req);

            return getOK("user-list-1.json");
        });
        mockApi.mock(req -> {
            return getOK("user-membership-0.json");
        });

        List<ConnectorObject> users = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            users.add(connectorObject);
            return true;
        };

        // When
        connector.search(ObjectClass.ACCOUNT,
                null, handler, new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(1, users.size());
        assertEquals(ObjectClass.ACCOUNT, users.get(0).getObjectClass());
        assertEquals("11446498", users.get(0).getUid().getUidValue());
        assertEquals("ceo@example.com", users.get(0).getName().getNameValue());
        assertEquals("Aaron Levie", users.get(0).getAttributeByName("name").getValue().get(0));
    }
}
