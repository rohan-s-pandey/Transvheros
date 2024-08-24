package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView aboutTextView = findViewById(R.id.aboutText);
        SpannableString spannableString = new SpannableString(aboutTextView.getText());

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Handle clicking the email address here
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:pandeynitin1212@gmail.com"));
                startActivity(intent);
            }
        };

        int startIndex = aboutTextView.getText().toString().indexOf("pandeynitin1212@gmail.com");
        if (startIndex != -1) {
            spannableString.setSpan(clickableSpan, startIndex, startIndex + "pandeynitin1212@gmail.com".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            aboutTextView.setText(spannableString);
            aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }


    }
}
