/*
 * Copyright (C) Exclamation Labs 2019. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */

package com.exclamationlabs.connid.box;

import com.box.sdk.*;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UsersHandler extends AbstractHandler {

    private static final Log LOGGER = Log.getLog(UsersHandler.class);

    // Use the type of the Box User resource:
    // https://developer.box.com/reference/resources/user/
    public static final ObjectClass OBJECT_CLASS_USER = new ObjectClass("user");

    private static final String ATTR_LOGIN = "login";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_ROLE = "role";
    private static final String ATTR_EXTERNAL_APP_USER_ID = "external_app_user_id";
    private static final String ATTR_LANGUAGE = "language";
    private static final String ATTR_IS_SYNC_ENABLED = "is_sync_enabled";
    private static final String ATTR_JOB_TITLE = "job_title";
    private static final String ATTR_PHONE = "phone";
    private static final String ATTR_ADDRESS = "address";
    private static final String ATTR_SPACE_AMOUNT = "space_amount";
    private static final String ATTR_CAN_SEE_MANAGED_USERS = "can_see_managed_users";
    private static final String ATTR_TIMEZONE = "timezone";
    private static final String ATTR_IS_EXEMPT_FROM_DEVICE_LIMITS = "is_exempt_from_device_limits";
    private static final String ATTR_IS_EXEMPT_FROM_LOGIN_VERIFICATION = "is_exempt_from_login_verification";
    private static final String ATTR_IS_EXEMPT_COLLAB_RESTRICTED = "is_external_collab_restricted";
    private static final String ATTR_STATUS = "status";
    private static final String ATTR_AVATAR_URL = "avatar_url";
    private static final String ATTR_ENTERPRISE = "enterprise";
    private static final String ATTR_NOTIFY = "notify";
    private static final String ATTR_CREATED_AT = "created_at";
    private static final String ATTR_MODIFIED_AT = "modified_at";
    private static final String ATTR_SPACE_USED = "space_used";
    private static final String ATTR_MAX_UPLOAD_SIZE = "max_upload_size";
    private static final String ATTR_IS_PASSWORD_RESET_REQUIRED = "is_password_reset_required";
    private static final String ATTR_TRACKING_CODES = "tracking_codes";
    private static final String ATTR_NOTIFICATION_EMAIL = "notification_email";
    private static final String ATTR_GROUP_MEMBERSHIP = "group_membership";

    private static final String[] DEFAULT_RETURN_ATTRIBUTES = new String[]{
            ATTR_NAME,
            ATTR_LOGIN,
            ATTR_CREATED_AT,
            ATTR_MODIFIED_AT,
            ATTR_LANGUAGE,
            ATTR_LANGUAGE,
            ATTR_TIMEZONE,
            ATTR_SPACE_AMOUNT,
            ATTR_SPACE_USED,
            ATTR_MAX_UPLOAD_SIZE,
            ATTR_STATUS,
            ATTR_JOB_TITLE,
            ATTR_PHONE,
            ATTR_ADDRESS,
            ATTR_AVATAR_URL,
            ATTR_NOTIFICATION_EMAIL
    };
    private static final Set<String> DEFAULT_RETURN_ATTRIBUTES_SET = createSet(DEFAULT_RETURN_ATTRIBUTES);

    private static Set<String> createSet(String[] array) {
        Set<String> attributesToGet = new HashSet<>();
        for (String a : array) {
            attributesToGet.add(a);
        }
        return attributesToGet;
    }

    private BoxAPIConnection boxAPI;

    public UsersHandler(BoxAPIConnection boxAPI) {
        this.boxAPI = boxAPI;
    }

    public ObjectClassInfo getUserSchema() {
        ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();
        builder.setType(OBJECT_CLASS_USER.getObjectClassValue());

        // id (__UID__)
        // Caution: Don't define a schema for "id" of user because the name conflicts with midPoint side.
//        builder.addAttributeInfo(
//                AttributeInfoBuilder.define(Uid.NAME)
//                        .setRequired(false) // Must be optional. It is not present for create operations
//                        .setCreateable(false)
//                        .setUpdateable(false)
//                        .setNativeName("id")
//                        .build()
//        );

        // login (__NAME__)
        builder.addAttributeInfo(AttributeInfoBuilder.define(Name.NAME)
                .setRequired(true)
                .setNativeName(ATTR_LOGIN)
                .setSubtype(AttributeInfo.Subtypes.STRING_CASE_IGNORE)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_LOGIN))
                .build());

        // name
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_NAME)
                .setRequired(true)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_NAME))
                .build());

        // role
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_ROLE)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_ROLE))
                .build());

        // external_app_user_id
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_EXTERNAL_APP_USER_ID)
                .setUpdateable(false)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_EXTERNAL_APP_USER_ID))
                .build());

        // language
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_LANGUAGE)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_LANGUAGE))
                .build());

        // is_sync_enabled
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_IS_SYNC_ENABLED)
                .setType(Boolean.class)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_IS_SYNC_ENABLED))
                .build());

        // job_titile
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_JOB_TITLE)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_JOB_TITLE))
                .build());

        // phone
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_PHONE)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_PHONE))
                .build());

        // address
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_ADDRESS)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_ADDRESS))
                .build());

        // space_amount
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_SPACE_AMOUNT)
                .setType(Long.class)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_SPACE_AMOUNT))
                .build());

        // max_upload_size
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_MAX_UPLOAD_SIZE)
                .setType(Long.class)
                .setCreateable(false)
                .setUpdateable(false)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_MAX_UPLOAD_SIZE))
                .build());

        // tracking_codes
        // e.g. "code1: 12345"
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_TRACKING_CODES)
                .setMultiValued(true)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_TRACKING_CODES))
                .build());

        // can_see_managed_users
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_CAN_SEE_MANAGED_USERS)
                .setType(Boolean.class)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_CAN_SEE_MANAGED_USERS))
                .build());

        // timezone
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_TIMEZONE)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_TIMEZONE))
                .build());

        // is_exempt_from_device_limits
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_IS_EXEMPT_FROM_DEVICE_LIMITS)
                .setType(Boolean.class)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_IS_EXEMPT_FROM_DEVICE_LIMITS))
                .build());

        // is_exempt_from_login_verification
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_IS_EXEMPT_FROM_LOGIN_VERIFICATION)
                .setType(Boolean.class)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_IS_EXEMPT_FROM_LOGIN_VERIFICATION))
                .build());

        // avatar
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_AVATAR_URL)
                .setSubtype(AttributeInfo.Subtypes.STRING_URI)
                .setCreateable(false)
                .setUpdateable(false)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_AVATAR_URL))
                .build());

        // is_external_collab_restricted
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_IS_EXEMPT_COLLAB_RESTRICTED)
                .setType(Boolean.class)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_IS_EXEMPT_COLLAB_RESTRICTED))
                .build());

        // enterprise
        // Only read/update:
        // https://developer.box.com/reference/put-users-id/#param-enterprise
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_ENTERPRISE)
                .setCreateable(false)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_ENTERPRISE))
                .build());

        // notify
        // Only update:
        // https://developer.box.com/reference/put-users-id/#param-notify
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_NOTIFY)
                .setType(Boolean.class)
                .setCreateable(false)
                .setReadable(false)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_NOTIFY))
                .build());

        // created_at
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_CREATED_AT)
                .setType(ZonedDateTime.class)
                .setCreateable(false)
                .setUpdateable(false)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_CREATED_AT))
                .build());

        // modified_at
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_MODIFIED_AT)
                .setType(ZonedDateTime.class)
                .setCreateable(false)
                .setUpdateable(false)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_MODIFIED_AT))
                .build());

        // space_used
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_SPACE_USED)
                .setType(Long.class)
                .setCreateable(false)
                .setUpdateable(false)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_SPACE_USED))
                .build());

        // is_password_reset_required
        // Only update:
        // https://developer.box.com/reference/put-users-id/#param-is_password_reset_required
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_IS_PASSWORD_RESET_REQUIRED)
                .setType(Boolean.class)
                .setCreateable(false)
                .setReadable(false)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_IS_PASSWORD_RESET_REQUIRED))
                .build());

        // Group membership
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_GROUP_MEMBERSHIP)
                .setMultiValued(true)
                .setReturnedByDefault(DEFAULT_RETURN_ATTRIBUTES_SET.contains(ATTR_GROUP_MEMBERSHIP))
                .build());

        // __ENABLE__
        builder.addAttributeInfo(OperationalAttributeInfos.ENABLE);

        ObjectClassInfo userSchemaInfo = builder.build();

        LOGGER.info("The constructed User core schema: {0}", userSchemaInfo);

        return userSchemaInfo;
    }

    public void query(BoxFilter query, ResultsHandler handler, OperationOptions ops) {
        LOGGER.info("UserHandler query VALUE: {0}", query);

        Set<String> attributesToGet = createAttributesToGetSet(ops);

        if (query == null) {
            getAllUsers(handler, ops, attributesToGet);
        } else {
            if (query.isByUid()) {
                getUser(query.uid, handler, ops, attributesToGet);
            } else {
                getUser(query.name, handler, ops, attributesToGet);
            }
        }
    }

    private void getAllUsers(ResultsHandler handler, OperationOptions ops, Set<String> attributesToGet) {
        Iterable<BoxUser.Info> users;
        if (attributesToGet.isEmpty()) {
            users = BoxUser.getAllEnterpriseUsers(boxAPI);
        } else {
            String[] merged = mergeAttributesToGet(ops, DEFAULT_RETURN_ATTRIBUTES);
            users = BoxUser.getAllEnterpriseUsers(boxAPI, null, merged);
        }

        for (BoxUser.Info info : users) {
            handler.handle(userToConnectorObject(info, attributesToGet));
        }
    }

    protected String[] mergeAttributesToGet(OperationOptions ops, String[] defaultReturnAttributes) {
        String[] attributesToGet = ops.getAttributesToGet();
        if (attributesToGet == null) {
            return defaultReturnAttributes;
        }

        String[] merged = new String[defaultReturnAttributes.length + attributesToGet.length];

        int i = 0;
        for (String attr : defaultReturnAttributes) {
            merged[i++] = attr;
        }
        for (String attr : attributesToGet) {
            merged[i++] = attr;
        }

        return merged;
    }

    private void getUser(Uid uid, ResultsHandler handler, OperationOptions ops, Set<String> attributesToGet) {
        BoxUser user = new BoxUser(boxAPI, uid.getUidValue());
        try {
            // Fetch an user
            BoxUser.Info info = user.getInfo();

            handler.handle(userToConnectorObject(info, attributesToGet));

        } catch (BoxAPIException e) {
            if (isNotFoundError(e)) {
                LOGGER.warn("Unknown uid: {0}", user.getID());
                throw new UnknownUidException(new Uid(user.getID()), OBJECT_CLASS_USER);
            }
            throw e;
        }
    }

    private void getUser(Name name, ResultsHandler handler, OperationOptions ops, Set<String> attributesToGet) {
        // "List enterprise users" supports find by "login" which is treated as __NAME__ in this connector.
        // https://developer.box.com/reference/get-users/
        Iterable<BoxUser.Info> users = BoxUser.getAllEnterpriseUsers(boxAPI, name.getNameValue());
        for (BoxUser.Info info : users) {
            if (info.getLogin().equalsIgnoreCase(name.getNameValue())) {
                handler.handle(userToConnectorObject(info, attributesToGet));
                // Break the loop to stop fetching remaining users if found
                return;
            }
        }
    }

    public Uid createUser(Set<Attribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            throw new InvalidAttributeValueException("attributes not provided or empty");
        }

        CreateUserParams createUserParams = new CreateUserParams();

        String login = null;
        String name = null;
        List<String> groupsToAdd = new ArrayList<>();

        for (Attribute attr : attributes) {
            if (attr.getName().equals(Name.NAME)) {
                login = getStringValue(attr);

            } else if (attr.getName().equals(ATTR_NAME)) {
                name = getStringValue(attr);

            } else if (attr.getName().equals(ATTR_ADDRESS)) {
                createUserParams.setAddress(getStringValue(attr));

            } else if (attr.getName().equals(ATTR_CAN_SEE_MANAGED_USERS)) {
                Boolean value = getBooleanValue(attr);
                if (value != null) {
                    createUserParams.setCanSeeManagedUsers(value.booleanValue());
                }
            } else if (attr.getName().equals(ATTR_EXTERNAL_APP_USER_ID)) {
                createUserParams.setExternalAppUserId(getStringValue(attr));

            } else if (attr.getName().equals(ATTR_IS_EXEMPT_FROM_DEVICE_LIMITS)) {
                Boolean value = getBooleanValue(attr);
                if (value != null) {
                    createUserParams.setIsExemptFromDeviceLimits(value.booleanValue());
                }
            } else if (attr.getName().equals(ATTR_IS_SYNC_ENABLED)) {
                Boolean value = getBooleanValue(attr);
                if (value != null) {
                    createUserParams.setIsSyncEnabled(value.booleanValue());
                }
            } else if (attr.getName().equals(ATTR_JOB_TITLE)) {
                createUserParams.setJobTitle(getStringValue(attr));

            } else if (attr.getName().equals(ATTR_LANGUAGE)) {
                createUserParams.setLanguage(getStringValue(attr));

            } else if (attr.getName().equals(ATTR_PHONE)) {
                createUserParams.setPhone(getStringValue(attr));

            } else if (attr.getName().equals(ATTR_SPACE_AMOUNT)) {
                Long value = getLongValue(attr);
                if (value != null) {
                    createUserParams.setSpaceAmount(value.longValue());
                }
            } else if (attr.getName().equals(OperationalAttributes.ENABLE_NAME)) {
                Boolean status = getBooleanValue(attr);
                if (Boolean.TRUE.equals(status)) {
                    createUserParams.setStatus(BoxUser.Status.ACTIVE);
                } else {
                    createUserParams.setStatus(BoxUser.Status.INACTIVE);
                }
            } else if (attr.getName().equals(ATTR_ROLE)) {
                // When creating a user, we can use "coadmin" or "user" only.
                // https://developer.box.com/reference/post-users/
                String role = getStringValue(attr);
                if (role != null) {
                    switch (role) {
                        case "coadmin":
                            createUserParams.setRole(BoxUser.Role.COADMIN);
                            break;
                        case "user":
                            createUserParams.setRole(BoxUser.Role.USER);
                            break;
                        default:
                            throw new InvalidAttributeValueException("Invalid role value of Box user: " + role);
                    }
                }
            } else if (attr.getName().equals(ATTR_GROUP_MEMBERSHIP)) {
                for (Object o : attr.getValue()) {
                    groupsToAdd.add(o.toString());
                }
            }
        }

        if (StringUtil.isBlank(login)) {
            throw new InvalidAttributeValueException(String.format("Missing mandatory attribute %s (%s)", Name.NAME, ATTR_LOGIN));
        }
        if (StringUtil.isBlank(name)) {
            throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_NAME);
        }

        try {
            BoxUser.Info createdUserInfo = BoxUser.createEnterpriseUser(boxAPI, login, name, createUserParams);

            if (!groupsToAdd.isEmpty()) {
                BoxUser user = createdUserInfo.getResource();
                for (String group : groupsToAdd) {
                    BoxGroup boxGroup = new BoxGroup(boxAPI, group);
                    boxGroup.addMembership(user);
                }
            }

            return new Uid(createdUserInfo.getID(), new Name(createdUserInfo.getLogin()));

        } catch (BoxAPIResponseException e) {
            if (isUserAlreadyExistsError(e)) {
                throw new AlreadyExistsException(e);
            }
            throw e;
        }
    }

    public Set<AttributeDelta> updateUser(Uid uid, Set<AttributeDelta> modifications) {
        BoxUser user = new BoxUser(boxAPI, uid.getUidValue());
        BoxUser.Info info = user.new Info();

        if (StringUtil.isEmpty(info.getID())) {
            throw new ConnectorIOException("Unable to confirm uid on box resource");
        }

        boolean renameLogin = false;
        List<String> groupsToAdd = null;
        List<String> groupsToRemove = null;

        for (AttributeDelta delta : modifications) {
            if (delta.getName().equals(ATTR_NAME)) {
                info.setName(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_ADDRESS)) {
                info.setAddress(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_CAN_SEE_MANAGED_USERS)) {
                info.setCanSeeManagedUsers(getBooleangValue(delta));

            } else if (delta.getName().equals(ATTR_IS_EXEMPT_FROM_DEVICE_LIMITS)) {
                info.setIsExemptFromDeviceLimits(getBooleangValue(delta));

            } else if (delta.getName().equals(ATTR_IS_SYNC_ENABLED)) {
                info.setIsSyncEnabled(getBooleangValue(delta));

            } else if (delta.getName().equals(ATTR_JOB_TITLE)) {
                info.setJobTitle(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_LANGUAGE)) {
                info.setLanguage(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_PHONE)) {
                info.setPhone(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_SPACE_AMOUNT)) {
                info.setSpaceAmount(getLongValue(delta));

            } else if (delta.getName().equals(OperationalAttributes.ENABLE_NAME)) {
                if (getBooleangValue(delta)) {
                    info.setStatus(BoxUser.Status.ACTIVE);
                } else {
                    info.setStatus(BoxUser.Status.INACTIVE);
                }

            } else if (delta.getName().equals(ATTR_ROLE)) {
                String role = getStringValue(delta);
                switch (role) {
                    case "admin":
                        info.setRole(BoxUser.Role.ADMIN);
                        break;
                    case "coadmin":
                        info.setRole(BoxUser.Role.COADMIN);
                        break;
                    case "user":
                        info.setRole(BoxUser.Role.USER);
                        break;
                    default:
                        //If it's wrong, just default to regular user account
                        info.setRole(BoxUser.Role.USER);
                }
            } else if (delta.getName().equals(Name.NAME)) {
                info.setLogin(getStringValue(delta));
                renameLogin = true;

            } else if (delta.getName().equals(ATTR_GROUP_MEMBERSHIP)) {
                groupsToAdd = getStringValuesToAdd(delta);
                groupsToRemove = getStringValuesToRemove(delta);
            }
        }

        // Handling email changing.
        // When updating email, we need to add email alias with confirmed flag first.
        // Then update the user with new email. If successful, the old email is moved to the alias.
        // Finally, we need to delete the alias.
        //
        // https://community.box.com/t5/Platform-and-Development-Forum/How-to-change-user-s-primary-login-via-API/td-p/26483

        EmailAlias newEmailAlias = null;
        String oldLogin = null;
        if (renameLogin) {
            // We need to get the current login (email) to rename it.
            // If the uid has NameHint, we can use the value as current login.
            // If not, we need to fetch the value from Box.
            if (uid.getNameHint() != null) {
                oldLogin = uid.getNameHint().getNameValue();
            } else {
                oldLogin = new BoxUser(boxAPI, uid.getUidValue()).getInfo("login").getLogin();
            }
            newEmailAlias = addEmailAlias(uid, info.getLogin());
        }

        try {
            info.getResource().updateInfo(info);
        } catch (BoxAPIException e) {
            LOGGER.error(e, "Failed to update an user. response: {0}", e.getResponse());

            // If updating email was failed, the new email alias will remain.
            // So we try to delete added new email alias for cleanup.
            if (newEmailAlias != null) {
                try {
                    user.deleteEmailAlias(newEmailAlias.getID());
                } catch (BoxAPIException e2) {
                    LOGGER.error(e2, "Failed to clean up added email alias {0} for {1}. response: {2}", newEmailAlias.getEmail(), oldLogin, e.getResponse());
                }
            }
            throw e;
        }

        // If updating email was successful, find the old email in the alias and delete it.
        if (renameLogin) {
            deleteEmailAlias(uid, oldLogin);
        }

        if (groupsToAdd != null || groupsToRemove != null) {
            updateMemberships(uid, groupsToAdd, groupsToRemove);
        }

        // Box doesn't support to modify user's id
        return null;
    }

    private void updateMemberships(Uid uid, List<String> groupsToAdd, List<String> groupsToRemove) {
        BoxUser user = new BoxUser(boxAPI, uid.getUidValue());

        if (!groupsToAdd.isEmpty()) {
            for (String group : groupsToAdd) {
                BoxGroup boxGroup = new BoxGroup(boxAPI, group);
                boxGroup.addMembership(user);
            }
        }
        if (!groupsToRemove.isEmpty()) {
            Iterable<BoxGroupMembership.Info> memberships = user.getAllMemberships();
            for (BoxGroupMembership.Info membershipInfo : memberships) {
                if (groupsToRemove.contains(membershipInfo.getGroup().getID())) {
                    membershipInfo.getResource().delete();
                }
            }
        }
    }

    private EmailAlias addEmailAlias(Uid uid, String email) {
        BoxUser user = new BoxUser(boxAPI, uid.getUidValue());
        EmailAlias newEmailAlias = null;
        try {
            return user.addEmailAlias(email, true);

        } catch (BoxAPIException e) {
            // Find email alias with new email because it might be added before.
            // In that case, we ignore the error.
            for (EmailAlias emailAlias : user.getEmailAliases()) {
                if (emailAlias.getEmail().equalsIgnoreCase(email)) {
                    newEmailAlias = emailAlias;
                    break;
                }
            }
            if (newEmailAlias == null) {
                LOGGER.error(e, "Failed to add email alias {0}. response: {1}", email, e.getResponse());
                throw e;
            }
        }
        return newEmailAlias;
    }

    private void deleteEmailAlias(Uid uid, String email) {
        BoxUser user = new BoxUser(boxAPI, uid.getUidValue());
        for (EmailAlias emailAlias : user.getEmailAliases()) {
            if (emailAlias.getEmail().equalsIgnoreCase(email)) {
                try {
                    user.deleteEmailAlias(emailAlias.getID());
                    break;
                } catch (BoxAPIException e) {
                    LOGGER.error(e, "Failed to delete old email: {0} response: {1}", email, e.getResponse());
                    throw e;
                }
            }
        }
    }

    public void deleteUser(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
        if (uid == null) {
            throw new InvalidAttributeValueException("uid not provided");
        }

        BoxUser user = new BoxUser(boxAPI, uid.getUidValue());
        user.delete(false, false);
    }

    private ConnectorObject userToConnectorObject(BoxUser.Info info, Set<String> attributesToGet) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();

        builder.setObjectClass(OBJECT_CLASS_USER);

        builder.setUid(new Uid(info.getID(), new Name(info.getLogin())));
        builder.setName(info.getLogin());

        // Default return
        builder.addAttribute(ATTR_NAME, info.getName());
        builder.addAttribute(ATTR_CREATED_AT, toZonedDateTime(info.getCreatedAt()));
        builder.addAttribute(ATTR_MODIFIED_AT, toZonedDateTime(info.getModifiedAt()));
        builder.addAttribute(ATTR_LANGUAGE, info.getLanguage());
        builder.addAttribute(ATTR_TIMEZONE, info.getTimezone());
        builder.addAttribute(ATTR_SPACE_AMOUNT, info.getSpaceAmount());
        builder.addAttribute(ATTR_SPACE_USED, info.getSpaceUsed());
        builder.addAttribute(ATTR_MAX_UPLOAD_SIZE, info.getMaxUploadSize());
        builder.addAttribute(ATTR_JOB_TITLE, info.getJobTitle());
        builder.addAttribute(ATTR_PHONE, info.getPhone());
        builder.addAttribute(ATTR_ADDRESS, info.getAddress());
        builder.addAttribute(ATTR_AVATAR_URL, info.getAvatarURL());

        // Status
        if (info.getStatus().equals(BoxUser.Status.ACTIVE)) {
            builder.addAttribute(OperationalAttributes.ENABLE_NAME, Boolean.TRUE);
        } else if (info.getStatus().equals(BoxUser.Status.INACTIVE)) {
            builder.addAttribute(OperationalAttributes.ENABLE_NAME, Boolean.FALSE);
        }

        // Optional return
        if (attributesToGet.contains(ATTR_ROLE)) {
            builder.addAttribute(ATTR_ROLE, toString(info.getRole()));
        }
        if (attributesToGet.contains(ATTR_IS_EXEMPT_FROM_DEVICE_LIMITS)) {
            builder.addAttribute(ATTR_IS_EXEMPT_FROM_DEVICE_LIMITS, info.getIsExemptFromDeviceLimits());
        }
        if (attributesToGet.contains(ATTR_IS_EXEMPT_FROM_LOGIN_VERIFICATION)) {
            builder.addAttribute(ATTR_IS_EXEMPT_FROM_LOGIN_VERIFICATION, info.getIsExemptFromLoginVerification());
        }
        if (attributesToGet.contains(ATTR_IS_EXEMPT_COLLAB_RESTRICTED)) {
            builder.addAttribute(ATTR_IS_EXEMPT_COLLAB_RESTRICTED, info.getIsExternalCollabRestricted());
        }
        if (attributesToGet.contains(ATTR_EXTERNAL_APP_USER_ID)) {
            builder.addAttribute(ATTR_EXTERNAL_APP_USER_ID, info.getExternalAppUserId());
        }
        // Fetch groups
        if (attributesToGet.contains(ATTR_GROUP_MEMBERSHIP)) {
            Iterable<BoxGroupMembership.Info> memberships = info.getResource().getAllMemberships();
            List<String> groupMemberships = new ArrayList<>();
            for (BoxGroupMembership.Info membershipInfo : memberships) {
                LOGGER.info("Group INFO getID {0}", membershipInfo.getGroup().getID());
                groupMemberships.add(membershipInfo.getGroup().getID());
            }
            builder.addAttribute(ATTR_GROUP_MEMBERSHIP, groupMemberships);
        }

        ConnectorObject connectorObject = builder.build();
        return connectorObject;
    }

    private String toString(BoxUser.Role role) {
        switch (role) {
            case USER:
                return "user";
            case ADMIN:
                return "admin";
            case COADMIN:
                return "coadmin";
        }
        throw new InvalidAttributeValueException("Unknown role: " + role);
    }
}
