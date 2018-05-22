
package com.example.android.splitwise.data;

public class Friend extends DataObject {

    private final Calculation_Friend calculation;
    private String name = "";
    private String phone= "";

    public Friend(Calculation_Friend calculation) {
        this.calculation = calculation;
    }

    public Calculation_Friend getCalculation() {
        return calculation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return getName();
    }

}
