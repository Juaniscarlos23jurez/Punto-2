package com.example.punto2.sqlite;
import android.provider.BaseColumns;

public final class DBContract {
    private DBContract() {}

    public static class TokenEntry implements BaseColumns {
        public static final String TABLE_NAME = "tokens";
        public static final String COLUMN_NAME_TOKEN = "token";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_TOKEN + " TEXT)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
