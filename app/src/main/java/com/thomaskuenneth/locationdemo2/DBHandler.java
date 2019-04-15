package com.thomaskuenneth.locationdemo2;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DBHandler extends SQLiteOpenHelper {

    // Database Version
    public static final int DATABASE_VERSION = 1;

    static final String OVERALL_TABLENAME = "Bereich";

    // Database Name
    public static final String DATABASE_NAME = "Maps_Data";

    static final String OVERALL_IDENTIFIER = "uuid";
    static final String OVERALL_CLASS = "klasse";
    static final String OVERALL_LATITUDE = "latitude";
    static final String OVERALL_LONGITUDE  = "longitude";

    static final String OVERALL_PICTURE= "bilder";

    // Contacts table name
    public static final String TABLE_1 = "marker";
    public static final String TABLE1_C1  = "name";
    public static String[] table1_keys = {TABLE_1, OVERALL_IDENTIFIER, OVERALL_CLASS, OVERALL_LATITUDE, OVERALL_LONGITUDE,
            TABLE1_C1, OVERALL_PICTURE };
    static String CREATE_CONTACTS_TABLE1 = String.format("CREATE TABLE if not exists %s(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT,%s TEXT,%s TEXT," +
                    "%s TEXT, %s TEXT) ",

            TABLE_1, OVERALL_IDENTIFIER, OVERALL_CLASS, OVERALL_LATITUDE, OVERALL_LONGITUDE,
            TABLE1_C1, OVERALL_PICTURE);


    public static final String TABLE_2 = "GDP";
    public static final String TABLE2_C1  = "Reinigungsplan_vorhanden";
    public static final String TABLE2_C2  = "Fahrerausweis_vorhanden";
    public static final String TABLE2_C3  = "Getränk_vorhanden";
    public static final String TABLE2_C4  = "GLS_Getränkebecher_genutzt";
    public static final String TABLE2_C5  = "Nur_zugelassene_Getränke";
    public static final String TABLE2_C6  = "keine_Speisen_oder_Lebensmittel";
    public static final String TABLE2_C7  = "Fahrzeug_innen_sauber";
    public static String[] table2_keys = {TABLE_2, OVERALL_IDENTIFIER, OVERALL_CLASS, OVERALL_LATITUDE, OVERALL_LONGITUDE,
            TABLE2_C1, TABLE2_C2, TABLE2_C3, TABLE2_C4, TABLE2_C5, TABLE2_C6, TABLE2_C7};
    static String CREATE_CONTACTS_TABLE2 = String.format("CREATE TABLE if not exists %s(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT,%s TIMESTAMP,%s INT," +
                    "%s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT)",

            TABLE_2, OVERALL_IDENTIFIER, OVERALL_CLASS, OVERALL_LATITUDE, OVERALL_LONGITUDE,
            TABLE2_C1, TABLE2_C2, TABLE2_C3, TABLE2_C4, TABLE2_C5, TABLE2_C6, TABLE2_C7);



    public String getCreateStatement(String[] table_keys) {
        String output = "CREATE TABLE if not exists " + table_keys[0] + "(";
        for (int c = 1; c < table_keys.length; c++) {
            output += table_keys[c] + " TEXT, ";
        }
        output += "PRIMARY KEY (" + OVERALL_IDENTIFIER + "))";
        System.out.println("CreateSatement: " + output);
        return output;
    }

    public static String[][] DATABASE_TABLES = { table1_keys, table2_keys };

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
        db.execSQL(CREATE_CONTACTS_TABLE1);
        db.execSQL(CREATE_CONTACTS_TABLE2);
        db.execSQL(CREATE_CONTACTS_TABLE3);
        db.execSQL(CREATE_CONTACTS_TABLE4);
        db.execSQL(CREATE_CONTACTS_TABLE5);
        db.execSQL(CREATE_CONTACTS_TABLE6);
        db.execSQL(CREATE_CONTACTS_TABLE7);
        db.execSQL(CREATE_CONTACTS_TABLE8);
        db.execSQL(CREATE_CONTACTS_TABLE9);
        db.execSQL(CREATE_CONTACTS_TABLE10);
        */
    }

    public void createTableIfNotExists(SQLiteDatabase db, String TABLE_NAME) {
        if (TABLE_NAME == TABLE_1) {
            db.execSQL(CREATE_CONTACTS_TABLE1);
        } else if (TABLE_NAME == TABLE_2) {
            db.execSQL(CREATE_CONTACTS_TABLE2);
        }
    }

    public void dropTable(String TABLE_NAME) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_1);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_2);
        // Creating tables again
        onCreate(db);
    }


    // Adding new shop
    public boolean addRecord(HashMap<String, String> input, String[] tablekeys) {

        SQLiteDatabase db = this.getWritableDatabase();
        String original_table = tablekeys[0];
        // DO THIS WHEN DATABASE SCHEMA HAS CHANGED:
        //db.execSQL("DROP TABLE IF EXISTS " + table);

        //onCreate(db);
        createTableIfNotExists(db, original_table);
        ContentValues values = new ContentValues();

        Iterator it = input.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair.getValue() == null) {
                values.putNull(pair.getKey() + "");
            } else {
                values.put(pair.getKey()+"", pair.getValue()+"");
            }
            it.remove(); // avoids a ConcurrentModificationException
        }

        String table = original_table;
        boolean handled = false;
        int count = 2;
        while (!handled) {
            if (!tableExists(db, tablekeys[0])) {
                String statement = getCreateStatement(tablekeys);
                db.execSQL(statement);
            }

            // insert element
            if (db.insert(table, null, values) == -1) {
                // still not working...
                tablekeys[0] = original_table + count;
                table = original_table + count;
            } else {
                // working
                handled = true;
            }
            count++;
        }

        return true;

    }

    public ArrayList<Map<String, String>>  getMapOfAllTables() {
        ArrayList<Map<String, String>> allElements = new ArrayList<>();
        Map<String, String> myMap;

        SQLiteDatabase db = this.getWritableDatabase();

        for (int t = 0; t < DATABASE_TABLES.length; t++) {
            String TABLE_NAME = DATABASE_TABLES[t][0];
            String selectQuery = "SELECT * FROM " + TABLE_NAME;
            if (tableExists(db, TABLE_NAME)) {
                Cursor cursor = db.rawQuery(selectQuery, null);
                if (cursor.moveToFirst()) {
                    do {
                        int count = cursor.getCount();
                        System.out.println("count: " + count);
                        if (count > 0) {
                            //for (int z = 0; z < cursor.getCount(); z++){
                            myMap = new HashMap<String, String>();
                            myMap.put(OVERALL_TABLENAME, TABLE_NAME);
                            for (int column = 0; column < cursor.getColumnCount(); column++) {
                                myMap.put(cursor.getColumnName(column), cursor.getString(column));
                            }
                            allElements.add(myMap);
                            System.out.println(myMap);
                            //}
                        }
                    } while (cursor.moveToNext());
                }
                int count = cursor.getCount();
                cursor.close();
            }

        }
        System.out.println(allElements);
        System.out.println(allElements.size());
        return allElements;
    }

    public boolean tableExists(SQLiteDatabase db, String tableName) {

        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }


    // Getting All Shops
    public ArrayList<ArrayList<String>> getAllRecords(String table) {
        int numRows = (int) DatabaseUtils.queryNumEntries(this.getWritableDatabase(), table);
        System.out.println("d hjnum" + numRows);
        ArrayList<ArrayList<String>> shopList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + table;

        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println("d hj" + db);
        Cursor cursor = db.rawQuery(selectQuery, null);
        System.out.println("d hj" + cursor);
        System.out.println("d hj" + cursor);
        System.out.println("d hj" + cursor);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                System.out.println("hier964hnoicg");
                String shop = "";
                int count = cursor.getCount();
                if (count > 0) {
                    ArrayList<String> record = new ArrayList<>();
                    for (int z = 0; z < cursor.getCount(); z++){
                        record.add(cursor.getString(z));
                        //shop += cursor.getString(z);
                    }
                    shopList.add(record);
                }
            } while (cursor.moveToNext());
        }
        int count = cursor.getCount();
        System.out.println("d hj" + count);
        cursor.close();

        // return contact list
        return shopList;
    }



    public ArrayList<Map<String, String>> executeSQLToDict(Context context, String[] TABLE_KEYS, String sql) {
        String TABLE_NAME = TABLE_KEYS[0];
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<Map<String, String>> recordsList = new ArrayList<>();
        if (tableExists(db, TABLE_NAME)) {
            // Select All Query
            String selectQuery = sql;


            Cursor cursor = db.rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                System.out.println("hier964hnoicg");
                String shop = "";
                int count = cursor.getCount();
                if (count > 0) {
                    System.out.println("count: " + count);
                    System.out.println(cursor.getColumnCount());
                    System.out.println(TABLE_KEYS.length);
                    for (int z = 0; z < cursor.getCount(); z++){
                        Map<String, String> record = new HashMap<>();
                        for (int n = 0; n < cursor.getColumnCount(); n++) {
                            System.out.println("hallohierda " + n+1);
                            System.out.println("hallohierda " + TABLE_KEYS.length);
                            System.out.println("hallohierda " + TABLE_KEYS);
                            System.out.println("hallohierda " + cursor.getString(n));
                            System.out.println("hallohierda ");
                            if (TABLE_KEYS.length > n+1) {
                                record.put(TABLE_KEYS[n+1], cursor.getString(n));
                            } else {
                                showAlert(context, "Fehler:",
                                        "Etwas ist schiefgelaufen mit dem Schema der Tabelle " + TABLE_NAME);
                            }
                        }
                        recordsList.add(record);
                        cursor.moveToNext();
                    }
                }
            }
            int count = cursor.getCount();
            System.out.println("d hj" + count);
            cursor.close();

        } else {
            return recordsList;
        }



        // return contact list
        return recordsList;
    }

    public void showAlert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }




    // Deleting a shop
    public void deleteAllRecords(String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table, "?" + " = ?",
                new String[] { });
        db.close();
    }

    public String getTableAsString(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }

    public String createStatementWithDifferentTableName(String createstatement, int number) {
        String sub = createstatement.substring(0, createstatement.indexOf("("));
        String table_name = sub.substring(sub.lastIndexOf(" ")).replace(" ", "");
        String end =createstatement.replace(table_name, table_name + number);
        System.out.println("oufdgh " + end);
        return end;
    }

    public ArrayList<String> getAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        ArrayList<String> tables = new ArrayList<>();
        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                if (!c.getString(0).equals("android_metadata")) {
                    tables.add(c.getString(0));
                }
                c.moveToNext();
            }
        }
        return tables;
    }


}

