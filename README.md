[![Build Status](https://travis-ci.org/MichaelRocks/libphonenumber-android.svg?branch=master)](https://travis-ci.org/MichaelRocks/libphonenumber-android)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-libphonenumber--android-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3676)
[![Methods Count](https://img.shields.io/badge/Methods%20count-core:%20707%20-e91e63.svg)](http://www.methodscount.com/?lib=io.michaelrocks%3Alibphonenumber-android%3A8.4.1)

libphonenumber-android
======================
Android port of Google's [libphonenumber][1].

Why?
----
Google's libphonenumber is a great library but it has to major flaws when used on Android:
 1. ~~It adds about [7k methods][2] to a final dex.~~ Not anymore, since 7.7.0.
 2. Internally the library uses `Class.getResourceAsStream()` method,
 which is [very slow on Android][3].

The goal of this library is to fix these two issues.

Download
--------
Gradle:
```groovy
repositories {
  jcenter()
}

dependencies {
  compile 'io.michaelrocks:libphonenumber-android:8.5.0'
}
```

Usage
-----
```java
PhoneNumberUtil phoneNumberUtil = AndroidPhoneNumberUtil.createInstance(context);
```

It's advisable to instantiate a single instance of `PhoneNumberUtil` and use it throughout your
application.

API differences
---------------
This library is not fully compatible with the original `libphonenumber`. The upstream library
hardcodes metadata loading for the following features:

- Shortcodes
- Alternate phone number formats
- Geocoding
- Prefix mapping

APIs using these features are not supported, e.g.

- [`ShortNumberInfo`](https://static.javadoc.io/com.googlecode.libphonenumber/libphonenumber/8.5.0/com/google/i18n/phonenumbers/ShortNumberInfo.html)
  - All methods are unsupported
- [`PhoneNumberUtil.Leniency`](https://static.javadoc.io/com.googlecode.libphonenumber/libphonenumber/8.5.0/com/google/i18n/phonenumbers/PhoneNumberUtil.html)
  - `STRICT_GROUPING` is unsupported

License
=======
    Copyright 2017 Michael Rozumyanskiy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [1]: https://github.com/googlei18n/libphonenumber
 [2]: http://www.methodscount.com/?lib=com.googlecode.libphonenumber%3Alibphonenumber%3A8.4.1
 [3]: http://blog.nimbledroid.com/2016/04/06/slow-ClassLoader.getResourceAsStream.html
