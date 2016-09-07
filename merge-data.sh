#!/usr/bin/env bash

LOCAL="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cp $1/java/libphonenumber/src/com/google/i18n/phonenumbers/data/* ${LOCAL}/library/src/main/assets/io/michaelrocks/libphonenumber/android/data
cp $1/java/libphonenumber/src/com/google/i18n/phonenumbers/data/* ${LOCAL}/library/src/test/resources/io/michaelrocks/libphonenumber/android/data
cp $1/java/libphonenumber/test/com/google/i18n/phonenumbers/data/* ${LOCAL}/library/src/test/resources/io/michaelrocks/libphonenumber/android/data
