package com.thirthydaylabs.dictionaryapp.app;

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
    private static String TABLE_NAME = "ab";
    private static final int DATABASE_VERSION = 1;

    public DatabaseController(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public Word search(String query, boolean languageSwitch){

        if(languageSwitch){
            TABLE_NAME = "ab";
        }else{
            TABLE_NAME = "ba";
        }

        Word result = new Word();
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] sqlSelect = {"word", "definition"};
        String sqlTables = "words";

        qb.setTables(sqlTables);
        Cursor c ;

        c = db.rawQuery("SELECT word, definition FROM "
                + TABLE_NAME + " where " + "word" + " like '" + query
                + "' ORDER BY word ASC", null);

        if(!c.moveToFirst()){
            c = db.rawQuery("SELECT word, definition FROM "
                    + TABLE_NAME + " where " + "word" + " like '" + query
                    + "%' ORDER BY word ASC", null);
        }

        if (!c.moveToFirst()) {
            c = db.rawQuery("SELECT word, definition FROM "
                    + TABLE_NAME + " where " + "word" + " like '%" + query
                    + "%' ORDER BY word ASC", null);
        }

        if (c.moveToFirst()) {
            c.moveToFirst();
            result.setWord(c.getString(0));
            result.setDefinition(c.getString(1));
            // Adding contact to list
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
