package in.lubble.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import in.lubble.app.marketplace.ItemSearchData;

public class SqliteHelper extends SQLiteOpenHelper {

    private static final String TAG = "SqliteHelper";

    public static final String DATABASE_NAME = "LubbleDatabase.db";
    private static final int DATABASE_VERSION = 1;

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
        db.execSQL(CREATE_TABLE_ITEM_SEARCH_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * CRUD TABLE_ITEM_SEARCH_DATA
     */

    public long createItemSearchData(ItemSearchData itemSearchData) {

        long createdRowNumber = 0;
        SQLiteDatabase db = DbSingleton.openDatabase();
        ContentValues values = new ContentValues();

        values.put(MPLACE_SEARCH_ITEM_ID, itemSearchData.getId());
        values.put(MPLACE_SEARCH_ITEM_NAME, itemSearchData.getName());

        createdRowNumber = db.insert(TABLE_ITEM_SEARCH_DATA, null, values);
        DbSingleton.closeDatabase();

        Log.i(TAG, "TABLE_ITEM_SEARCH_DATA: Created row #" + createdRowNumber);
        return createdRowNumber;
    }

    public ArrayList<ItemSearchData> readAllItemSearchData(String query) {

        SQLiteDatabase db = DbSingleton.openDatabase();

        String rawQuery = "SELECT * FROM " + TABLE_ITEM_SEARCH_DATA
                + " WHERE " + MPLACE_SEARCH_ITEM_NAME + " LIKE " + "'%" + query + "%'";
        Cursor cursor = db.rawQuery(rawQuery, null);
        ArrayList<ItemSearchData> itemSearchDataList = new ArrayList<>();

        while (cursor.moveToNext()) {
            ItemSearchData data = new ItemSearchData();
            data.setId(cursor.getInt(cursor.getColumnIndex(MPLACE_SEARCH_ITEM_ID)));
            data.setName(cursor.getString(cursor.getColumnIndex(MPLACE_SEARCH_ITEM_NAME)));
            itemSearchDataList.add(data);
        }

        Log.i(TAG, "TABLE_ITEM_SEARCH_DATA: Rows read #" + cursor.getCount());

        cursor.close();
        DbSingleton.closeDatabase();

        return itemSearchDataList;
    }

}
