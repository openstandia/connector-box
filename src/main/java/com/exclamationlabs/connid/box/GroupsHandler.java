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
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupsHandler extends AbstractHandler {

    private static final Log LOGGER = Log.getLog(GroupsHandler.class);

    // Use the type of the Box Group resource:
    // https://developer.box.com/reference/resources/group/
    public static final ObjectClass OBJECT_CLASS_GROUP = new ObjectClass("group");

    // Mini
    protected static final String ATTR_GROUP_TYPE = "group_type";
    protected static final String ATTR_NAME = "name";

    // Standard
    protected static final String ATTR_CREATED_AT = "created_at";
    protected static final String ATTR_MODIFIED_AT = "modified_at";

    // Full
    protected static final String ATTR_DESCRIPTION = "description";
    protected static final String ATTR_EXTERNAL_SYNC_IDENTIFIER = "external_sync_identifier";
    protected static final String ATTR_INVITABILITY_LEVEL = "invitability_level";
    protected static final String ATTR_MEMBER_VIEWABILITY_LEVEL = "member_viewability_level";
    protected static final String ATTR_PROVENANCE = "provenance";

    // Association
    // There are two roles of members: "member" and "admin"
    // https://developer.box.com/reference/resources/group-membership/#param-role
    protected static final String ATTR_MEMBER = "member";
    protected static final String ATTR_ADMIN = "admin";

    // Collaborations for group
    protected static final String ATTR_CO_OWNER = "co_owner";
    protected static final String ATTR_EDITOR = "editor";
    protected static final String ATTR_PREVIEWER = "previewer";
    protected static final String ATTR_PREVIEWER_UPLOADER = "previewer_uploader";
    protected static final String ATTR_UPLOADER = "uploader";
    protected static final String ATTR_VIEWER = "viewer";
    protected static final String ATTR_VIEWER_UPLOADER = "viewer_uploader";

    protected static final String[] MINI_ATTRS = new String[]{
            ATTR_GROUP_TYPE,
            ATTR_NAME
    };
    protected static final String[] STANDARD_ATTRS = new String[]{
            ATTR_CREATED_AT,
            ATTR_MODIFIED_AT,
    };
    protected static final String[] FULL_ATTRS = new String[]{
            ATTR_DESCRIPTION,
            ATTR_EXTERNAL_SYNC_IDENTIFIER,
            ATTR_INVITABILITY_LEVEL,
            ATTR_MEMBER_VIEWABILITY_LEVEL,
            ATTR_PROVENANCE
    };
    protected static final Set<String> STANDARD_ATTRS_SET =
            Collections.unmodifiableSet(Stream.of(
                    MINI_ATTRS,
                    STANDARD_ATTRS
            ).flatMap(Arrays::stream).collect(Collectors.toSet()));
    protected static final Set<String> FULL_ATTRS_SET =
            Collections.unmodifiableSet(Stream.of(
                    MINI_ATTRS,
                    STANDARD_ATTRS,
                    FULL_ATTRS
            ).flatMap(Arrays::stream).collect(Collectors.toSet()));

    private BoxAPIConnection boxAPI;

    public GroupsHandler(BoxAPIConnection boxAPI) {
        this.boxAPI = boxAPI;
    }

    public ObjectClassInfo getGroupSchema() {
        ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();
        builder.setType(OBJECT_CLASS_GROUP.getObjectClassValue());

        // Base

        // id (__UID__)
        // Caution: Don't define a schema for "id" of group because the name conflicts with midPoint side.
//        builder.addAttributeInfo(
//                AttributeInfoBuilder.define(Uid.NAME)
//                        .setRequired(false) // Must be optional. It is not present for create operations
//                        .setCreateable(false)
//                        .setUpdateable(false)
//                        .setNativeName(ATTR_ID)
//                        .build()
//        );

        // type (read-only)
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_TYPE)
                .setCreateable(false)
                .setUpdateable(false)
                .setReturnedByDefault(STANDARD_ATTRS_SET.contains(ATTR_TYPE))
                .build());

        // Mini

        // group_type (read-only)
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_GROUP_TYPE)
                .setCreateable(false)
                .setUpdateable(false)
                .setReturnedByDefault(STANDARD_ATTRS_SET.contains(ATTR_GROUP_TYPE))
                .build());

        // name (__NAME__)
        builder.addAttributeInfo(AttributeInfoBuilder.define(Name.NAME)
                .setRequired(true)
                .setNativeName(ATTR_NAME)
                .setSubtype(AttributeInfo.Subtypes.STRING_CASE_IGNORE)
                .build());

        // Standard

        // created_at (read-only)
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_CREATED_AT)
                .setType(ZonedDateTime.class)
                .setCreateable(false)
                .setUpdateable(false)
                .setReturnedByDefault(STANDARD_ATTRS_SET.contains(ATTR_CREATED_AT))
                .build());

        // modified_at (read-only)
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_MODIFIED_AT)
                .setType(ZonedDateTime.class)
                .setCreateable(false)
                .setUpdateable(false)
                .setReturnedByDefault(STANDARD_ATTRS_SET.contains(ATTR_MODIFIED_AT))
                .build());

        // Full

        // description
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_DESCRIPTION)
                .build());

        // external_sync_identifier
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_EXTERNAL_SYNC_IDENTIFIER)
                .build());

        // invitability_level
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_INVITABILITY_LEVEL)
                .build());

        // member_viewability_level
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_MEMBER_VIEWABILITY_LEVEL)
                .build());

        // provenance
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_PROVENANCE)
                .build());

        // Association

        // Group member(member)
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_MEMBER)
                .setMultiValued(true)
                .build());

        // Group member(admin)
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_ADMIN)
                .setMultiValued(true)
                .build());

        // Collaborations for group
        // TODO: Although define schemas, they aren't implemented yet
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_CO_OWNER)
                .setMultiValued(true)
                .setReturnedByDefault(false)
                .build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_EDITOR)
                .setMultiValued(true)
                .setReturnedByDefault(false)
                .build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_PREVIEWER)
                .setMultiValued(true)
                .setReturnedByDefault(false)
                .build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_PREVIEWER_UPLOADER)
                .setMultiValued(true)
                .setReturnedByDefault(false)
                .build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_UPLOADER)
                .setMultiValued(true)
                .setReturnedByDefault(false)
                .build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_VIEWER)
                .setMultiValued(true)
                .setReturnedByDefault(false)
                .build());
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_VIEWER_UPLOADER)
                .setMultiValued(true)
                .setReturnedByDefault(false)
                .build());

        ObjectClassInfo groupSchemaInfo = builder.build();

        LOGGER.info("The constructed group schema representation: {0}", groupSchemaInfo);

        return groupSchemaInfo;

    }

    public Uid createGroup(Set<Attribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            throw new InvalidAttributeValueException("attributes not provided or empty");
        }

        String name = null;
        String description = null;
        String externalSyncIdentifier = null;
        String invitabilityLevel = null;
        String memberViewabilityLevel = null;
        String provenance = null;

        for (Attribute attr : attributes) {
            if (attr.getName().equals(Name.NAME)) {
                name = AttributeUtil.getStringValue(attr);
            } else if (attr.getName().equals(ATTR_DESCRIPTION)) {
                description = AttributeUtil.getStringValue(attr);

            } else if (attr.getName().equals(ATTR_EXTERNAL_SYNC_IDENTIFIER)) {
                externalSyncIdentifier = AttributeUtil.getStringValue(attr);

            } else if (attr.getName().equals(ATTR_INVITABILITY_LEVEL)) {
                invitabilityLevel = AttributeUtil.getStringValue(attr);

            } else if (attr.getName().equals(ATTR_MEMBER_VIEWABILITY_LEVEL)) {
                memberViewabilityLevel = AttributeUtil.getStringValue(attr);

            } else if (attr.getName().equals(ATTR_PROVENANCE)) {
                provenance = AttributeUtil.getStringValue(attr);
            }
        }

        if (StringUtil.isBlank(name)) {
            throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_NAME);
        }

        try {
            BoxGroup.Info groupInfo = BoxGroup.createGroup(
                    boxAPI,
                    name,
                    provenance,
                    externalSyncIdentifier,
                    description,
                    invitabilityLevel,
                    memberViewabilityLevel
            );
            return new Uid(groupInfo.getID(), new Name(name));

        } catch (BoxAPIException e) {
            if (isGroupAlreadyExistsError(e)) {
                throw new AlreadyExistsException(e);
            }
            throw e;
        }
    }

    public Set<AttributeDelta> updateGroup(Uid uid, Set<AttributeDelta> modifications) {
        if (modifications == null || modifications.isEmpty()) {
            throw new InvalidAttributeValueException("attributes not provided or empty");
        }

        BoxGroup group = new BoxGroup(boxAPI, uid.getUidValue());
        BoxGroup.Info info = group.new Info();

        for (AttributeDelta delta : modifications) {
            if (delta.getName().equals(ATTR_NAME)) {
                info.setName(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_PROVENANCE)) {
                info.setProvenance(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_EXTERNAL_SYNC_IDENTIFIER)) {
                info.setExternalSyncIdentifier(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_DESCRIPTION)) {
                info.setDescription(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_INVITABILITY_LEVEL)) {
                info.setInvitabilityLevel(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_MEMBER_VIEWABILITY_LEVEL)) {
                info.setMemberViewabilityLevel(getStringValue(delta));
            }
        }

        try {
            info.getResource().updateInfo(info);

            // Box doesn't support to modify group's id
            return null;

        } catch (BoxAPIException e) {
            if (isNotFoundError(e)) {
                throw newUnknownUidException(uid, OBJECT_CLASS_GROUP, e);
            }
            throw e;
        }
    }

    public void query(BoxFilter query, ResultsHandler handler, OperationOptions ops) {
        LOGGER.info("GroupsHandler query VALUE: {0}", query);

        Set<String> attributesToGet = createFullAttributesToGetSet(STANDARD_ATTRS_SET, ops);

        if (query == null) {
            getAllGroups(handler, ops, attributesToGet);
        } else {
            if (query.isByUid()) {
                getGroup(query.uid, handler, ops, attributesToGet);
            } else {
                getGroup(query.name, handler, ops, attributesToGet);
            }
        }
    }

    private void getAllGroups(ResultsHandler handler, OperationOptions ops, Set<String> attributesToGet) {
        Iterable<BoxGroup.Info> groups = BoxGroup.getAllGroups(boxAPI, attributesToGet.toArray(new String[attributesToGet.size()]));
        for (BoxGroup.Info groupInfo : groups) {
            handler.handle(groupToConnectorObject(groupInfo, attributesToGet));
        }
    }

    private void getGroup(Uid uid, ResultsHandler handler, OperationOptions ops, Set<String> attributesToGet) {
        BoxGroup group = new BoxGroup(boxAPI, uid.getUidValue());
        try {
            // Fetch a group
            BoxGroup.Info info = group.getInfo(attributesToGet.toArray(new String[attributesToGet.size()]));

            handler.handle(groupToConnectorObject(info, attributesToGet));

        } catch (BoxAPIException e) {
            if (isNotFoundError(e)) {
                LOGGER.warn("Unknown uid: {0}", group.getID());
                // It should not throw any exception
                return;
            }
            throw e;
        }
    }

    private void getGroup(Name name, ResultsHandler handler, OperationOptions ops, Set<String> attributesToGet) {
        // "List groups for enterprise" doesn't support find by "name" according to the following API spec:
        // https://developer.box.com/reference/get-groups/
        // But it supports query filter internally and the SDK has utility method: BoxGroup.getAllGroupsByName.
        // However, the SDK doesn't have a method with fields.
        // So we call own getAllGroupsByName method as the workaround.
        Iterable<BoxGroup.Info> groups = AdditionalAPI.getAllGroupsByName(boxAPI, name.getNameValue(),
                attributesToGet.toArray(new String[attributesToGet.size()]));

        for (BoxGroup.Info info : groups) {
            if (info.getName().equalsIgnoreCase(name.getNameValue())) {
                handler.handle(groupToConnectorObject(info, attributesToGet));
                break;
            }
        }
    }

    public void deleteGroup(Uid uid) {
        try {
            BoxGroup group = new BoxGroup(boxAPI, uid.getUidValue());
            group.delete();

        } catch (BoxAPIException e) {
            if (isNotFoundError(e)) {
                throw newUnknownUidException(uid, OBJECT_CLASS_GROUP, e);
            }
            throw e;
        }
    }

    private ConnectorObject groupToConnectorObject(BoxGroup.Info info, Set<String> attributesToGet) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();

        builder.setObjectClass(OBJECT_CLASS_GROUP);

        builder.setUid(new Uid(info.getID(), new Name(info.getName())));
        builder.setName(info.getName());

        // Standard
        if (attributesToGet.contains(ATTR_CREATED_AT)) {
            builder.addAttribute(ATTR_CREATED_AT, toZonedDateTime(info.getCreatedAt()));
        }
        if (attributesToGet.contains(ATTR_MODIFIED_AT)) {
            builder.addAttribute(ATTR_MODIFIED_AT, toZonedDateTime(info.getModifiedAt()));
        }

        // Full
        if (attributesToGet.contains(ATTR_PROVENANCE)) {
            builder.addAttribute(ATTR_PROVENANCE, info.getProvenance());
        }
        if (attributesToGet.contains(ATTR_DESCRIPTION)) {
            builder.addAttribute(ATTR_DESCRIPTION, info.getDescription());
        }
        if (attributesToGet.contains(ATTR_EXTERNAL_SYNC_IDENTIFIER)) {
            builder.addAttribute(ATTR_EXTERNAL_SYNC_IDENTIFIER, info.getExternalSyncIdentifier());
        }
        if (attributesToGet.contains(ATTR_INVITABILITY_LEVEL)) {
            builder.addAttribute(ATTR_INVITABILITY_LEVEL, info.getInvitabilityLevel());
        }
        if (attributesToGet.contains(ATTR_MEMBER_VIEWABILITY_LEVEL)) {
            builder.addAttribute(ATTR_MEMBER_VIEWABILITY_LEVEL, info.getMemberViewabilityLevel());
        }

        // Association
        if (attributesToGet.contains(ATTR_MEMBER) || attributesToGet.contains(ATTR_ADMIN)) {
            // Fetch the group members
            Iterable<BoxGroupMembership.Info> memberships = info.getResource().getAllMemberships();
            for (BoxGroupMembership.Info membershipInfo : memberships) {
                if (membershipInfo.getRole().equals(BoxUser.Role.USER) && attributesToGet.contains(ATTR_MEMBER)) {
                    builder.addAttribute(ATTR_MEMBER, membershipInfo.getID());
                } else if (membershipInfo.getRole().equals(BoxUser.Role.ADMIN) && attributesToGet.contains(ATTR_ADMIN)) {
                    builder.addAttribute(ATTR_ADMIN, membershipInfo.getID());
                }
            }
        }

        ConnectorObject connectorObject = builder.build();
        return connectorObject;
    }
}
