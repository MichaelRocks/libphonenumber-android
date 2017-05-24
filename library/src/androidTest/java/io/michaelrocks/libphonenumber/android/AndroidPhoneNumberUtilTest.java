package io.michaelrocks.libphonenumber.android;

import static org.junit.Assert.assertEquals;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AndroidPhoneNumberUtilTest {
  private final Application application =
      (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();

  @Test
  public void litmus() {
    final PhoneNumberUtil util = AndroidPhoneNumberUtil.createInstance(application);
    assertEquals(64, util.getCountryCodeForRegion("NZ"));
  }
}
