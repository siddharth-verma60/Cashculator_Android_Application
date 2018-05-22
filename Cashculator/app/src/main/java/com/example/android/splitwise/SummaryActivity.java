
package com.example.android.splitwise;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.android.splitwise.dao.CalculationDataSource;
import com.example.android.splitwise.dao.DataBaseHelper;
import com.example.android.splitwise.data.Calculation;
import com.example.android.splitwise.data.Expense;
import com.example.android.splitwise.data.Person;

import java.text.DateFormat;
import java.util.List;



public class SummaryActivity extends Activity {

	public static final String PARAM_CALCULATION_ID = "calculationId";

	private final DataBaseHelper dbHelper = new DataBaseHelper(this);
	private final CalculationDataSource calculationDataSource = new CalculationDataSource(dbHelper);
	private CurrencyHelper currencyHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.summary);

		Intent intent = getIntent();
		long calculationId = intent.getLongExtra(PARAM_CALCULATION_ID, -1);
		Calculation calculation = calculationDataSource.get(calculationId);
		currencyHelper = calculation.getMainCurrency().getCurrencyHelper();
		List<Person> persons = calculation.getPersons();
		List<Expense> expenses = calculation.getExpenses();

		if (expenses.isEmpty()) {
			TextView noExpensesView = (TextView) findViewById(R.id.no_expenses);
			noExpensesView.setVisibility(View.VISIBLE);
			TableLayout summaryTable = (TableLayout) findViewById(R.id.summary_table);
			summaryTable.setVisibility(View.GONE);
		} else {
			setSummary(calculation);
		}

		double[] totalExpenses = new double[persons.size()];
		double[] totalConsumption = new double[persons.size()];

		for (Expense expense : expenses) {
			List<Double> shares = expense.getExchangedShares(persons);
			for (int i = 0; i < persons.size(); i++) {
				if (persons.get(i).equals(expense.getPerson()))
					totalExpenses[i] += expense.getExchangedAmount();
				totalConsumption[i] += shares.get(i);
			}
		}

		setTitle(calculation.getTitle());

		TableLayout table = (TableLayout) findViewById(R.id.results_table);
		LayoutInflater inflater = getLayoutInflater();

		for (int i = 0; i < persons.size(); i++) {
			Person person = persons.get(i);

			TableRow row = (TableRow) inflater.inflate(R.layout.summary_row, table, false);
			table.addView(row);
			TextView nameView = (TextView) row.findViewById(R.id.name);
			TextView sumExpenses = (TextView) row.findViewById(R.id.sum_expenses);
			TextView sumConsumption = (TextView) row.findViewById(R.id.sum_consumption);
			TextView resultView = (TextView) row.findViewById(R.id.result);

			nameView.setText(person.getName() + ":");
			sumExpenses.setText(currencyHelper.format(totalExpenses[i]));
			sumConsumption.setText(currencyHelper.format(totalConsumption[i]));

			double result = totalExpenses[i] - totalConsumption[i];
			int color = getResources().getColor(result >= 0 ? R.color.result_positive : R.color.result_negative);
			resultView.setText(currencyHelper.format(result));
			resultView.setTextColor(color);
		}
	}

	private void setSummary(Calculation calculation) {
		TextView firstDateView = (TextView) findViewById(R.id.first_date);
		TextView lastDateView = (TextView) findViewById(R.id.last_date);
		TextView durationView = (TextView) findViewById(R.id.duration);
		TextView numExpensesView = (TextView) findViewById(R.id.num_expenses);
		TextView totalAmountView = (TextView) findViewById(R.id.total_amount);

		DateFormat format = DateFormat.getDateInstance();
		firstDateView.setText(format.format(calculation.getFirstDate().getTime()));
		lastDateView.setText(format.format(calculation.getLastDate().getTime()));

		long duration = calculation.getDuration();
		String daysFormat = getResources().getString(duration == 1 ? R.string.day_format : R.string.days_format);
		durationView.setText(String.format(daysFormat, calculation.getDuration()));

		double totalAmount = 0;
		List<Expense> expenses = calculation.getExpenses();
		for (Expense expense : expenses)
			totalAmount += expense.getExchangedAmount();

		numExpensesView.setText(Integer.toString(expenses.size()));
		totalAmountView.setText(currencyHelper.format(totalAmount));
	}

}
