/*
 * Copyright (C) Exclamation Labs 2019. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */

package com.exclamationlabs.connid.box;

import com.box.sdk.BoxAPIRequest;
import com.exclamationlabs.connid.box.testutil.AbstractTests;
import org.identityconnectors.framework.common.exceptions.RetryableException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.exclamationlabs.connid.box.GroupsHandler.OBJECT_CLASS_GROUP;
import static com.exclamationlabs.connid.box.testutil.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hiroyuki Wada
 */
class GroupDeleteTests extends AbstractTests {

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
        mockAPI.setMaxRequestAttempts(1);

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
}
