
package com.example.android.splitwise.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.android.splitwise.data.Calculation;
import com.example.android.splitwise.data.Person;

public class PersonDataSource extends AbstractDataSource<Person> {

	private final Calculation calculation;

	private static final String[] COLUMNS = {
		DataBaseHelper.COLUMN_ID,
		DataBaseHelper.COLUMN_CALCULATION_ID,
		DataBaseHelper.COLUMN_NAME
	};

	public PersonDataSource(DataBaseHelper dbHelper, Calculation calculation) {
		super(dbHelper, DataBaseHelper.TABLE_PERSONS, COLUMNS);
		this.calculation = calculation;
	}

	@Override
	protected ContentValues toContentValues(Person person) {
		ContentValues values = new ContentValues();
		values.put(DataBaseHelper.COLUMN_CALCULATION_ID, person.getCalculation().getId());
		values.put(DataBaseHelper.COLUMN_NAME, person.getName());
		return values;
	}

	@Override
	public Person fromCursor(Cursor cursor) {
		Person person = new Person(calculation);
		person.setId(cursor.getLong(0));
		person.setName(cursor.getString(2));
		return person;
	}

	public Cursor listByCalculation() {
		return getDatabase().query(
				DataBaseHelper.TABLE_PERSONS, COLUMNS,
				DataBaseHelper.COLUMN_CALCULATION_ID + " = ?", new String[] { Long.toString(calculation.getId()) },
				null, null, null);
	}

}
