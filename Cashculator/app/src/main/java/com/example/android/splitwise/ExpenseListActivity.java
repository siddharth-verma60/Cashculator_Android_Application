
package com.example.android.splitwise;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.splitwise.dao.CalculationDataSource;
import com.example.android.splitwise.dao.DataBaseHelper;
import com.example.android.splitwise.dao.ExpenseDataSource;
import com.example.android.splitwise.data.Calculation;
import com.example.android.splitwise.data.Currency;
import com.example.android.splitwise.data.Expense;
import com.example.android.splitwise.data.Person;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class ExpenseListActivity extends AppCompatActivity implements OnChildClickListener {

	public static final String PARAM_CALCULATION_ID = "calculationId";

	private long calculationId;

	private final DataBaseHelper dbHelper = new DataBaseHelper(this);
	private final CalculationDataSource calculationDataSource = new CalculationDataSource(dbHelper);
	private ExpenseDataSource expenseDataSource;

	private static String TAG = "PermissionDemo";
	private static final int REQUEST_WRITE_STORAGE = 112;

	private static final int ITEM_DELETE = 0;

	private ExpenseAdapter adapter;

	private class ExpenseAdapter extends BaseExpandableListAdapter {

		private boolean groupByPerson = true;

		private Calculation calculation;


		private final Map<Person, List<Expense>> expensesByPerson = new HashMap<>();

		private final Set<Calendar> dates = new TreeSet<>();
		private final Map<Calendar, List<Expense>> expensesByDate = new HashMap<>();

		private final LayoutInflater inflater;
		private final String groupSummaryFormat = getResources().getString(R.string.expenses_summary_format);

		public ExpenseAdapter(Context context) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setCalculation(Calculation calculation) {
			this.calculation = calculation;
			expenseDataSource = new ExpenseDataSource(dbHelper, calculation);

			expensesByPerson.clear();
			dates.clear();
			expensesByDate.clear();

			for (Person person : calculation.getPersons())
				expensesByPerson.put(person, new ArrayList<Expense>());

			for (Expense expense : calculation.getExpenses()) {
				List<Expense> byPersonList = expensesByPerson.get(expense.getPerson());
				byPersonList.add(expense);

				Calendar date = expense.getDate();
				List<Expense> byDateList = expensesByDate.get(date);
				if (byDateList == null) {
					byDateList = new ArrayList<>();
					expensesByDate.put(date, byDateList);
					dates.add(date);
				}
				byDateList.add(expense);
			}

			notifyDataSetChanged();
		}

		public void setGroupByPerson(boolean groupByPerson) {
			this.groupByPerson = groupByPerson;
			notifyDataSetChanged();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public int getGroupCount() {
			if (calculation == null)
				return 0;
			else if (groupByPerson)
				return calculation.getPersons().size();
			else
				return dates != null ? dates.size() : 0;
		}

		@Override
		public long getGroupId(int groupPosition) {
			if (groupByPerson) {
				Person person = (Person) getGroup(groupPosition);
				return person.getId();
			} else {
				Calendar date = (Calendar) getGroup(groupPosition);
				return date.getTimeInMillis();
			}
		}

		@Override
		public Object getGroup(int groupPosition) {
			if (groupByPerson) {
				return calculation.getPersons().get(groupPosition);
			} else {
				return dates.toArray()[groupPosition];
			}
		}

		private class GroupViewHolder {
			public TextView nameView;
			public TextView summaryView;
			public ImageView addButton;
		}

		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View view = convertView;
			GroupViewHolder holder;

			if (view == null) {
				view = inflater.inflate(R.layout.expense_list_group_row, parent, false);
				holder = new GroupViewHolder();
				holder.nameView = (TextView) view.findViewById(android.R.id.text1);
				holder.summaryView = (TextView) view.findViewById(android.R.id.text2);
				holder.addButton = (ImageView) view.findViewById(R.id.add_button);
				view.setTag(holder);
			} else {
				holder = (GroupViewHolder) view.getTag();
			}

			List<Expense> expenses;

			if (groupByPerson) {
				Person person = (Person) getGroup(groupPosition);
				holder.nameView.setText(person.getName());
				expenses = expensesByPerson.get(person);
			} else {
				Calendar date = (Calendar) getGroup(groupPosition);
				DateFormat format = DateFormat.getDateInstance();
				holder.nameView.setText(format.format(date.getTime()));
				expenses = expensesByDate.get(date);
			}

			int count = 0;
			double total = 0;
			for (Expense expense : expenses) {
				count++;
				total += expense.getExchangedAmount();
			}

			if (count == 0) {
				holder.summaryView.setText(R.string.no_expenses);
			} else {
				String totalStr = calculation.getMainCurrency().getCurrencyHelper().format(total);
				String summary = String.format(groupSummaryFormat, count, totalStr);
				holder.summaryView.setText(summary);
			}

			holder.addButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (groupByPerson) {
						Person person = (Person) getGroup(groupPosition);
						addExpenseForPerson(person.getId());
					} else {
						Calendar date = (Calendar) getGroup(groupPosition);
						addExpenseForDate(date);
					}
				}
			});

			return view;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			if (groupByPerson) {
				Person person = (Person) getGroup(groupPosition);
				List<Expense> list = expensesByPerson.get(person);
				return list.size();
			} else {
				Calendar date = (Calendar) getGroup(groupPosition);
				List<Expense> list = expensesByDate.get(date);
				return list.size();
			}
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			Expense expense = (Expense) getChild(groupPosition, childPosition);
			return expense.getId();
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			if (groupByPerson) {
				Person person = (Person) getGroup(groupPosition);
				List<Expense> list = expensesByPerson.get(person);
				return list.get(childPosition);
			} else {
				Calendar date = (Calendar) getGroup(groupPosition);
				List<Expense> list = expensesByDate.get(date);
				return list.get(childPosition);
			}
		}

		private class ChildViewHolder {
			public TextView titleView;
			public TextView amountView;
			public TextView exchangedView;
			public TextView details1View;
			public TextView details2View;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View view = convertView;
			ChildViewHolder holder;

			if (view == null) {
				view = inflater.inflate(R.layout.expense_child_row, parent, false);
				holder = new ChildViewHolder();
				holder.titleView = (TextView) view.findViewById(R.id.expense_title);
				holder.amountView = (TextView) view.findViewById(R.id.expense_amount);
				holder.exchangedView = (TextView) view.findViewById(R.id.expense_exchanged_amount);
				holder.details1View = (TextView) view.findViewById(R.id.expense_details_1);
				holder.details2View = (TextView) view.findViewById(R.id.expense_details_2);
				view.setTag(holder);
			} else {
				holder = (ChildViewHolder) view.getTag();
			}

			Expense expense = (Expense) getChild(groupPosition, childPosition);
			Currency currency = expense.getCurrency();
			CurrencyHelper currencyHelper = currency.getCurrencyHelper();

			holder.titleView.setText(expense.getTitle());
			holder.amountView.setText(currencyHelper.format(expense.getAmount()));
			if (expense.getCurrency().equals(calculation.getMainCurrency())) {
				holder.exchangedView.setVisibility(View.GONE);
			} else {
				CurrencyHelper mainCurrencyHelper = calculation.getMainCurrency().getCurrencyHelper();
				double exchanged = expense.getExchangedAmount();
				holder.exchangedView.setVisibility(View.VISIBLE);
				holder.exchangedView.setText(mainCurrencyHelper.format(exchanged));
			}

			if (groupByPerson) {
				DateFormat format = DateFormat.getDateInstance();
				holder.details1View.setText(format.format(expense.getDate().getTime()));
			} else {
				Person person = expense.getPerson();
				holder.details1View.setText(person.getName());
			}

			if (expense.isUnevenSplit()) {
				List<Person> persons = calculation.getPersons();
				List<Double> shares = expense.getShares(persons);
				StringBuilder msg = new StringBuilder();
				for (int i = 0; i < persons.size(); i++) {
					if (shares.get(i) > 0) {
						Person person = persons.get(i);
						if (msg.length() > 0)
							msg.append("; ");
						String shareStr = currencyHelper.format(shares.get(i));
						msg.append(String.format("%s: %s", person.getName(), shareStr));
					}
				}

				holder.details2View.setVisibility(View.VISIBLE);
				holder.details2View.setText(msg);
			} else {
				holder.details2View.setVisibility(View.GONE);
			}
			return view;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);

		setContentView(R.layout.expense_list_group);

		Intent intent = getIntent();
		calculationId = intent.getLongExtra(PARAM_CALCULATION_ID, -1);

		ExpandableListView listView = (ExpandableListView) findViewById(R.id.expense_list);
		adapter = new ExpenseAdapter(this);
		listView.setAdapter(adapter);
		listView.setOnChildClickListener(this);
		registerForContextMenu(listView);
		//setContentView(listView);

		refresh();
	}

	private void refresh() {
		Calculation calculation = calculationDataSource.get(calculationId);
		setTitle(calculation.getTitle());
		adapter.setCalculation(calculation);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.expense_list_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
			case R.id.new_expense:
				addExpense();
			case R.id.group_by_person:
				adapter.setGroupByPerson(true);
				return true;
			case R.id.group_by_date:
				adapter.setGroupByPerson(false);
				return true;
			case R.id.manage_currencies:
				intent = new Intent(this, ManageCurrenciesActivity.class);
				intent.putExtra(ManageCurrenciesActivity.PARAM_CALCULATION_ID, calculationId);
				startActivity(intent);
				return true;
			case R.id.calcluation_summary:
				intent = new Intent(this, SummaryActivity.class);
				intent.putExtra(SummaryActivity.PARAM_CALCULATION_ID, calculationId);
				startActivity(intent);
				return true;
			case R.id.export_calculation:
				checkPermission();
				CsvExporter.export(calculationDataSource.get(calculationId), this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void checkPermission(){
		//ask for the permission in android M
		int permission = ContextCompat.checkSelfPermission(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			Log.i(TAG, "Permission to record denied");

			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Permission to access the SD-CARD is required for this app to Download PDF.")
						.setTitle("Permission required");

				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						Log.i(TAG, "Clicked");
						makeRequest();
					}
				});

				AlertDialog dialog = builder.create();
				dialog.show();

			} else {
				makeRequest();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case REQUEST_WRITE_STORAGE: {

				if (grantResults.length == 0
						|| grantResults[0] !=
						PackageManager.PERMISSION_GRANTED) {

					Log.i(TAG, "Permission has been denied by user");

				} else {

					Log.i(TAG, "Permission has been granted by user");

				}
				return;
			}
		}
	}

	protected void makeRequest() {
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
				REQUEST_WRITE_STORAGE);
	}


	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		Expense expense = (Expense) adapter.getChild(groupPosition, childPosition);
		Intent intent = new Intent(this, ExpenseEditorActivity.class);
		intent.putExtra(ExpenseEditorActivity.PARAM_CALCULATION_ID, expense.getCalculation().getId());
		intent.putExtra(ExpenseEditorActivity.PARAM_EXPENSE_ID, expense.getId());
		startActivity(intent);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.expense_list) {
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
			int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
			if (child != -1) {
				Expense expense = (Expense) adapter.getChild(group, child);
				menu.setHeaderTitle(expense.getTitle());
				menu.add(0, ITEM_DELETE, 0, R.string.menu_delete);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == ITEM_DELETE) {
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
			int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
			if (child != -1) {
				Expense expense = (Expense) adapter.getChild(group, child);
				expenseDataSource.delete(expense.getId());
				refresh();
			}
		} else {
			return false;
		}
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		dbHelper.close();
	}

	@Override
	protected void onResume() {
		refresh();
		super.onResume();
	}

	private void addExpenseForPerson(long personId) {
		Intent intent = new Intent(this, ExpenseEditorActivity.class);
		intent.putExtra(ExpenseEditorActivity.PARAM_CALCULATION_ID, calculationId);
		intent.putExtra(ExpenseEditorActivity.PARAM_PERSON_ID, personId);
		startActivity(intent);
	}

	private void addExpenseForDate(Calendar date) {
		Intent intent = new Intent(this, ExpenseEditorActivity.class);
		intent.putExtra(ExpenseEditorActivity.PARAM_CALCULATION_ID, calculationId);
		intent.putExtra(ExpenseEditorActivity.PARAM_DATE, date.getTimeInMillis());
		startActivity(intent);
	}

	private void addExpense() {
		Intent intent = new Intent(this, ExpenseEditorActivity.class);
		intent.putExtra(ExpenseEditorActivity.PARAM_CALCULATION_ID, calculationId);
		startActivity(intent);
	}

}
