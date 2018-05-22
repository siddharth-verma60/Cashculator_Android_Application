package com.example.android.splitwise;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

public class CurrencyHelper {

	private final NumberFormat currencyFormat;
	private final NumberFormat plainFormat;

	public CurrencyHelper(Currency currency, Locale locale) {
		currencyFormat = NumberFormat.getCurrencyInstance(locale);
		plainFormat = NumberFormat.getNumberInstance(locale);

		currencyFormat.setCurrency(currency);
		currencyFormat.setMinimumFractionDigits(currency.getDefaultFractionDigits());
		currencyFormat.setMaximumFractionDigits(currency.getDefaultFractionDigits());

		plainFormat.setMinimumFractionDigits(currency.getDefaultFractionDigits());
		plainFormat.setMaximumFractionDigits(currency.getDefaultFractionDigits());
	}

	public CurrencyHelper(Currency currency) {
		this(currency, Locale.getDefault());
	}

	public void setGroupingUsed(boolean value) {
		currencyFormat.setGroupingUsed(value);
		plainFormat.setGroupingUsed(value);
	}

	public String format(double value, boolean withSymbol) {
		NumberFormat format = withSymbol ? currencyFormat : plainFormat;
		return format.format(value);	
	}

	public String format(double value) {
		return format(value, true);
	}

	public double parse(String amountString) throws ParseException {
		Number amount = plainFormat.parse(amountString);
		return amount.floatValue();
	}

}
