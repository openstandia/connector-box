/*
 * Copyright (C) Exclamation Labs 2019. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */

package com.exclamationlabs.connid.box;

import com.box.sdk.BoxAPIException;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.AttributeDeltaUtil;
import org.identityconnectors.framework.common.objects.AttributeUtil;

import java.util.List;
import java.util.stream.Collectors;

public class AbstractHandler {

    protected String getStringValue(Attribute attr) {
        return AttributeUtil.getStringValue(attr);
    }

    protected String getStringValue(AttributeDelta delta) {
        if (delta.getValuesToReplace().isEmpty()) {
            // To delete the attribute in Box side, we need to set "".
            return null;
        }
        return AttributeDeltaUtil.getStringValue(delta);
    }

    protected Boolean getBooleanValue(Attribute attr) {
        return AttributeUtil.getBooleanValue(attr);
    }

    protected Boolean getBooleangValue(AttributeDelta delta) {
        if (delta.getValuesToReplace().isEmpty()) {
            // To delete the attribute in Box side, we need to set false.
            return false;
        }
        return AttributeDeltaUtil.getBooleanValue(delta);
    }

    protected Long getLongValue(Attribute attr) {
        return AttributeUtil.getLongValue(attr);
    }

    protected Long getLongValue(AttributeDelta delta) {
        if (delta.getValuesToReplace().isEmpty()) {
            // To delete the attribute in Box side, we need to set 0.
            return Long.valueOf(0);
        }
        return AttributeDeltaUtil.getLongValue(delta);
    }

    protected List<String> getStringValuesToAdd(Attribute attr) {
        return attr.getValue().stream().map(v -> v.toString()).collect(Collectors.toList());
    }

    protected List<String> getStringValuesToAdd(AttributeDelta delta) {
        List<Object> valuesToAdd = delta.getValuesToAdd();
        if (valuesToAdd == null) {
            return null;
        }
        return valuesToAdd.stream().map(v -> v.toString()).collect(Collectors.toList());
    }

    protected List<String> getStringValuesToRemove(AttributeDelta delta) {
        List<Object> valuesToRemove = delta.getValuesToRemove();
        if (valuesToRemove == null) {
            return null;
        }
        return valuesToRemove.stream().map(v -> v.toString()).collect(Collectors.toList());
    }

    protected boolean isUserAlreadyExistsError(BoxAPIException e) {
        if (e.getResponseCode() != 409) {
            return false;
        }
        String code = getErrorCode(e);
        return code.equals("user_login_already_used");
    }

    protected boolean isGroupAlreadyExistsError(BoxAPIException e) {
        if (e.getResponseCode() != 409) {
            return false;
        }
        String code = getErrorCode(e);
        return code.equals("conflict");
    }

    protected boolean isNotFoundError(BoxAPIException e) {
        if (e.getResponseCode() != 404) {
            return false;
        }
        String code = getErrorCode(e);
        return code.equals("not_found");
    }

    protected String getErrorCode(BoxAPIException e) {
        JsonObject response = JsonObject.readFrom(e.getResponse());
        JsonValue code = response.get("code");
        return code.asString();
    }
}
