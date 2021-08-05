package in.lubble.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteHelper extends SQLiteOpenHelper {

    private static final String TAG = "SqliteHelper";

    public static final String DATABASE_NAME = "LubbleDatabase.db";
    private static final int DATABASE_VERSION = 2;

    /**
     * COLUMNS
     */
    private static final String MPLACE_SEARCH_ITEM_ID = "item_id";
    private static final String MPLACE_SEARCH_ITEM_NAME = "item_name";
    /**
     * TABLE NAMES
     */
    private static final String TABLE_ITEM_SEARCH_DATA = "TABLE_ITEM_SEARCH_DATA";
    /**
     * CREATE TABLE QUERIES
     */
    private static final String CREATE_TABLE_ITEM_SEARCH_DATA = "CREATE TABLE "
            + "IF NOT EXISTS "
            + TABLE_ITEM_SEARCH_DATA
            + "("
            + MPLACE_SEARCH_ITEM_ID + " INTEGER PRIMARY KEY, "
            + MPLACE_SEARCH_ITEM_NAME + " TEXT "
            + ")";

    public SqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // no more tables reqd.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                // Delete table as we switched to Algolia search
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM_SEARCH_DATA);
                break;
        }
    }

}
