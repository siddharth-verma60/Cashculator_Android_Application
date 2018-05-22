package com.example.android.splitwise;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.splitwise.dao.CalculationDataSource;
import com.example.android.splitwise.dao.CalculationDataSourceFriend;
import com.example.android.splitwise.dao.DataBaseHelper;
import com.example.android.splitwise.dao.DataBaseHelperFriend;
import com.example.android.splitwise.data.Calculation;
import com.example.android.splitwise.data.Calculation_Friend;
import com.example.android.splitwise.data.Expense;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.example.android.splitwise.ExpenseEditorActivity.PARAM_DATE;
import static com.example.android.splitwise.ExpenseEditorActivity.PARAM_PERSON_ID;
import static com.example.android.splitwise.R.mipmap.calculation;
import static com.example.android.splitwise.R.mipmap.expense;


public class CalculationEditorActivityFriend extends AppCompatActivity {

	private EditText titleField;
	private static final String TAG = "MainActivity";

	private EditText phoneField;
	private EditText amountField;

	private final DataBaseHelperFriend dbHelper = new DataBaseHelperFriend(this);
	Calculation_Friend calculation;
	CalculationDataSourceFriend dataSource = new CalculationDataSourceFriend(dbHelper);;
	private enum  Mode { NEW_EXPENSE, EDIT_EXPENSE }
	private Mode mode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);


		setContentView(R.layout.calculation_editor_friend);
		setTitle("Add Friend");
		Log.v("yahoooo", "uoooooo");
		Intent intent=getIntent();
		long cal_id=intent.getLongExtra("cal_id",-1);
		//calculation=dataSource.get(cal_id);
		//Toast.makeText(this, calculation.getTitle()+"", Toast.LENGTH_SHORT).show();
		//cal_id=-2;

		titleField = (EditText) findViewById(R.id.calculation_editor_title_friend);
		phoneField = (EditText) findViewById(R.id.calculation_editor_phone);
		amountField = (EditText) findViewById(R.id.calculation_editor_amount);

		//calculation=dataSource.get(cal_id);

		mode = (cal_id >= 0 ? Mode.EDIT_EXPENSE : Mode.NEW_EXPENSE);
		if (mode == Mode.EDIT_EXPENSE) {
			calculation=dataSource.get(cal_id);
			titleField.setText(calculation.getTitle());
			phoneField.setText(calculation.getPhone());
			amountField.setText(calculation.getAmount());
			amountField.requestFocus();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//outState.putStringArrayList("personNames", getPersonNames());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calculation_editor_options, menu);
		return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_save:
    			doSave();
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }


	private String getCalculationTitle() {
		return titleField.getText().toString().trim();
	}
	private String getCalculationPhone() {
		return phoneField.getText().toString().trim();
	}
	private String getCalculationAmount() {
		return amountField.getText().toString().trim();
	}


	private boolean validate() {
		final Resources res = getResources();

		boolean valid = true;
		titleField.setError(null);
		phoneField.setError(null);
		amountField.setError(null);


		if (getCalculationTitle().length() == 0) {
			final String errRequired = res.getString(R.string.validate_required);
			titleField.setError(errRequired);
			valid = false;
		}
		if (getCalculationPhone().length() == 0) {
			final String errRequired = res.getString(R.string.validate_required);
			phoneField.setError(errRequired);
			valid = false;
		}
		if (getCalculationAmount().length() == 0) {
			final String errRequired = res.getString(R.string.validate_required);
			amountField.setError(errRequired);
			valid = false;
		}

		return valid;
	}

	private void save() {

		if(mode==Mode.EDIT_EXPENSE){
			calculation.setAmount(getCalculationAmount());
			dataSource.update(calculation);
		}
		else {
//			DataBaseHelperFriend dbHelper = new DataBaseHelperFriend(this);
//			dataSource = new CalculationDataSourceFriend(dbHelper);
			calculation = dataSource.createCalculation(getCalculationTitle(), getCalculationPhone(), getCalculationAmount());
			dbHelper.close();
		}

//		Intent intent = new Intent(this, ExpenseListActivity.class);
//		intent.putExtra(ExpenseListActivity.PARAM_CALCULATION_ID, calculation.getId());
//		startActivity(intent);
		finish();
	}

	private void doSave() {
		try {
			if (validate()) save();
		} catch (Exception e) {
			Toast.makeText(this, "Error saving calculation", Toast.LENGTH_LONG).show();
			Log.e(CalculationEditorActivityFriend.class.toString(), "Error saving calculation", e);
		}
	}

}
