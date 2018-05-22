
package com.example.android.splitwise.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android.splitwise.data.DataObject;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDataSource<T extends DataObject> {

	private final DataBaseHelper dbHelper;
	private final String table;
	private final String[] columns;

	AbstractDataSource(DataBaseHelper dbHelper, String table, String[] columns) {
		this.dbHelper = dbHelper; 
		this.table = table;
		this.columns = columns;
	}

	SQLiteDatabase getDatabase() {
		return dbHelper.getWritableDatabase();
	}

	long insert(T object) {
		long insertId = getDatabase().insert(
				table,
				null,
				toContentValues(object));
		object.setId(insertId);
		return insertId;
	}

	void update(T object) {
		long id = object.getId();
		getDatabase().update(
				table,
				toContentValues(object),
				DataBaseHelper.COLUMN_ID + " = ?", new String[] { Long.toString(id) });
	}

	public void delete(long id) {
		getDatabase().delete(
				table,
				DataBaseHelper.COLUMN_ID + " = ?", new String[] { Long.toString(id) });
	}

	public T get(long id) {
		Cursor cursor = getDatabase().query(
				table, columns,
				DataBaseHelper.COLUMN_ID + " = ?", new String[] { Long.toString(id) },
				null, null, null, null);
	
		cursor.moveToFirst();
		T object = fromCursor(cursor);
		cursor.close();
	
		return object;
	}

	public Cursor listAll() {
		return getDatabase().query(
				table, columns,
				null, null, null, null, null);
	}

	List<T> getAllFromCursor(Cursor cursor) {
		List<T> objects = new ArrayList<>();

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			T object = fromCursor(cursor);
			objects.add(object);
			cursor.moveToNext();
		}
		cursor.close();

		return objects;
		
	}

	protected abstract ContentValues toContentValues(T object);

	protected abstract T fromCursor(Cursor cursor);

}
