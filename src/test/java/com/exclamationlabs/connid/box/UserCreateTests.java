/*
 * Copyright (C) Exclamation Labs 2019. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */

package com.exclamationlabs.connid.box;

import com.box.sdk.BoxAPIRequest;
import com.exclamationlabs.connid.box.testutil.AbstractTests;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.exclamationlabs.connid.box.UsersHandler.OBJECT_CLASS_USER;
import static com.exclamationlabs.connid.box.testutil.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hiroyuki Wada
 */
class UserCreateTests extends AbstractTests {

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
        Uid uid = connector.create(OBJECT_CLASS_USER, attributes, new OperationOptionsBuilder().build());

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
            Uid uid = connector.create(OBJECT_CLASS_USER, attributes, new OperationOptionsBuilder().build());
        });

        // Then
        assertNotNull(e);
    }
}
