/*
 * Copyright (C) Exclamation Labs 2019. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */

package com.exclamationlabs.connid.box;

import com.box.sdk.BoxAPIRequest;
import com.exclamationlabs.connid.box.testutil.AbstractTests;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static com.exclamationlabs.connid.box.UsersHandler.OBJECT_CLASS_USER;
import static com.exclamationlabs.connid.box.testutil.TestUtils.ok;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hiroyuki Wada
 */
class UserGetTests extends AbstractTests {

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
        ConnectorObject result = connector.getObject(OBJECT_CLASS_USER,
                new Uid(uid, new Name(login)),
                new OperationOptionsBuilder()
                        // Only standard
                        .setReturnDefaultAttributes(true)
                        .build());

        // Then
        assertNotNull(request.get());
        assertEquals("/2.0/users/" + uid, request.get().getUrl().getPath());
        assertEquals(OBJECT_CLASS_USER, result.getObjectClass());
        assertEquals(uid, result.getUid().getUidValue());
        assertEquals(login, result.getName().getNameValue());

        for (String attr : UsersHandler.STANDARD_ATTRS) {
            assertNotNull(result.getAttributeByName(attr), attr + " should not be null");
        }
        for (String attr : UsersHandler.FULL_ATTRS) {
            assertNull(result.getAttributeByName(attr) , attr + " should be null");
        }

        assertEquals("6509241374", result.getAttributeByName("phone").getValue().get(0));
    }

    @Test
    void getUser_fullAttributes() {
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
        ConnectorObject result = connector.getObject(OBJECT_CLASS_USER,
                new Uid(uid, new Name(login)),
                new OperationOptionsBuilder()
                        .setReturnDefaultAttributes(true)
                        .setAttributesToGet(
                                UsersHandler.FULL_ATTRS
                        )
                        .build());

        // Then
        assertNotNull(request.get());
        assertEquals("/2.0/users/" + uid, request.get().getUrl().getPath());
        assertEquals(OBJECT_CLASS_USER, result.getObjectClass());
        assertEquals(uid, result.getUid().getUidValue());
        assertEquals(login, result.getName().getNameValue());

        for (String attr : UsersHandler.STANDARD_ATTRS) {
            assertNotNull(result.getAttributeByName(attr), attr + " should not be null");
        }
        for (String attr : UsersHandler.FULL_ATTRS) {
            assertNotNull(result.getAttributeByName(attr) , attr + " should not be null");
        }
    }
}
