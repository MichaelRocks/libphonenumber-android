package io.michaelrocks.libphonenumber.android;

import android.content.Context;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

/** Android-specific initializer for the libphonenumber library. */
public final class AndroidPhoneNumberUtil {

  public static PhoneNumberUtil createInstance(Context context) {
    if (context == null) {
      throw new IllegalArgumentException("context == null");
    }
    return PhoneNumberUtil.createInstance(new AssetsMetadataLoader(context.getAssets()));
  }

  private AndroidPhoneNumberUtil() {
    throw new AssertionError("No instances.");
  }
}
