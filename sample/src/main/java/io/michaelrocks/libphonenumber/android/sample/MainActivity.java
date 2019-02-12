package io.michaelrocks.libphonenumber.android.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final TextView textView = findViewById(R.id.textView);
    findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
      private PhoneNumberUtil util = null;

      @Override
      public void onClick(final View v) {
        if (util == null) {
          util = PhoneNumberUtil.createInstance(getApplicationContext());
        }

        try {
          final Phonenumber.PhoneNumber phoneNumber = util.parse("8005551212", "US");
          textView.setText(util.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL));
        } catch (NumberParseException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
