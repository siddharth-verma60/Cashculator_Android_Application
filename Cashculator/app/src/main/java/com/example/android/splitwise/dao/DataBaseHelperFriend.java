
package com.example.android.splitwise.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.android.splitwise.dao.DataBaseHelper.COLUMN_CALCULATION_ID;
import static com.example.android.splitwise.dao.DataBaseHelper.COLUMN_CURRENCY_CODE;
import static com.example.android.splitwise.dao.DataBaseHelper.COLUMN_CURRENCY_ID;
import static com.example.android.splitwise.dao.DataBaseHelper.COLUMN_DATE;
import static com.example.android.splitwise.dao.DataBaseHelper.COLUMN_EXPENSE_ID;
import static com.example.android.splitwise.dao.DataBaseHelper.COLUMN_NAME;
import static com.example.android.splitwise.dao.DataBaseHelper.COLUMN_PERSON_ID;
import static com.example.android.splitwise.dao.DataBaseHelper.COLUMN_RATE_MAIN;
import static com.example.android.splitwise.dao.DataBaseHelper.COLUMN_RATE_THIS;
import static com.example.android.splitwise.dao.DataBaseHelper.COLUMN_WEIGHT;
import static com.example.android.splitwise.dao.DataBaseHelper.TABLE_CURRENCIES;
import static com.example.android.splitwise.dao.DataBaseHelper.TABLE_EXPENSES;
import static com.example.android.splitwise.dao.DataBaseHelper.TABLE_FRIENDS;
import static com.example.android.splitwise.dao.DataBaseHelper.TABLE_FRIENDS_EXPENSES;
import static com.example.android.splitwise.dao.DataBaseHelper.TABLE_PERSONS;
import static com.example.android.splitwise.dao.DataBaseHelper.TABLE_SPLIT_WEIGHTS;

public class DataBaseHelperFriend extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "friend.db";
	private static final int DATABASE_VERSION = 2;

	public static final String TABLE_CALCULATIONS = "calculations";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_AMOUNT = "amount";
	public static final String COLUMN_PHONE = "phone";

	public DataBaseHelperFriend(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	private void dropAll(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALCULATIONS);
	}

	private void createV1(SQLiteDatabase db) {
		String sql;

		sql = "CREATE TABLE " + TABLE_CALCULATIONS + "(" +
				COLUMN_ID + " integer primary key autoincrement, " +
				COLUMN_TITLE + " text not null," +
				COLUMN_PHONE + " text not null," +
				COLUMN_AMOUNT + " text not null)" ;
		db.execSQL(sql);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		onUpgrade(db, 0, DATABASE_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(DataBaseHelperFriend.class.getName(), String.format("Upgrading from version %d to %d", oldVersion, newVersion));

		if (oldVersion > DATABASE_VERSION) {
			// TODO: Prompt for confirmation before wiping database
			Log.w(DataBaseHelperFriend.class.getName(), String.format("Unsupported database version %d; recreating from scratch", oldVersion));
			dropAll(db);
			oldVersion = 0;
		}

		if (oldVersion < 1) createV1(db);
	}

}
