/*
 * Copyright (C) 2022 The Libphonenumber Authors
 * Copyright (C) 2022 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.michaelrocks.libphonenumber.android.metadata.source;

import junit.framework.TestCase;

import org.junit.function.ThrowingRunnable;

import static org.junit.Assert.assertThrows;

public final class MultiFileModeFileNameProviderTest extends TestCase {

  private final PhoneMetadataFileNameProvider metadataFileNameProvider =
      new MultiFileModeFileNameProvider("some/file");

  public void test_getFor_shouldAppendKeyToTheBase() {
    String metadataFileName = metadataFileNameProvider.getFor("key1");

    assertEquals("some/file_key1", metadataFileName);
  }

  public void test_getFor_shouldThrowExceptionForNonAlphanumericKey() {
    assertThrows(
        IllegalArgumentException.class,
        new ThrowingRunnable() {
          @Override
          public void run() {
            metadataFileNameProvider.getFor("\tkey1\n");
          }
        });
  }

  public void getFor_shouldThrowExceptionForEmptyKey() {
    assertThrows(
        IllegalArgumentException.class,
        new ThrowingRunnable() {
          @Override
          public void run() {
            metadataFileNameProvider.getFor("");
          }
        });
  }
}
