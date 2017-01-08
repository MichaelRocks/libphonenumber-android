/*
 * Copyright (C) 2012 The Libphonenumber Authors
 * Copyright (C) 2017 Michael Rozumyanskiy
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

package io.michaelrocks.libphonenumber.android;

import junit.framework.TestCase;

import java.util.concurrent.ConcurrentHashMap;

import io.michaelrocks.libphonenumber.android.Phonemetadata.PhoneMetadata;

/**
 * Some basic tests to check that metadata can be correctly loaded.
 */
public class MetadataManagerTest extends TestCase {
  private MetadataManager metadataManager = new MetadataManager(new ResourceMetadataLoader(getClass()));

  public void testAlternateFormatsLoadCorrectly() {
    // We should have some data for Germany.
    PhoneMetadata germanyMetadata = metadataManager.getAlternateFormatsForCountry(49,
        MetadataManager.ALTERNATE_FORMATS_FILE_PREFIX);
    assertNotNull(germanyMetadata);
    assertTrue(germanyMetadata.numberFormats().size() > 0);
  }

  public void testAlternateFormatsFailsGracefully() throws Exception {
    PhoneMetadata noAlternateFormats = metadataManager.getAlternateFormatsForCountry(999,
        MetadataManager.ALTERNATE_FORMATS_FILE_PREFIX);
    assertNull(noAlternateFormats);
  }

  public void testShortNumberMetadataLoadCorrectly() throws Exception {
    // We should have some data for France.
    PhoneMetadata franceMetadata = metadataManager.getShortNumberMetadataForRegion("FR",
        MetadataManager.SHORT_NUMBER_METADATA_FILE_PREFIX);
    assertNotNull(franceMetadata);
    assertTrue(franceMetadata.hasShortCode());
  }

  public void testShortNumberMetadataFailsGracefully() throws Exception {
    PhoneMetadata noShortNumberMetadata = metadataManager.getShortNumberMetadataForRegion("XXX",
        MetadataManager.SHORT_NUMBER_METADATA_FILE_PREFIX);
    assertNull(noShortNumberMetadata);
  }

  public void testGetMetadataFromMultiFilePrefix_regionCode() {
    ConcurrentHashMap<String, PhoneMetadata> map = new ConcurrentHashMap<String, PhoneMetadata>();
    PhoneMetadata metadata = metadataManager.getMetadataFromMultiFilePrefix("CA", map,
        "/io/michaelrocks/libphonenumber/android/data/PhoneNumberMetadataProtoForTesting");
    assertEquals(metadata, map.get("CA"));
  }

  public void testGetMetadataFromMultiFilePrefix_countryCallingCode() {
    ConcurrentHashMap<Integer, PhoneMetadata> map = new ConcurrentHashMap<Integer, PhoneMetadata>();
    PhoneMetadata metadata = metadataManager.getMetadataFromMultiFilePrefix(800, map,
        "/io/michaelrocks/libphonenumber/android/data/PhoneNumberMetadataProtoForTesting");
    assertEquals(metadata, map.get(800));
  }

  public void testGetMetadataFromMultiFilePrefix_missingMetadataFileThrowsRuntimeException() {
    // In normal usage we should never get a state where we are asking to load metadata that doesn't
    // exist. However if the library is packaged incorrectly in the jar, this could happen and the
    // best we can do is make sure the exception has the file name in it.
    try {
      metadataManager.getMetadataFromMultiFilePrefix("XX",
          new ConcurrentHashMap<String, PhoneMetadata>(), "no/such/file");
      fail("expected exception");
    } catch (RuntimeException e) {
      assertTrue("Unexpected error: " + e, e.getMessage().contains("no/such/file_XX"));
    }
    try {
      metadataManager.getMetadataFromMultiFilePrefix(123,
          new ConcurrentHashMap<Integer, PhoneMetadata>(), "no/such/file");
      fail("expected exception");
    } catch (RuntimeException e) {
      assertTrue("Unexpected error: " + e, e.getMessage().contains("no/such/file_123"));
    }
  }
}
