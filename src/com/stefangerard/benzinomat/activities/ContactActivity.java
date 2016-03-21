package com.stefangerard.benzinomat.activities;

import com.stefangerard.benzinomat.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ContactActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        ImageButton backButton = (ImageButton) findViewById(R.id.contactActionBarBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView imprint = (TextView) findViewById(R.id.imprint);
        imprint.setText(Html.fromHtml(getResources().getString(R.string.imprint)));
        imprint.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
