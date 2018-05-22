
package com.example.android.splitwise.data;

public class Person extends DataObject {

	private final Calculation calculation;
	private String name = "";

	public Person(Calculation calculation) {
		this.calculation = calculation;
	}

	public Calculation getCalculation() {
		return calculation;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

}
