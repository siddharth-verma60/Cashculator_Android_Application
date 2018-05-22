package com.example.android.splitwise;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.splitwise.dao.*;
import com.example.android.splitwise.data.Calculation;
import com.example.android.splitwise.data.Expense;
import com.example.android.splitwise.data.Person;

import java.text.DateFormat;
import java.util.List;

public class CalculationListActivityGroup extends AppCompatActivity implements OnItemClickListener {

	private static final String TAG = "CalctivityGroup";
	private final DataBaseHelper dbHelper = new DataBaseHelper(this);
	private final CalculationDataSource dataSource = new CalculationDataSource(dbHelper);
	private Cursor cursor;

	private ListView listView;
	private CalculationAdapter adapter;

	private static final int ITEM_DELETE = 0;
	private static final int ITEM_SUMMARY = 1;

    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
    private ListView mDrawerList;


    private class CalculationAdapter extends CursorAdapter {

		private final String summaryFormat = getResources().getString(R.string.expenses_summary_format);
		private final String dateRangeFormat = getResources().getString(R.string.date_range_format);

		CalculationAdapter(Context context) {
			super(context, null, 0);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO: Use view holder
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return inflater.inflate(R.layout.calculation_list_group_row, parent, false);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			Calculation calculation = dataSource.fromCursor(cursor);

			StringBuilder personNames = new StringBuilder();
			for (Person person : calculation.getPersons()) {
				if (personNames.length() > 0) {
					personNames.append(", ");
				}
				personNames.append(person.getName());
			}

			TextView titleView = (TextView) view.findViewById(R.id.calculation_title);
			titleView.setText(calculation.getTitle());
			TextView personsView = (TextView) view.findViewById(R.id.calculation_persons);
			personsView.setText(personNames);

			TextView datesView = (TextView) view.findViewById(R.id.calculation_dates);
			TextView summaryView = (TextView) view.findViewById(R.id.calculation_summary);

			List<Expense> expenses = calculation.getExpenses();
			if (expenses.size() == 0) {
				datesView.setVisibility(View.GONE);
				summaryView.setText(R.string.no_expenses);
			} else {
				DateFormat format = DateFormat.getDateInstance();
				String firstDate = format.format(calculation.getFirstDate().getTime());
				String lastDate = format.format(calculation.getLastDate().getTime());
				datesView.setText(String.format(dateRangeFormat, firstDate, lastDate));
				datesView.setVisibility(View.VISIBLE);

				int count = calculation.getExpenses().size();
				CurrencyHelper helper = calculation.getMainCurrency().getCurrencyHelper();
				String total = helper.format(calculation.getExpenseTotal());
				summaryView.setText(String.format(summaryFormat, count, total));
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	//	supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);

		setContentView(R.layout.calculation_list_group);
		setTitle("Events/Apartments: ");

        String[] arr={"Events/Apartments", "Friends", "Logout", "ContactUs"};
        mPlanetTitles = arr;

		mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.string.drawer_open,  /* "open drawer" description */
				R.string.drawer_close  /* "close drawer" description */
		) {

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getSupportActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.addDrawerListener(mDrawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);



		// Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());



        Button buttonX = (Button)findViewById(R.id.new_groups_button);

// Register the onClick listener with the implementation above
        buttonX.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
//                Toast toast=Toast.makeText(getApplicationContext(),"Message",Toast.LENGTH_SHORT);
//                toast.show();
                Intent new_intent=new Intent(CalculationListActivityGroup.this, CalculationEditorActivity.class);
				new_intent.putExtra("Event_type","Trip/Apartment: ");
                startActivity(new_intent);
            }
        });

		listView = (ListView) findViewById(R.id.cal_list);
		adapter = new CalculationAdapter(this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		registerForContextMenu(listView);

//        if(listView.getParent()!=null)
//            ((LinearLayout)listView.getParent()).removeView(listView); // <- fix

//		setContentView(R.layout.calculation_list_group);
		refresh();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    public void selectItem(int position) {
        Intent intent = null;
        switch(position) {

            case 0:
                intent = new Intent(this, CalculationListActivityGroup.class);
                startActivity(intent);
                break;
            case 1:
                intent = new Intent(this, CalculationListActivityFriend.class);
				startActivity(intent);
                break;
			case 2:
				intent = new Intent(this, HomeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			case 3:
				intent = new Intent(this, ContactActivity.class);
				startActivity(intent);
				break;

        }
       // startActivity(intent);
    }

	private void refresh() {
		cursor = dataSource.listAll();
		adapter.changeCursor(cursor);
		listView.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
		cursor.moveToPosition(position);
		Calculation calculation = dataSource.fromCursor(cursor);
		Intent intent = new Intent(this, ExpenseListActivity.class);
		intent.putExtra(ExpenseListActivity.PARAM_CALCULATION_ID, calculation.getId());
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calculation_list_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		if(item.getItemId()==R.id.about) {
                showAboutDialog();
                return true;
            }
            else if(item.getItemId()==R.id.new_calculation){
                Intent cal_intent=new Intent(CalculationListActivityGroup.this, CalculationEditorActivity.class);
				cal_intent.putExtra("Event_type","Trip/Apartment: ");
                startActivity(cal_intent);
                return true;
        }
            else{
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.cal_list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			cursor.moveToPosition(info.position);
			Calculation calculation = dataSource.fromCursor(cursor);
			menu.setHeaderTitle(calculation.getTitle());
			menu.add(0, ITEM_DELETE, 0, R.string.menu_delete);
			menu.add(0, ITEM_SUMMARY, 0, R.string.calculation_summary);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		cursor.moveToPosition(info.position);
		long calculationId = cursor.getLong(0);

		if (item.getItemId() == ITEM_DELETE) {
			confirmAndDelete(dataSource.get(calculationId));
			return true;
		} else if (item.getItemId() == ITEM_SUMMARY) {
			Intent intent = new Intent(this, SummaryActivity.class);
			intent.putExtra(ExpenseListActivity.PARAM_CALCULATION_ID, calculationId);
			startActivity(intent);
		} else {
			return false;
		}
		return true;
	}

	private void confirmAndDelete(final Calculation calculation) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(android.R.drawable.ic_delete);
		dialog.setTitle(calculation.getTitle());
		dialog.setMessage(R.string.confirm_delete_calculation);
		dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dataSource.delete(calculation.getId());
				refresh();
			}
		});
		dialog.setNegativeButton(android.R.string.no, null);
		dialog.show();
	}

	private void showAboutDialog() {
		AboutDialog about = new AboutDialog(this);
		about.show();
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

}
