#!/usr/bin/env bash

LOCAL="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

pushd $1
git diff $2 $3 -- java/libphonenumber/**/*.java > ${LOCAL}/$3.patch
popd

sed -i \
  -e 's:java/libphonenumber/src/com/google/i18n/phonenumbers:library/src/main/java/io/michaelrocks/libphonenumber/android:g' \
  -e 's:java/libphonenumber/test/com/google/i18n/phonenumbers:library/src/test/java/io/michaelrocks/libphonenumber/android:g' \
  ${LOCAL}/$3.patch

patch -p1 < ${LOCAL}/$3.patch

rm ${LOCAL}/$3.patch
