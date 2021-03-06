package org.gooru.nucleus.reports.infra.constants;

public final class ColumnFamilyConstants {

    public static final String USER_GROUP_ASSOCIATION = "user_group_association";
    public static final String CLASS_ACTIVITY = "class_activity";
    public static final String CLASS = "class";
    public static final String DIM_RESOURCE = "dim_resource";
    public static final String RESOURCE = "resource";
    public static final String DIM_USER = "dim_user";
    public static final String USER = "user";
    public static final String COLLECTION_ITEM_ASSOC = "collection_item_assoc";
    public static final String SESSIONS = "sessions";
    public static final String SESSION_ACTIVITY = "session_activity";

    private ColumnFamilyConstants() {
        throw new AssertionError();
    }
}
