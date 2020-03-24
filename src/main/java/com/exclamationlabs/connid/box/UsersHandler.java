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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UsersHandler extends AbstractHandler {

    private static final Log LOGGER = Log.getLog(UsersHandler.class);

    private static final String ATTR_LOGIN = "login";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_ROLE = "role";
    private static final String ATTR_EXTERNAL_APP_USER_ID = "external_app_user_id";
    private static final String ATTR_LANGUAGE = "language";
    private static final String ATTR_SYNC = "is_sync_enabled";
    private static final String ATTR_TITLE = "job_title";
    private static final String ATTR_PHONE = "phone";
    private static final String ATTR_ADDRESS = "address";
    private static final String ATTR_SPACE = "space_amount";
    private static final String ATTR_MANAGED = "can_see_managed_users";
    private static final String ATTR_TIMEZONE = "timezone";
    private static final String ATTR_DEVICELIMITS = "is_exempt_from_device_limits";
    private static final String ATTR_LOGINVERIFICATION = "is_exempt_from_login_verification";
    private static final String ATTR_COLLAB = "is_external_collab_restricted";
    private static final String ATTR_STATUS = "status";
    private static final String ATTR_AVATAR = "avatar_url";
    private static final String ATTR_ENTERPRISE = "enterprise";
    private static final String ATTR_NOTIFY = "notify";
    private static final String ATTR_CREATED = "created_at";
    private static final String ATTR_MODIFIED = "modified_at";
    private static final String ATTR_USED = "space_used";
    private static final String ATTR_PSSWD = "is_password_reset_required";
    private static final String ATTR_CODE = "tracking_codes";
    private static final String ATTR_MEMBERSHIPS = "group_membership";

    private BoxDeveloperEditionAPIConnection boxDeveloperEditionAPIConnection;

    public UsersHandler(BoxDeveloperEditionAPIConnection boxDeveloperEditionAPIConnection) {
        this.boxDeveloperEditionAPIConnection = boxDeveloperEditionAPIConnection;
    }

    public ObjectClassInfo getUserSchema() {
        ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();

        // name
        AttributeInfoBuilder attrNameBuilder = new AttributeInfoBuilder(ATTR_NAME);
        attrNameBuilder.setRequired(true);
        attrNameBuilder.setUpdateable(true);
        ocBuilder.addAttributeInfo(attrNameBuilder.build());
        // mail
        AttributeInfoBuilder attrLoginBuilder = new AttributeInfoBuilder(Name.NAME);
        attrLoginBuilder.setRequired(true);
        attrLoginBuilder.setUpdateable(true);
        attrLoginBuilder.setNativeName(ATTR_LOGIN);
        attrLoginBuilder.setSubtype(AttributeInfo.Subtypes.STRING_CASE_IGNORE);
        ocBuilder.addAttributeInfo(attrLoginBuilder.build());
        // role
        AttributeInfoBuilder attrRoleBuilder = new AttributeInfoBuilder(ATTR_ROLE);
        attrRoleBuilder.setUpdateable(true);
        attrRoleBuilder.setReturnedByDefault(false);
        ocBuilder.addAttributeInfo(attrRoleBuilder.build());
        // external_app_user_id
        AttributeInfoBuilder attrExternalAppUserId = new AttributeInfoBuilder(ATTR_EXTERNAL_APP_USER_ID);
        attrExternalAppUserId.setUpdateable(false);
        attrExternalAppUserId.setReturnedByDefault(false);
        ocBuilder.addAttributeInfo(attrExternalAppUserId.build());
        // language
        AttributeInfoBuilder attrLanguageBuilder = new AttributeInfoBuilder(ATTR_LANGUAGE);
        attrLanguageBuilder.setUpdateable(true);
        ocBuilder.addAttributeInfo(attrLanguageBuilder.build());
        // is_sync_enabled
        AttributeInfoBuilder attrIsSyncEnabledBuilder = new AttributeInfoBuilder(ATTR_SYNC, Boolean.class);
        attrIsSyncEnabledBuilder.setUpdateable(true);
        attrIsSyncEnabledBuilder.setReturnedByDefault(false);
        ocBuilder.addAttributeInfo(attrIsSyncEnabledBuilder.build());
        // job_titile
        AttributeInfoBuilder attrJobTitleBuilder = new AttributeInfoBuilder(ATTR_TITLE);
        attrJobTitleBuilder.setUpdateable(true);
        ocBuilder.addAttributeInfo(attrJobTitleBuilder.build());
        // phone
        AttributeInfoBuilder attrPhoneBuilder = new AttributeInfoBuilder(ATTR_PHONE);
        attrPhoneBuilder.setUpdateable(true);
        ocBuilder.addAttributeInfo(attrPhoneBuilder.build());
        // address
        AttributeInfoBuilder attrAddressBuilder = new AttributeInfoBuilder(ATTR_ADDRESS);
        attrAddressBuilder.setUpdateable(true);
        ocBuilder.addAttributeInfo(attrAddressBuilder.build());
        // space_amount
        AttributeInfoBuilder attrSpaceAmountBuilder = new AttributeInfoBuilder(ATTR_SPACE, Long.class);
        attrSpaceAmountBuilder.setUpdateable(true);
        ocBuilder.addAttributeInfo(attrSpaceAmountBuilder.build());
        // tracking_codes
        AttributeInfoBuilder attrTrackingCodeBuilder = new AttributeInfoBuilder(ATTR_CODE);
        attrTrackingCodeBuilder.setMultiValued(true);
        attrTrackingCodeBuilder.setUpdateable(true);
        attrTrackingCodeBuilder.setReturnedByDefault(false);
        ocBuilder.addAttributeInfo(attrTrackingCodeBuilder.build());
        // can_see_managed_users
        AttributeInfoBuilder attrCanSeeManagedUsersBuilder = new AttributeInfoBuilder(ATTR_MANAGED, Boolean.class);
        attrCanSeeManagedUsersBuilder.setUpdateable(true);
        attrCanSeeManagedUsersBuilder.setReturnedByDefault(false);
        ocBuilder.addAttributeInfo(attrCanSeeManagedUsersBuilder.build());
        // timezone
        AttributeInfoBuilder attrTimezoneBuilder = new AttributeInfoBuilder(ATTR_TIMEZONE);
        attrTimezoneBuilder.setUpdateable(true);
        ocBuilder.addAttributeInfo(attrTimezoneBuilder.build());
        // is_exempt_from_device_limits
        AttributeInfoBuilder attrIsExemptFromDeviceLimits = new AttributeInfoBuilder(ATTR_DEVICELIMITS, Boolean.class);
        attrIsExemptFromDeviceLimits.setUpdateable(true);
        attrIsExemptFromDeviceLimits.setReturnedByDefault(false);
        ocBuilder.addAttributeInfo(attrIsExemptFromDeviceLimits.build());
        // is_exempt_from_login_verification
        AttributeInfoBuilder attrIsExemptFromLoginVerification = new AttributeInfoBuilder(ATTR_LOGINVERIFICATION,
                Boolean.class);
        attrIsExemptFromLoginVerification.setUpdateable(true);
        attrIsExemptFromLoginVerification.setReturnedByDefault(false);
        ocBuilder.addAttributeInfo(attrIsExemptFromLoginVerification.build());
        // avatar
        AttributeInfoBuilder attrAvatar = new AttributeInfoBuilder(ATTR_AVATAR, String.class);
        attrAvatar.setUpdateable(false);
        ocBuilder.addAttributeInfo(attrAvatar.build());
        // is_external_collab_restricted
        AttributeInfoBuilder attrCollab = new AttributeInfoBuilder(ATTR_COLLAB, Boolean.class);
        attrCollab.setUpdateable(true);
        attrCollab.setReturnedByDefault(false);
        ocBuilder.addAttributeInfo(attrCollab.build());
        // enterprise
        AttributeInfoBuilder attrEnterpise = new AttributeInfoBuilder(ATTR_ENTERPRISE);
        attrEnterpise.setUpdateable(true);
        attrEnterpise.setReturnedByDefault(false);
        ocBuilder.addAttributeInfo(attrEnterpise.build());
        // notify
        AttributeInfoBuilder attrNotify = new AttributeInfoBuilder(ATTR_NOTIFY, Boolean.class);
        attrNotify.setUpdateable(true);
        attrNotify.setReturnedByDefault(false);
        ocBuilder.addAttributeInfo(attrNotify.build());

        AttributeInfoBuilder attrCreated = new AttributeInfoBuilder(ATTR_CREATED);
        attrCreated.setUpdateable(false);
        attrCreated.setCreateable(false);
        ocBuilder.addAttributeInfo(attrCreated.build());

        AttributeInfoBuilder attrModified = new AttributeInfoBuilder(ATTR_MODIFIED);
        attrModified.setUpdateable(false);
        attrModified.setCreateable(false);
        ocBuilder.addAttributeInfo(attrModified.build());

        AttributeInfoBuilder attrUsed = new AttributeInfoBuilder(ATTR_USED, Long.class);
        attrUsed.setUpdateable(false);
        ocBuilder.addAttributeInfo(attrUsed.build());

        AttributeInfoBuilder attrPsswd = new AttributeInfoBuilder(ATTR_PSSWD, Boolean.class);
        attrPsswd.setCreateable(false);
        attrPsswd.setReturnedByDefault(false);
        ocBuilder.addAttributeInfo(attrPsswd.build());

        AttributeInfoBuilder attrMembership = new AttributeInfoBuilder(ATTR_MEMBERSHIPS);
        attrMembership.setMultiValued(true);
        attrMembership.setUpdateable(true);
        ocBuilder.addAttributeInfo(attrMembership.build());

        // __ENABLE__
        ocBuilder.addAttributeInfo(OperationalAttributeInfos.ENABLE);

        ObjectClassInfo userSchemaInfo = ocBuilder.build();
        LOGGER.info("The constructed User core schema: {0}", userSchemaInfo);
        return userSchemaInfo;
    }

    public void query(BoxFilter query, ResultsHandler handler, OperationOptions ops) {
        LOGGER.info("UserHandler query VALUE: {0}", query);

        if (query == null) {
            getAllUsers(handler, ops);
        } else {
            if (query.isByUid()) {
                getUser(query.uid, handler, ops);
            } else {
                getUser(query.name, handler, ops);
            }
        }
    }

    private void getAllUsers(ResultsHandler handler, OperationOptions ops) {
        Iterable<BoxUser.Info> users = BoxUser.getAllEnterpriseUsers(boxDeveloperEditionAPIConnection);
        for (BoxUser.Info info : users) {
            handler.handle(userToConnectorObject(info));
        }
    }

    private void getUser(Uid uid, ResultsHandler handler, OperationOptions ops) {
        BoxUser user = new BoxUser(boxDeveloperEditionAPIConnection, uid.getUidValue());
        try {
            // Fetch an user
            BoxUser.Info info = user.getInfo();

            handler.handle(userToConnectorObject(info));

        } catch (BoxAPIException e) {
            if (isNotFoundError(e)) {
                LOGGER.warn("Unknown uid: {0}", user.getID());
                throw new UnknownUidException(new Uid(user.getID()), ObjectClass.ACCOUNT);
            }
            throw e;
        }
    }

    private void getUser(Name name, ResultsHandler handler, OperationOptions ops) {
        // "List enterprise users" supports find by "login" which is treated as __NAME__ in this connector.
        // https://developer.box.com/reference/get-users/
        Iterable<BoxUser.Info> users = BoxUser.getAllEnterpriseUsers(boxDeveloperEditionAPIConnection, name.getNameValue());
        for (BoxUser.Info info : users) {
            if (info.getLogin().equalsIgnoreCase(name.getNameValue())) {
                handler.handle(userToConnectorObject(info));
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

            } else if (attr.getName().equals(ATTR_MANAGED)) {
                Boolean value = getBooleanValue(attr);
                if (value != null) {
                    createUserParams.setCanSeeManagedUsers(value.booleanValue());
                }
            } else if (attr.getName().equals(ATTR_EXTERNAL_APP_USER_ID)) {
                createUserParams.setExternalAppUserId(getStringValue(attr));

            } else if (attr.getName().equals(ATTR_DEVICELIMITS)) {
                Boolean value = getBooleanValue(attr);
                if (value != null) {
                    createUserParams.setIsExemptFromDeviceLimits(value.booleanValue());
                }
            } else if (attr.getName().equals(ATTR_SYNC)) {
                Boolean value = getBooleanValue(attr);
                if (value != null) {
                    createUserParams.setIsSyncEnabled(value.booleanValue());
                }
            } else if (attr.getName().equals(ATTR_TITLE)) {
                createUserParams.setJobTitle(getStringValue(attr));

            } else if (attr.getName().equals(ATTR_LANGUAGE)) {
                createUserParams.setLanguage(getStringValue(attr));

            } else if (attr.getName().equals(ATTR_PHONE)) {
                createUserParams.setPhone(getStringValue(attr));

            } else if (attr.getName().equals(ATTR_SPACE)) {
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
            } else if (attr.getName().equals(ATTR_MEMBERSHIPS)) {
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
            BoxUser.Info createdUserInfo = BoxUser.createEnterpriseUser(boxDeveloperEditionAPIConnection, login, name, createUserParams);

            if (!groupsToAdd.isEmpty()) {
                BoxUser user = createdUserInfo.getResource();
                for (String group : groupsToAdd) {
                    BoxGroup boxGroup = new BoxGroup(boxDeveloperEditionAPIConnection, group);
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
        BoxUser user = new BoxUser(boxDeveloperEditionAPIConnection, uid.getUidValue());
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

            } else if (delta.getName().equals(ATTR_MANAGED)) {
                info.setCanSeeManagedUsers(getBooleangValue(delta));

            } else if (delta.getName().equals(ATTR_DEVICELIMITS)) {
                info.setIsExemptFromDeviceLimits(getBooleangValue(delta));

            } else if (delta.getName().equals(ATTR_SYNC)) {
                info.setIsSyncEnabled(getBooleangValue(delta));

            } else if (delta.getName().equals(ATTR_TITLE)) {
                info.setJobTitle(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_LANGUAGE)) {
                info.setLanguage(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_PHONE)) {
                info.setPhone(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_SPACE)) {
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

            } else if (delta.getName().equals(ATTR_MEMBERSHIPS)) {
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
                oldLogin = new BoxUser(boxDeveloperEditionAPIConnection, uid.getUidValue()).getInfo("login").getLogin();
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
        BoxUser user = new BoxUser(boxDeveloperEditionAPIConnection, uid.getUidValue());

        if (!groupsToAdd.isEmpty()) {
            for (String group : groupsToAdd) {
                BoxGroup boxGroup = new BoxGroup(boxDeveloperEditionAPIConnection, group);
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
        BoxUser user = new BoxUser(boxDeveloperEditionAPIConnection, uid.getUidValue());
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
        BoxUser user = new BoxUser(boxDeveloperEditionAPIConnection, uid.getUidValue());
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

        BoxUser user = new BoxUser(boxDeveloperEditionAPIConnection, uid.getUidValue());
        user.delete(false, false);
    }

    private ConnectorObject userToConnectorObject(BoxUser.Info info) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();

        builder.setUid(new Uid(info.getID(), new Name(info.getLogin())));
        builder.setName(info.getLogin());
        builder.addAttribute(ATTR_NAME, info.getName());
        builder.addAttribute(ATTR_ADDRESS, info.getAddress());
        builder.addAttribute(ATTR_DEVICELIMITS, info.getIsExemptFromDeviceLimits());
        builder.addAttribute(ATTR_LANGUAGE, info.getLanguage());
        builder.addAttribute(ATTR_PHONE, info.getPhone());
        builder.addAttribute(ATTR_ROLE, info.getRole());
        builder.addAttribute(ATTR_SPACE, info.getSpaceAmount());
        builder.addAttribute(ATTR_TIMEZONE, info.getTimezone());
        builder.addAttribute(ATTR_TITLE, info.getJobTitle());
        builder.addAttribute(ATTR_AVATAR, info.getAvatarURL());
        builder.addAttribute(ATTR_CREATED, info.getCreatedAt().getTime());
        builder.addAttribute(ATTR_MODIFIED, info.getModifiedAt().getTime());
        builder.addAttribute(ATTR_USED, info.getSpaceUsed());
        builder.addAttribute(ATTR_EXTERNAL_APP_USER_ID, info.getExternalAppUserId());

        if (info.getStatus().equals(BoxUser.Status.ACTIVE)) {
            builder.addAttribute(OperationalAttributes.ENABLE_NAME, Boolean.TRUE);
        } else if (info.getStatus().equals(BoxUser.Status.INACTIVE)) {
            builder.addAttribute(OperationalAttributes.ENABLE_NAME, Boolean.FALSE);
        }

        // Fetch groups
        Iterable<BoxGroupMembership.Info> memberships = info.getResource().getAllMemberships();
        List<String> groupMemberships = new ArrayList<>();
        for (BoxGroupMembership.Info membershipInfo : memberships) {
            LOGGER.info("Group INFO getID {0}", membershipInfo.getGroup().getID());
            groupMemberships.add(membershipInfo.getGroup().getID());
        }
        builder.addAttribute(ATTR_MEMBERSHIPS, groupMemberships);

        ConnectorObject connectorObject = builder.build();
        return connectorObject;
    }
}
