package com.example.android.splitwise.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by siddharthverma on 4/14/17.
 */

public class Calculation_Friend extends DataObject {

    private String name;
    private String phone;
    private String amount;

    public Calculation_Friend(String name, String phone, String amount) {
        this.name = name;
        this.phone=phone;
        this.amount=amount;
    }

    public String getPhone() {
        return phone;
    }

    public String getTitle() {
        return name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
