package com.example.android.splitwise;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by siddharthverma on 3/30/17.
 */

public class  BannerActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.banner_page);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra("user_name");

        // Capture the layout's TextView and set the string as its text
        TextView textView = (TextView) findViewById(R.id.name_banner);
        textView.setText(message+" !");

        final Button button_choose_event = (Button) findViewById(R.id.choose_event);
        button_choose_event.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent activityChangeIntent = new Intent(BannerActivity.this, CalculationEditorActivity.class);
                activityChangeIntent.putExtra("Event_type", "Event/Trip Title:");
                BannerActivity.this.startActivity(activityChangeIntent);
            }
        });

        final Button button_choose_apartment = (Button) findViewById(R.id.choose_apartment);
        button_choose_apartment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent activityChangeIntent = new Intent(BannerActivity.this, CalculationEditorActivity.class);
                activityChangeIntent.putExtra("Event_type", "Apartment Name:");
                BannerActivity.this.startActivity(activityChangeIntent);
            }
        });

        final Button button_home = (Button) findViewById(R.id.home);
        button_home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent activityChangeIntent = new Intent(BannerActivity.this, CalculationListActivityGroup.class);
                BannerActivity.this.startActivity(activityChangeIntent);
            }
        });


    }

}
