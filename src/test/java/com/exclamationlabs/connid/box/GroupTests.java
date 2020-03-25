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
import org.identityconnectors.framework.common.exceptions.RetryableException;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.exclamationlabs.connid.box.GroupsHandler.OBJECT_CLASS_GROUP;
import static com.exclamationlabs.connid.box.testutil.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hiroyuki Wada
 */
class GroupTests {

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
    void createGroup() {
        // Given
        String groupName = "Support";
        String description = "Support Group - as imported from Active Directory";

        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new Name(groupName));
        attributes.add(AttributeBuilder.build("description", description));

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return created("group-create.json");
        });

        // When
        Uid uid = connector.create(OBJECT_CLASS_GROUP, attributes, new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(groupName, getJsonAttr(request.get(), "name"));
        assertEquals(description, getJsonAttr(request.get(), "description"));
        assertEquals("11446498", uid.getUidValue());
        assertEquals(groupName, uid.getNameHintValue());
    }

    @Test
    void createGroup_alreadyExists() {
        // Given
        String groupName = "Support";
        String description = "Support Group - as imported from Active Directory";

        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new Name(groupName));
        attributes.add(AttributeBuilder.build("description", description));

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            throw conflict();
        });

        // When
        AlreadyExistsException e = assertThrows(AlreadyExistsException.class, () -> {
            Uid uid = connector.create(OBJECT_CLASS_GROUP, attributes, new OperationOptionsBuilder().build());
        });

        // Then
        assertNotNull(e);
    }

    @Test
    void createGroup_otherError() {
        // Given
        String groupName = "Support";
        String description = "Support Group - as imported from Active Directory";

        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new Name(groupName));
        attributes.add(AttributeBuilder.build("description", description));

        // Set retry count of the Box SDK
        mockAPI.setMaxRequestAttempts(2);

        AtomicInteger count = new AtomicInteger();
        mockAPI.push(req -> {
            count.incrementAndGet();

            throw internalServerError();
        });
        mockAPI.push(req -> {
            count.incrementAndGet();

            throw internalServerError();
        });

        // When
        RetryableException e = assertThrows(RetryableException.class, () -> {
            Uid uid = connector.create(OBJECT_CLASS_GROUP, attributes, new OperationOptionsBuilder().build());
        });

        // Then
        assertNotNull(e);
        assertEquals(2, count.get());
    }

    @Test
    void updateGroup() {
        // Given
        String groupName = "Support";

        Set<AttributeDelta> modifications = new HashSet<>();
        modifications.add(AttributeDeltaBuilder.build("description", "Support Group"));

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return ok("group-update.json");
        });

        // When
        Set<AttributeDelta> sideEffects = connector.updateDelta(OBJECT_CLASS_GROUP,
                new Uid("11446498", new Name(groupName)),
                modifications, new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals("Support Group", getJsonAttr(request.get(), "description"));
        assertNull(sideEffects);
    }

    @Test
    void updateGroup_notFound() {
        // Given
        String groupName = "Support";

        Set<AttributeDelta> modifications = new HashSet<>();
        modifications.add(AttributeDeltaBuilder.build("description", "Support Group"));

        mockAPI.push(req -> {
            throw notFound();
        });

        // When
        UnknownUidException e = assertThrows(UnknownUidException.class, () -> {
            Set<AttributeDelta> sideEffects = connector.updateDelta(OBJECT_CLASS_GROUP,
                    new Uid("11446498", new Name(groupName)),
                    modifications, new OperationOptionsBuilder().build());
        });

        // Then
        assertNotNull(e);
    }

    @Test
    void updateGroup_otherError() {
        // Given
        String groupName = "Support";

        Set<AttributeDelta> modifications = new HashSet<>();
        modifications.add(AttributeDeltaBuilder.build("description", "Support Group"));

        // Set retry count of the Box SDK
        mockAPI.setMaxRequestAttempts(2);

        AtomicInteger count = new AtomicInteger();
        mockAPI.push(req -> {
            count.incrementAndGet();

            throw internalServerError();
        });
        mockAPI.push(req -> {
            count.incrementAndGet();

            throw internalServerError();
        });

        // When
        RetryableException e = assertThrows(RetryableException.class, () -> {
            Set<AttributeDelta> sideEffects = connector.updateDelta(OBJECT_CLASS_GROUP,
                    new Uid("11446498", new Name(groupName)),
                    modifications, new OperationOptionsBuilder().build());
        });

        // Then
        assertNotNull(e);
        assertEquals(2, count.get());
    }

    @Test
    void deleteGroup() {
        // Given
        String uid = "11446498";
        String groupName = "Support";

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return noContent();
        });

        // When
        connector.delete(OBJECT_CLASS_GROUP,
                new Uid(uid, new Name(groupName)),
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals("DELETE", request.get().getMethod());
        assertEquals("/2.0/groups/" + uid, request.get().getUrl().getPath());
        assertNull(request.get().getBody());
    }

    @Test
    void deleteGroup_notFound() {
        // Given
        String uid = "11446498";
        String groupName = "Support";

        mockAPI.push(req -> {
            throw notFound();
        });

        // When
        UnknownUidException e = assertThrows(UnknownUidException.class, () -> {
            connector.delete(OBJECT_CLASS_GROUP,
                    new Uid(uid, new Name(groupName)),
                    new OperationOptionsBuilder().build());
        });

        // Then
        assertNotNull(e);
    }

    @Test
    void deleteGroup_otherError() {
        // Given
        String uid = "11446498";
        String groupName = "Support";

        // Set retry count of the Box SDK
        mockAPI.setMaxRequestAttempts(2);

        AtomicInteger count = new AtomicInteger();
        mockAPI.push(req -> {
            count.incrementAndGet();

            throw internalServerError();
        });
        mockAPI.push(req -> {
            count.incrementAndGet();

            throw internalServerError();
        });

        // When
        RetryableException e = assertThrows(RetryableException.class, () -> {
            connector.delete(OBJECT_CLASS_GROUP,
                    new Uid(uid, new Name(groupName)),
                    new OperationOptionsBuilder().build());
        });

        // Then
        assertNotNull(e);
        assertEquals(2, count.get());
    }

    @Test
    void getGroup() {
        // Given
        String uid = "11446498";
        String groupName = "Support";

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return ok("group-get.json");
        });
        mockAPI.push(req -> {
            return ok("group-member-0.json");
        });

        // When
        ConnectorObject result = connector.getObject(OBJECT_CLASS_GROUP,
                new Uid(uid, new Name(groupName)),
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals("/2.0/groups/" + uid, request.get().getUrl().getPath());
        assertEquals(OBJECT_CLASS_GROUP, result.getObjectClass());
        assertEquals(uid, result.getUid().getUidValue());
        assertEquals(groupName, result.getName().getNameValue());
        assertNotNull(result.getAttributeByName("description"));
        assertEquals("Support Group - as imported from Active Directory", result.getAttributeByName("description").getValue().get(0));
    }

    @Test
    void searchAllGroup_1() {
        // Given
        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return ok("group-list-1.json");
        });
        mockAPI.push(req -> {
            return ok("group-member-0.json");
        });

        List<ConnectorObject> groups = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            groups.add(connectorObject);
            return true;
        };

        // When
        connector.search(OBJECT_CLASS_GROUP,
                null,
                handler,
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(1, groups.size());
        assertEquals(OBJECT_CLASS_GROUP, groups.get(0).getObjectClass());
        assertEquals("11446498", groups.get(0).getUid().getUidValue());
        assertEquals("Support", groups.get(0).getName().getNameValue());
        assertEquals("Support Group - as imported from Active Directory", groups.get(0).getAttributeByName("description").getValue().get(0));
    }

    @Test
    void searchAllGroup_2() {
        // Given
        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return ok("group-list-2.json");
        });
        mockAPI.push(req -> {
            return ok("group-member-0.json");
        });
        mockAPI.push(req -> {
            return ok("group-member-0.json");
        });

        List<ConnectorObject> groups = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            groups.add(connectorObject);
            return true;
        };

        // When
        connector.search(OBJECT_CLASS_GROUP,
                null,
                handler,
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(2, groups.size());
        assertEquals(OBJECT_CLASS_GROUP, groups.get(0).getObjectClass());
        assertEquals("11446498", groups.get(0).getUid().getUidValue());
        assertEquals("Support", groups.get(0).getName().getNameValue());
        assertEquals("Support Group - as imported from Active Directory", groups.get(0).getAttributeByName("description").getValue().get(0));
        assertEquals(OBJECT_CLASS_GROUP, groups.get(1).getObjectClass());
        assertEquals("12345678", groups.get(1).getUid().getUidValue());
        assertEquals("Foo", groups.get(1).getName().getNameValue());
        assertEquals("Foo Group", groups.get(1).getAttributeByName("description").getValue().get(0));
    }

    @Test
    void searchAllGroup_empty() {
        // Given
        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return ok("group-list-0.json");
        });

        List<ConnectorObject> groups = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            groups.add(connectorObject);
            return true;
        };

        // When
        connector.search(OBJECT_CLASS_GROUP,
                null,
                handler,
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(0, groups.size());
    }

    @Test
    void searchGroupByName() throws UnsupportedEncodingException {
        // Given
        String uid = "11446498";
        String groupName = "Support";

        AtomicReference<BoxAPIRequest> request = new AtomicReference<>();
        mockAPI.push(req -> {
            request.set(req);

            return ok("group-list-1.json");
        });
        mockAPI.push(req -> {
            return ok("group-member-0.json");
        });

        List<ConnectorObject> groups = new ArrayList<>();
        ResultsHandler handler = connectorObject -> {
            groups.add(connectorObject);
            return true;
        };

        // When
        connector.search(OBJECT_CLASS_GROUP,
                new EqualsFilter(new Name(groupName)),
                handler,
                new OperationOptionsBuilder().build());

        // Then
        assertNotNull(request.get());
        assertEquals(String.format("name=%s&limit=1000&offset=0", enc(groupName)), request.get().getUrl().getQuery());
        assertEquals(1, groups.size());
        assertEquals(OBJECT_CLASS_GROUP, groups.get(0).getObjectClass());
        assertEquals("11446498", groups.get(0).getUid().getUidValue());
        assertEquals(groupName, groups.get(0).getName().getNameValue());
    }
}
