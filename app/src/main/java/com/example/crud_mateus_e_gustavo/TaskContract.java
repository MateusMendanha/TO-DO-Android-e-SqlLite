package com.example.crud_mateus_e_gustavo;

import android.provider.BaseColumns;

public final class TaskContract {
    private TaskContract() {}

    public static class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_DESCRIPTION = "description";
    }
}
