package com.example.android.splitwise.dao;

import android.content.ContentValues;
import android.database.Cursor;


import com.example.android.splitwise.data.Calculation_Friend;

/**
 * Created by siddharthverma on 4/14/17.
 */

public class CalculationDataSourceFriend extends AbstractDataSourceFriend<Calculation_Friend> {

    private final DataBaseHelperFriend dbHelper;


    private static final String[] COLUMNS = {
            DataBaseHelper.COLUMN_ID,
            DataBaseHelper.COLUMN_TITLE,
            DataBaseHelper.COLUMN_AMOUNT,
            DataBaseHelper.COLUMN_PHONE
    };

    public CalculationDataSourceFriend(DataBaseHelperFriend dbHelper) {
        super(dbHelper, DataBaseHelperFriend.TABLE_CALCULATIONS, COLUMNS);
        this.dbHelper = dbHelper;
    }

    @Override
    protected ContentValues toContentValues(Calculation_Friend calculation) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelperFriend.COLUMN_TITLE, calculation.getTitle());
        values.put(DataBaseHelperFriend.COLUMN_PHONE, calculation.getPhone());
        values.put(DataBaseHelperFriend.COLUMN_AMOUNT, calculation.getAmount());
        return values;
    }

    @Override
    public Calculation_Friend fromCursor(Cursor cursor) {
        long calculationId = cursor.getLong(0);
        String name = cursor.getString(1);
        String phone =cursor.getString(3);
        String amount = cursor.getString(2);

        Calculation_Friend calculation = new Calculation_Friend(name, phone, amount);
        calculation.setId(calculationId);

        return calculation;
    }

    @Override
    public void delete(long id) {
        super.delete(id);
    }

    @Override
    public void update(Calculation_Friend calculation) {
        super.update(calculation);
    }



    public Calculation_Friend createCalculation(String title, String phone, String amount) {
        Calculation_Friend calculation = new Calculation_Friend(title, phone, amount);
        insert(calculation);
        return get(calculation.getId());
    }


}
