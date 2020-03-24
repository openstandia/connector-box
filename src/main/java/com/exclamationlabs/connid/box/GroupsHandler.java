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

    private static final String ATTR_NAME = "name";
    private static final String ATTR_ID = "groupID";
    private static final String ATTR_PROVENANCE = "provenance";
    private static final String ATTR_IDENTIFIER = "external_sync_identifier";
    private static final String ATTR_DESCRIPTION = "description";
    private static final String ATTR_INVITABILITY = "invitability_level";
    private static final String ATTR_VIEWABILITY = "member_viewability_level";
    private static final String ATTR_CREATED = "created_at";
    private static final String ATTR_MODIFIED = "modified_at";
    private static final String ATTR_SYNC = "is_sync_enabled";
    private static final String ATTR_MEMBERS = "member";
    private static final String ATTR_ADMINS = "admin";
    private static final String ATTR_CO_OWNER = "co_owner";
    private static final String ATTR_EDITOR = "editor";
    private static final String ATTR_PREVIEWER = "previewer";
    private static final String ATTR_PREVIEWER_UPLOADER = "previewer_uploader";
    private static final String ATTR_UPLOADER = "uploader";
    private static final String ATTR_VIEWER = "viewer";
    private static final String ATTR_VIEWER_UPLOADER = "viewer_uploader";

    private BoxDeveloperEditionAPIConnection boxDeveloperEditionAPIConnection;

    public GroupsHandler(BoxDeveloperEditionAPIConnection boxDeveloperEditionAPIConnection) {
        this.boxDeveloperEditionAPIConnection = boxDeveloperEditionAPIConnection;
    }

    public ObjectClassInfo getGroupSchema() {
        ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();

        builder.setType(ObjectClass.GROUP_NAME);

        AttributeInfoBuilder attrOwner = new AttributeInfoBuilder(ATTR_CO_OWNER);
        attrOwner.setMultiValued(true);
        attrOwner.setUpdateable(true);
        builder.addAttributeInfo(attrOwner.build());

        AttributeInfoBuilder attrEditor = new AttributeInfoBuilder(ATTR_EDITOR);
        attrEditor.setMultiValued(true);
        attrEditor.setUpdateable(true);
        builder.addAttributeInfo(attrEditor.build());

        AttributeInfoBuilder attrPreviewer = new AttributeInfoBuilder(ATTR_PREVIEWER);
        attrPreviewer.setMultiValued(true);
        attrPreviewer.setUpdateable(true);
        attrPreviewer.setReturnedByDefault(false);
        builder.addAttributeInfo(attrPreviewer.build());

        AttributeInfoBuilder attrPrevUpl = new AttributeInfoBuilder(ATTR_PREVIEWER_UPLOADER);
        attrPrevUpl.setMultiValued(true);
        attrPrevUpl.setUpdateable(true);
        attrPrevUpl.setReturnedByDefault(false);
        builder.addAttributeInfo(attrPrevUpl.build());

        AttributeInfoBuilder attrUploader = new AttributeInfoBuilder(ATTR_UPLOADER);
        attrUploader.setMultiValued(true);
        attrUploader.setUpdateable(true);
        attrUploader.setReturnedByDefault(false);
        builder.addAttributeInfo(attrUploader.build());

        AttributeInfoBuilder attrViewer = new AttributeInfoBuilder(ATTR_VIEWER);
        attrViewer.setMultiValued(true);
        attrViewer.setUpdateable(true);
        attrViewer.setReturnedByDefault(false);
        builder.addAttributeInfo(attrViewer.build());

        AttributeInfoBuilder attrViewUpl = new AttributeInfoBuilder(ATTR_VIEWER_UPLOADER);
        attrViewUpl.setMultiValued(true);
        attrViewUpl.setUpdateable(true);
        attrViewUpl.setReturnedByDefault(false);
        builder.addAttributeInfo(attrViewUpl.build());

        AttributeInfoBuilder attrProvenanceBuilder = new AttributeInfoBuilder(ATTR_PROVENANCE);
        attrProvenanceBuilder.setUpdateable(true);
        attrProvenanceBuilder.setReturnedByDefault(false);
        builder.addAttributeInfo(attrProvenanceBuilder.build());

        AttributeInfoBuilder attrGroupId = new AttributeInfoBuilder(ATTR_ID);
        attrGroupId.setUpdateable(false);
        builder.addAttributeInfo(attrGroupId.build());

        AttributeInfoBuilder attrMembers = new AttributeInfoBuilder(ATTR_MEMBERS);
        attrMembers.setMultiValued(true);
        attrMembers.setUpdateable(true);
        builder.addAttributeInfo(attrMembers.build());

        AttributeInfoBuilder attrAdmins = new AttributeInfoBuilder(ATTR_ADMINS);
        attrAdmins.setMultiValued(true);
        attrAdmins.setUpdateable(true);
        builder.addAttributeInfo(attrAdmins.build());

        AttributeInfoBuilder attrIdentifierBuilder = new AttributeInfoBuilder(ATTR_IDENTIFIER);
        attrIdentifierBuilder.setUpdateable(true);
        attrIdentifierBuilder.setReturnedByDefault(false);
        builder.addAttributeInfo(attrIdentifierBuilder.build());

        AttributeInfoBuilder attrDescriptionBuilder = new AttributeInfoBuilder(ATTR_DESCRIPTION);
        attrDescriptionBuilder.setUpdateable(true);
        attrDescriptionBuilder.setReturnedByDefault(false);
        builder.addAttributeInfo(attrDescriptionBuilder.build());

        AttributeInfoBuilder attrInvitabilityBuilder = new AttributeInfoBuilder(ATTR_INVITABILITY);
        attrInvitabilityBuilder.setUpdateable(true);
        attrInvitabilityBuilder.setReturnedByDefault(false);
        builder.addAttributeInfo(attrInvitabilityBuilder.build());

        AttributeInfoBuilder attrViewabilityBuilder = new AttributeInfoBuilder(ATTR_VIEWABILITY);
        attrViewabilityBuilder.setUpdateable(true);
        attrViewabilityBuilder.setReturnedByDefault(false);
        builder.addAttributeInfo(attrViewabilityBuilder.build());

        AttributeInfoBuilder attrIsSyncEnabledBuilder = new AttributeInfoBuilder(ATTR_SYNC, Boolean.class);
        attrIsSyncEnabledBuilder.setUpdateable(true);
        attrIsSyncEnabledBuilder.setReturnedByDefault(false);
        builder.addAttributeInfo(attrIsSyncEnabledBuilder.build());

        AttributeInfoBuilder attrCreated = new AttributeInfoBuilder(ATTR_CREATED);
        attrCreated.setUpdateable(false);
        attrCreated.setCreateable(false);
        builder.addAttributeInfo(attrCreated.build());

        AttributeInfoBuilder attrModified = new AttributeInfoBuilder(ATTR_MODIFIED);
        attrModified.setUpdateable(false);
        attrModified.setCreateable(false);
        builder.addAttributeInfo(attrModified.build());

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

            } else if (attr.getName().equals(ATTR_IDENTIFIER)) {
                externalSyncIdentifier = AttributeUtil.getStringValue(attr);

            } else if (attr.getName().equals(ATTR_INVITABILITY)) {
                invitabilityLevel = AttributeUtil.getStringValue(attr);

            } else if (attr.getName().equals(ATTR_VIEWABILITY)) {
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
                    boxDeveloperEditionAPIConnection,
                    name,
                    provenance,
                    externalSyncIdentifier,
                    description,
                    invitabilityLevel,
                    memberViewabilityLevel
            );
            return new Uid(groupInfo.getID(), new Name(name));

        } catch(BoxAPIException e) {
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

        BoxGroup group = new BoxGroup(boxDeveloperEditionAPIConnection, uid.getUidValue());
        BoxGroup.Info info = group.new Info();

        for (AttributeDelta delta : modifications) {
            if (delta.getName().equals(ATTR_NAME)) {
                info.setName(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_PROVENANCE)) {
                info.setProvenance(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_IDENTIFIER)) {
                info.setProvenance(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_DESCRIPTION)) {
                info.setProvenance(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_INVITABILITY)) {
                info.setProvenance(getStringValue(delta));

            } else if (delta.getName().equals(ATTR_VIEWABILITY)) {
                info.setProvenance(getStringValue(delta));
            }
        }

        if (StringUtil.isBlank(info.getName())) {
            throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_NAME);
        }

        info.getResource().updateInfo(info);

        // Box doesn't support to modify group's id
        return null;
    }

    public void query(String query, ResultsHandler handler, OperationOptions ops) {
        LOGGER.info("GroupsHandler query VALUE: {0}", query);

        if (query == null) {
            getAllGroups(handler, ops);
        } else {
            getGroup(query, handler, ops);
        }
    }

    private void getAllGroups(ResultsHandler handler, OperationOptions ops) {
        Iterable<BoxGroup.Info> groups = BoxGroup.getAllGroups(boxDeveloperEditionAPIConnection);
        for (BoxGroup.Info groupInfo : groups) {
            handler.handle(groupToConnectorObject(groupInfo));
        }
    }

    private void getGroup(String uid, ResultsHandler handler, OperationOptions ops) {
        BoxGroup group = new BoxGroup(boxDeveloperEditionAPIConnection, uid);
        try {
            // Fetch a group
            BoxGroup.Info info = group.getInfo();

            handler.handle(groupToConnectorObject(info));

        } catch (BoxAPIException e) {
            if (isNotFoundError(e)) {
                LOGGER.warn("Unknown uid: {0}", group.getID());
                throw new UnknownUidException(new Uid(group.getID()), ObjectClass.GROUP);
            }
            throw e;
        }
    }
    public void deleteGroup(Uid uid) {
        BoxGroup group = new BoxGroup(boxDeveloperEditionAPIConnection, uid.toString());
        group.delete();
    }

    private ConnectorObject groupToConnectorObject(BoxGroup.Info info) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();

        builder.setUid(new Uid(info.getID(), new Name(info.getName())));

        builder.setName(info.getName());
        builder.addAttribute(ATTR_ID, info.getID());

        builder.addAttribute(ATTR_PROVENANCE, info.getProvenance());
        builder.addAttribute(ATTR_DESCRIPTION, info.getDescription());
        builder.addAttribute(ATTR_SYNC, info.getExternalSyncIdentifier());
        builder.addAttribute(ATTR_INVITABILITY, info.getInvitabilityLevel());
        builder.addAttribute(ATTR_VIEWABILITY, info.getMemberViewabilityLevel());
        builder.addAttribute(ATTR_CREATED, info.getCreatedAt().getTime());
        builder.addAttribute(ATTR_MODIFIED, info.getModifiedAt().getTime());

        // Fetch the group members
        Iterable<BoxGroupMembership.Info> memberships = info.getResource().getAllMemberships();
        for (BoxGroupMembership.Info membershipInfo : memberships) {
            if (membershipInfo.getRole().equals(BoxUser.Role.USER)) {
                builder.addAttribute(ATTR_MEMBERS, membershipInfo.getID());
            } else if (membershipInfo.getRole().equals(BoxUser.Role.ADMIN)) {
                builder.addAttribute(ATTR_ADMINS, membershipInfo.getID());

                //I don't know if this is right
            } else if (membershipInfo.getRole().equals(BoxUser.Role.COADMIN)) {
                builder.addAttribute(ATTR_CO_OWNER, membershipInfo.getID());
            }
        }

        ConnectorObject connectorObject = builder.build();
        return connectorObject;
    }
}
