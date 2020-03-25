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

import java.util.Set;

public class GroupsHandler extends AbstractHandler {

    private static final Log LOGGER = Log.getLog(GroupsHandler.class);

    // Use the type of the Box Group resource:
    // https://developer.box.com/reference/resources/group/
    public static final ObjectClass OBJECT_CLASS_GROUP = new ObjectClass("group");

    private static final String ATTR_NAME = "name";
    private static final String ATTR_PROVENANCE = "provenance";
    private static final String ATTR_EXTERNAL_SYNC_IDENTIFIER = "external_sync_identifier";
    private static final String ATTR_DESCRIPTION = "description";
    private static final String ATTR_INVITABILITY_LEVEL = "invitability_level";
    private static final String ATTR_VIEWABILITY_LEVEL = "member_viewability_level";
    private static final String ATTR_CREATED_AT = "created_at";
    private static final String ATTR_MODIFIED_AT = "modified_at";

    // Group membership
    // There are two roles of members: "member" and "admin"
    // https://developer.box.com/reference/resources/group-membership/#param-role
    private static final String ATTR_MEMBER = "member";
    private static final String ATTR_ADMIN = "admin";

    // Collaborations for group
    private static final String ATTR_CO_OWNER = "co_owner";
    private static final String ATTR_EDITOR = "editor";
    private static final String ATTR_PREVIEWER = "previewer";
    private static final String ATTR_PREVIEWER_UPLOADER = "previewer_uploader";
    private static final String ATTR_UPLOADER = "uploader";
    private static final String ATTR_VIEWER = "viewer";
    private static final String ATTR_VIEWER_UPLOADER = "viewer_uploader";

    private BoxAPIConnection boxAPI;

    public GroupsHandler(BoxAPIConnection boxAPI) {
        this.boxAPI = boxAPI;
    }

    public ObjectClassInfo getGroupSchema() {
        ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();
        builder.setType(OBJECT_CLASS_GROUP.getObjectClassValue());

        // id (__UID__)
        // Caution: Don't define a schema for "id" of group because the name conflicts with midPoint side.
//        builder.addAttributeInfo(
//                AttributeInfoBuilder.define(Uid.NAME)
//                        .setRequired(false) // Must be optional. It is not present for create operations
//                        .setCreateable(false)
//                        .setUpdateable(false)
//                        .setNativeName("id")
//                        .build()
//        );

        // name (__NAME__)
        builder.addAttributeInfo(AttributeInfoBuilder.define(Name.NAME)
                .setRequired(true)
                .setNativeName(ATTR_NAME)
                .setSubtype(AttributeInfo.Subtypes.STRING_CASE_IGNORE)
                .build());

        // provenance
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_PROVENANCE)
                .build());

        // external_sync_identifier
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_EXTERNAL_SYNC_IDENTIFIER)
                .build());


        // description
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_DESCRIPTION)
                .build());

        // invitability_level
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_INVITABILITY_LEVEL)
                .build());

        // member_viewability_level
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_VIEWABILITY_LEVEL)
                .build());

        // created_at
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_CREATED_AT)
                .setCreateable(false)
                .setUpdateable(false)
                .build());

        // modified_at
        builder.addAttributeInfo(AttributeInfoBuilder.define(ATTR_MODIFIED_AT)
                .setCreateable(false)
                .setUpdateable(false)
                .build());

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

            } else if (attr.getName().equals(ATTR_VIEWABILITY_LEVEL)) {
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

            } else if (delta.getName().equals(ATTR_VIEWABILITY_LEVEL)) {
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

        if (query == null) {
            getAllGroups(handler, ops);
        } else {
            if (query.isByUid()) {
                getGroup(query.uid, handler, ops);
            } else {
                getGroup(query.name, handler, ops);
            }
        }
    }

    private void getAllGroups(ResultsHandler handler, OperationOptions ops) {
        Iterable<BoxGroup.Info> groups = BoxGroup.getAllGroups(boxAPI);
        for (BoxGroup.Info groupInfo : groups) {
            handler.handle(groupToConnectorObject(groupInfo));
        }
    }

    private void getGroup(Uid uid, ResultsHandler handler, OperationOptions ops) {
        BoxGroup group = new BoxGroup(boxAPI, uid.getUidValue());
        try {
            // Fetch a group
            BoxGroup.Info info = group.getInfo();

            handler.handle(groupToConnectorObject(info));

        } catch (BoxAPIException e) {
            if (isNotFoundError(e)) {
                LOGGER.warn("Unknown uid: {0}", group.getID());
                throw new UnknownUidException(new Uid(group.getID()), OBJECT_CLASS_GROUP);
            }
            throw e;
        }
    }

    private void getGroup(Name name, ResultsHandler handler, OperationOptions ops) {
        // "List groups for enterprise" doesn't support find by "name" according to the following API spec:
        // https://developer.box.com/reference/get-groups/
        // But it supports query filter internally and the SDK has utility method: BoxGroup.getAllGroupsByName
        // Also, this api returns only 4 attributes, type, id, name and group_type.
        Iterable<BoxGroup.Info> groups = BoxGroup.getAllGroupsByName(boxAPI, name.getNameValue());
        for (BoxGroup.Info info : groups) {
            if (info.getName().equalsIgnoreCase(name.getNameValue())) {
                handler.handle(groupToConnectorObject(info));
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

    private ConnectorObject groupToConnectorObject(BoxGroup.Info info) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();

        builder.setObjectClass(OBJECT_CLASS_GROUP);

        builder.setUid(new Uid(info.getID(), new Name(info.getName())));
        builder.setName(info.getName());

        builder.addAttribute(ATTR_PROVENANCE, info.getProvenance());
        builder.addAttribute(ATTR_DESCRIPTION, info.getDescription());
        builder.addAttribute(ATTR_EXTERNAL_SYNC_IDENTIFIER, info.getExternalSyncIdentifier());
        builder.addAttribute(ATTR_INVITABILITY_LEVEL, info.getInvitabilityLevel());
        builder.addAttribute(ATTR_VIEWABILITY_LEVEL, info.getMemberViewabilityLevel());
        builder.addAttribute(ATTR_CREATED_AT, info.getCreatedAt().getTime());
        builder.addAttribute(ATTR_MODIFIED_AT, info.getModifiedAt().getTime());

        // Fetch the group members
        Iterable<BoxGroupMembership.Info> memberships = info.getResource().getAllMemberships();
        for (BoxGroupMembership.Info membershipInfo : memberships) {
            if (membershipInfo.getRole().equals(BoxUser.Role.USER)) {
                builder.addAttribute(ATTR_MEMBER, membershipInfo.getID());
            } else if (membershipInfo.getRole().equals(BoxUser.Role.ADMIN)) {
                builder.addAttribute(ATTR_ADMIN, membershipInfo.getID());
            }
        }

        ConnectorObject connectorObject = builder.build();
        return connectorObject;
    }
}
