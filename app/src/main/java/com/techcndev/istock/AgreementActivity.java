package com.techcndev.istock;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class AgreementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        TextView agreement_textview = findViewById(R.id.agreement_text);
        agreement_textview.setText(Html.fromHtml(getString(R.string.agreement_text)));
    }

    public void close_agreement(View view) {
        finish();
    }
}