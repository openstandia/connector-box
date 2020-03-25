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
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.test.common.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.exclamationlabs.connid.box.testutil.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hiroyuki Wada
 */
class UserTests {

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
    void createUser() {
        // Given
        String login = "ceo@example.com";
        String name = "Aaron Levie";

        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new Name(login));
        attributes.add(AttributeBuilder.build("name", name));

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return created("user-create.json");
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
    void createUser_alreadyExists() {
        // Given
        String login = "ceo@example.com";
        String name = "Aaron Levie";

        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new Name(login));
        attributes.add(AttributeBuilder.build("name", name));

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            throw conflict();
        });

        // When
        AlreadyExistsException e = assertThrows(AlreadyExistsException.class, () -> {
            Uid uid = connector.create(ObjectClass.ACCOUNT, attributes, new OperationOptionsBuilder().build());
        });

        // Then
        assertNotNull(e);
    }

    @Test
    void updateUser() {
        // Given
        String login = "ceo@example.com";
        String name = "Aaron Levie";

        Set<AttributeDelta> modifications = new HashSet<>();
        modifications.add(AttributeDeltaBuilder.build("job_title", "CTO"));

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return ok("user-update.json");
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
    void updateUser_notFound() {
        // Given
        String login = "ceo@example.com";

        Set<AttributeDelta> modifications = new HashSet<>();
        modifications.add(AttributeDeltaBuilder.build("job_title", "CTO"));

        mockAPI.push(req -> {
            throw notFound();
        });

        // When
        UnknownUidException e = assertThrows(UnknownUidException.class, () -> {
            Set<AttributeDelta> sideEffects = connector.updateDelta(ObjectClass.ACCOUNT,
                    new Uid("11446498", new Name(login)),
                    modifications, new OperationOptionsBuilder().build());
        });

        // Then
        assertNotNull(e);
    }

    @Test
    void deleteUser() {
        // Given
        String uid = "11446498";
        String login = "ceo@example.com";

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return noContent();
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
    void deleteUser_notFound() {
        // Given
        String uid = "11446498";
        String login = "ceo@example.com";

        mockAPI.push(req -> {
            throw notFound();
        });

        // When
        UnknownUidException e = assertThrows(UnknownUidException.class, () -> {
            connector.delete(ObjectClass.ACCOUNT,
                    new Uid(uid, new Name(login)),
                    new OperationOptionsBuilder().build());

        });

        // Then
        assertNotNull(e);
    }

    @Test
    void getUser() {
        // Given
        String uid = "11446498";
        String login = "ceo@example.com";

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return ok("user-get.json");
        });
        mockAPI.push(req -> {
            return ok("user-membership-0.json");
        });

        // When
        ConnectorObject result = connector.getObject(ObjectClass.ACCOUNT,
                new Uid(uid, new Name(login)),
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals("/2.0/users/" + uid, request.get().getUrl().getPath());
        assertEquals(ObjectClass.ACCOUNT, result.getObjectClass());
        assertEquals(uid, result.getUid().getUidValue());
        assertEquals(login, result.getName().getNameValue());
        assertNotNull(result.getAttributeByName("role"));
        assertEquals("admin", result.getAttributeByName("role").getValue().get(0));
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
        connector.search(ObjectClass.ACCOUNT,
                null,
                handler,
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(1, users.size());
        assertEquals(ObjectClass.ACCOUNT, users.get(0).getObjectClass());
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
        connector.search(ObjectClass.ACCOUNT,
                null,
                handler,
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(2, users.size());
        assertEquals(ObjectClass.ACCOUNT, users.get(0).getObjectClass());
        assertEquals("11446498", users.get(0).getUid().getUidValue());
        assertEquals("ceo@example.com", users.get(0).getName().getNameValue());
        assertEquals("Aaron Levie", users.get(0).getAttributeByName("name").getValue().get(0));
        assertEquals(ObjectClass.ACCOUNT, users.get(1).getObjectClass());
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
        connector.search(ObjectClass.ACCOUNT,
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
        connector.search(ObjectClass.ACCOUNT,
                new EqualsFilter(new Name(login)),
                handler,
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(String.format("filter_term=%s&limit=1000&offset=0", enc(login)), request.get().getUrl().getQuery());
        assertEquals(1, users.size());
        assertEquals(ObjectClass.ACCOUNT, users.get(0).getObjectClass());
        assertEquals("11446498", users.get(0).getUid().getUidValue());
        assertEquals(login, users.get(0).getName().getNameValue());
        assertEquals("Aaron Levie", users.get(0).getAttributeByName("name").getValue().get(0));
    }
}
