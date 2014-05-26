package com.mardox.dictionaryapp.app;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by HooMan on 25/05/2014.
 */
public class DatabaseController extends SQLiteAssetHelper {


    private static final String DATABASE_NAME = "definitions.sqlite";
    private static final String TABLE_NAME = "words";
    private static final int DATABASE_VERSION = 1;

    public DatabaseController(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public Word search(String query){

        Word result = new Word();
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] sqlSelect = {"word", "definition", "type"};
        String sqlTables = "words";

        qb.setTables(sqlTables);
//        Cursor c = qb.query(db, sqlSelect, "word" + "=?", new String [] {query}, null,
//                null, null, null);

        Cursor c = db.rawQuery("SELECT word, definition, type FROM "
                + TABLE_NAME + " where " + "word" + " like '" + query
                + "%'", null);

        c.moveToFirst();

        if (c.moveToFirst()) {
                result.setWord(c.getString(0));
                result.setDefinition(c.getString(1));
                result.setType(c.getString(2));
                // Adding contact to list
        }else{
            c = db.rawQuery("SELECT word, definition, type FROM "
                    + TABLE_NAME + " where " + "word" + " like '%" + query
                    + "%'", null);
            if (c.moveToFirst()) {
                result.setWord(c.getString(0));
                result.setDefinition(c.getString(1));
                result.setType(c.getString(2));
                // Adding contact to list
            }
        }

        return result;



    }


    public Cursor getEmployees() {

        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] sqlSelect = {"0 _id", "FirstName", "LastName"};
        String sqlTables = "Employees";

        qb.setTables(sqlTables);
        Cursor c = qb.query(db, sqlSelect, null, null,
                null, null, null);

        c.moveToFirst();
        return c;

    }


}
