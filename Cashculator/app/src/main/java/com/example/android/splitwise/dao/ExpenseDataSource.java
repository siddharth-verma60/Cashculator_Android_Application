
package com.example.android.splitwise.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.android.splitwise.data.Calculation;
import com.example.android.splitwise.data.Currency;
import com.example.android.splitwise.data.Expense;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class ExpenseDataSource extends AbstractDataSource<Expense> {

	private final Calculation calculation;

	private static final String[] COLUMNS = {
		DataBaseHelper.COLUMN_ID,
		DataBaseHelper.COLUMN_PERSON_ID,
		DataBaseHelper.COLUMN_TITLE,
		DataBaseHelper.COLUMN_AMOUNT,
		DataBaseHelper.COLUMN_CURRENCY_ID,
		DataBaseHelper.COLUMN_DATE
	};

	public ExpenseDataSource(DataBaseHelper dbHelper, Calculation calculation) {
		super(dbHelper, DataBaseHelper.TABLE_EXPENSES, COLUMNS);
		this.calculation = calculation;
	}

	@Override
	protected ContentValues toContentValues(Expense expense) {
		long fixedAmount = Math.round(expense.getAmount() * expense.getCurrency().getDecimalFactor());
		ContentValues values = new ContentValues();
		values.put(DataBaseHelper.COLUMN_PERSON_ID, expense.getPerson().getId());
		values.put(DataBaseHelper.COLUMN_TITLE, expense.getTitle());
		values.put(DataBaseHelper.COLUMN_AMOUNT, fixedAmount);
		values.put(DataBaseHelper.COLUMN_CURRENCY_ID, expense.getCurrency().getId());
		values.put(DataBaseHelper.COLUMN_DATE, expense.getDate().getTimeInMillis());
		return values;
	}

	@Override
	public Expense fromCursor(Cursor cursor) {
		long id = cursor.getLong(0);
		Expense expense = new Expense(calculation);
		expense.setId(id);
		expense.setPerson(calculation.getPersonById(cursor.getLong(1)));
		expense.setTitle(cursor.getString(2));

		long fixedAmount = cursor.getLong(3);
		Currency currency = calculation.getCurrencyById(cursor.getLong(4));
		expense.setCurrency(currency);
		expense.setAmount((double)fixedAmount / currency.getDecimalFactor());

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(cursor.getLong(5));
		expense.setDate(cal);

		Map<Long, Double> weights = null;

		Cursor weightsCursor = getDatabase().query(
				DataBaseHelper.TABLE_SPLIT_WEIGHTS,
				new String[] { DataBaseHelper.COLUMN_PERSON_ID, DataBaseHelper.COLUMN_WEIGHT },
				DataBaseHelper.COLUMN_EXPENSE_ID + " = ?", new String[] { Long.toString(id) },
				null, null, null, null);

		weightsCursor.moveToFirst();
		if (!weightsCursor.isAfterLast()) {
			weights = new HashMap<>();
			do {
				weights.put(weightsCursor.getLong(0), weightsCursor.getDouble(1));
			} while (weightsCursor.moveToNext());
		}
		weightsCursor.close();

		expense.setSplitWeights(weights);
		return expense;
	}

	private void insertWeights(Expense expense) {
		Map<Long, Double> weights = expense.getSplitWeights();
		if (weights != null) {
			for (Map.Entry<Long, Double> entry : weights.entrySet()) {
				ContentValues values = new ContentValues();
				values.put(DataBaseHelper.COLUMN_EXPENSE_ID, expense.getId());
				values.put(DataBaseHelper.COLUMN_PERSON_ID, entry.getKey());
				values.put(DataBaseHelper.COLUMN_WEIGHT, entry.getValue());

				getDatabase().insert(
						DataBaseHelper.TABLE_SPLIT_WEIGHTS,
						null,
						values);
			}
		}		
	}

	private void deleteWeights(long id) {
		getDatabase().delete(
				DataBaseHelper.TABLE_SPLIT_WEIGHTS,
				DataBaseHelper.COLUMN_EXPENSE_ID + "= ?", new String[] { Long.toString(id) });
	}

	@Override
	public long insert(Expense expense) {
		long insertId = super.insert(expense);
		insertWeights(expense);
		return insertId;
	}

	@Override
	public void update(Expense expense) {
		super.update(expense);
		deleteWeights(expense.getId());
		insertWeights(expense);
	}

	@Override
	public void delete(long id) {
		deleteWeights(id);
		super.delete(id);
	}

	public Cursor listByPerson(long personId) {
		return getDatabase().query(
				DataBaseHelper.TABLE_EXPENSES, COLUMNS,
				DataBaseHelper.COLUMN_PERSON_ID + " = ?", new String[] { Long.toString(personId) },
				null, null, DataBaseHelper.COLUMN_DATE);
	}

}
