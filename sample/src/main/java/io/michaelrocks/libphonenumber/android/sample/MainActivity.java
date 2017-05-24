package io.michaelrocks.libphonenumber.android.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import io.michaelrocks.libphonenumber.android.AndroidPhoneNumberUtil;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final TextView textView = (TextView) findViewById(R.id.textView);
    findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
      private PhoneNumberUtil util = null;

      @Override
      public void onClick(final View v) {
        if (util == null) {
          util = AndroidPhoneNumberUtil.createInstance(getApplicationContext());
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
