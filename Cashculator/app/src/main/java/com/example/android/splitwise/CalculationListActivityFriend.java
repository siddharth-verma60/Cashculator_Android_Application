package com.example.android.splitwise;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.view.WindowCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.splitwise.dao.CalculationDataSourceFriend;
import com.example.android.splitwise.dao.DataBaseHelper;
import com.example.android.splitwise.dao.DataBaseHelperFriend;
import com.example.android.splitwise.data.Calculation;
import com.example.android.splitwise.data.Calculation_Friend;
import com.example.android.splitwise.data.Expense;
import com.example.android.splitwise.data.Person;

import static android.R.attr.x;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.example.android.splitwise.R.mipmap.calculation;
import static com.example.android.splitwise.R.mipmap.expense;
import static com.example.android.splitwise.R.mipmap.person;


public class CalculationListActivityFriend extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private final DataBaseHelperFriend dbHelper = new DataBaseHelperFriend(this);
    private final CalculationDataSourceFriend dataSource = new CalculationDataSourceFriend(dbHelper);
    Calculation_Friend calculation;
    private Cursor cursor;
    private String name;
    private String phone;

    private ListView listView;
    private CalculationAdapter adapter;

    private static final int ITEM_DELETE = 0;

    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ListView mDrawerList;
    private static final int CONTACT_PICKER_RESULT = 1001;
    private static final String DEBUG_TAG = "Contact List";
    private static final int RESULT_OK = -1;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Calculation_Friend cal=adapter.get(position);
        //Toast.makeText(this, cal.getTitle()+"", Toast.LENGTH_SHORT).show();
        Calculation_Friend cal=dataSource.get(adapter.get(position));
        //Toast.makeText(this, cal.getTitle()+"", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, CalculationEditorActivityFriend.class);
        intent.putExtra("cal_id", adapter.get(position));
        //intent.putExtra(ExpenseEditorActivity.PARAM_EXPENSE_ID, expense.getId());
        startActivity(intent);
    }


    private class CalculationAdapter extends CursorAdapter {

        CalculationAdapter(Context context) {
            super(context, null, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // TODO: Use view holder
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.calculation_list_friend_row, parent, false);
        }


        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }

        public long get(int position) {
            Cursor cursor = getCursor();
            //Calculation_Friend cal=null;
            long cal_id=0;


            if(cursor.moveToPosition(position)) {
                cal_id = cursor.getLong(cursor.getColumnIndex(dbHelper.COLUMN_ID));
            }
            //cursor.close();
            return cal_id;

        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Calculation_Friend calculation = dataSource.fromCursor(cursor);

            TextView titleView = (TextView) view.findViewById(R.id.calculation_title_friend);
            titleView.setText(calculation.getTitle());

            TextView phoneView = (TextView) view.findViewById(R.id.phone_friend);
            phoneView.setText(calculation.getPhone());

            TextView amountView = (TextView) view.findViewById(R.id.calculation_amount_friend);
            amountView.setText(calculation.getAmount());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);

        setContentView(R.layout.calculation_list_friend);
        setTitle("Friends");

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



        Button buttonX = (Button)findViewById(R.id.new_button_friend);

// Register the onClick listener with the implementation above
        buttonX.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                openContact();
            }
        });

        listView = (ListView) findViewById(R.id.cal_list_friend);
        adapter = new CalculationAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);

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

    private void openContact() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CONTACT_PICKER_RESULT);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER_RESULT:
                    Uri contactUri = data.getData();
                    // yahan pe we have to add this to the database
                    String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER};

                    Cursor people = getContentResolver().query(contactUri, null, null, null, null);
                    people.moveToFirst();
                    int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    name   = people.getString(indexName);
                    phone = people.getString(indexNumber);

                    DataBaseHelperFriend dbHelper = new DataBaseHelperFriend(this);
                    CalculationDataSourceFriend dataSource = new CalculationDataSourceFriend(dbHelper);
                    calculation = dataSource.createCalculation(name, phone, "0");
                    dbHelper.close();

                    Log.w(DEBUG_TAG, "Warning: activity result is ok!");
                    break;
            }
        } else {

            Log.w(DEBUG_TAG, "Warning: activity result not ok");
        }
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
//        startActivity(intent);
    }

    private void refresh() {
        cursor = dataSource.listAll();
        adapter.changeCursor(cursor);
        listView.setAdapter(adapter);
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
            Intent cal_intent=new Intent(CalculationListActivityFriend.this, CalculationEditorActivityFriend.class);
            startActivity(cal_intent);
            return true;
        }
        else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.cal_list_friend) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            cursor.moveToPosition(info.position);
            Calculation_Friend calculation = dataSource.fromCursor(cursor);
            menu.setHeaderTitle(calculation.getTitle());
            menu.add(0, ITEM_DELETE, 0, R.string.menu_delete);
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
        }else {
            return false;
        }
    }

    private void confirmAndDelete(final Calculation_Friend calculation) {
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


