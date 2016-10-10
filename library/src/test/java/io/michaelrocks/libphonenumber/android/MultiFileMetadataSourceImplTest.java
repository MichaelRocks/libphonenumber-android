/*
 * Copyright (C) 2015 The Libphonenumber Authors
 * Copyright (C) 2016 Michael Rozumyanskiy
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
 * Unit tests for MultiFileMetadataSourceImpl.java.
 */
public class MultiFileMetadataSourceImplTest extends TestCase {
  public MultiFileMetadataSourceImplTest() {}

  public void testGeographicalRegionMetadataLoadsCorrectly() {
    ConcurrentHashMap<String, PhoneMetadata> map = new ConcurrentHashMap<String, PhoneMetadata>();
    PhoneMetadata metadata = MultiFileMetadataSourceImpl.loadMetadataFromFile(
        "CA", map, "/io/michaelrocks/libphonenumber/android/data/PhoneNumberMetadataProtoForTesting",
        new ResourceMetadataLoader());
    assertEquals(metadata, map.get("CA"));
  }

  public void testNonGeographicalRegionMetadataLoadsCorrectly() {
    ConcurrentHashMap<Integer, PhoneMetadata> map = new ConcurrentHashMap<Integer, PhoneMetadata>();
    PhoneMetadata metadata = MultiFileMetadataSourceImpl.loadMetadataFromFile(
        800, map, "/io/michaelrocks/libphonenumber/android/data/PhoneNumberMetadataProtoForTesting",
        new ResourceMetadataLoader());
    assertEquals(metadata, map.get(800));
  }

  public void testMissingMetadataFileThrowsRuntimeException() {
    // In normal usage we should never get a state where we are asking to load metadata that doesn't
    // exist. However if the library is packaged incorrectly in the jar, this could happen and the
    // best we can do is make sure the exception has the file name in it.
    try {
      MultiFileMetadataSourceImpl.loadMetadataFromFile(
          "XX", new ConcurrentHashMap<String, PhoneMetadata>(), "no/such/file",
          new ResourceMetadataLoader());
      fail("expected exception");
    } catch (RuntimeException e) {
      assertTrue("Unexpected error: " + e, e.getMessage().contains("no/such/file_XX"));
    }
    try {
      MultiFileMetadataSourceImpl.loadMetadataFromFile(
          123, new ConcurrentHashMap<Integer, PhoneMetadata>(), "no/such/file",
          new ResourceMetadataLoader());
      fail("expected exception");
    } catch (RuntimeException e) {
      assertTrue("Unexpected error: " + e, e.getMessage().contains("no/such/file_123"));
    }
  }

  public void testAlternateFormatsContainsData() throws Exception {
    MultiFileMetadataSourceImpl multiFileMetadataSource =
        new MultiFileMetadataSourceImpl(new ResourceMetadataLoader(MultiFileMetadataSourceImplTest.class));
    // We should have some data for Germany.
    PhoneMetadata germanyAlternateFormats = multiFileMetadataSource.getAlternateFormatsForCountry(49);
    assertNotNull(germanyAlternateFormats);
    assertTrue(germanyAlternateFormats.numberFormatSize() > 0);
  }

  public void testShortNumberMetadataContainsData() throws Exception {
    MultiFileMetadataSourceImpl multiFileMetadataSource =
        new MultiFileMetadataSourceImpl(new ResourceMetadataLoader(MultiFileMetadataSourceImplTest.class));
    // We should have some data for France.
    PhoneMetadata franceShortNumberMetadata =
        multiFileMetadataSource.getShortNumberMetadataForRegion("FR");
    assertNotNull(franceShortNumberMetadata);
    assertTrue(franceShortNumberMetadata.hasShortCode());
  }

  public void testAlternateFormatsFailsGracefully() throws Exception {
    MultiFileMetadataSourceImpl multiFileMetadataSource =
        new MultiFileMetadataSourceImpl(new ResourceMetadataLoader(MultiFileMetadataSourceImplTest.class));
    PhoneMetadata noAlternateFormats = multiFileMetadataSource.getAlternateFormatsForCountry(999);
    assertNull(noAlternateFormats);
  }

  public void testShortNumberMetadataFailsGracefully() throws Exception {
    MultiFileMetadataSourceImpl multiFileMetadataSource =
        new MultiFileMetadataSourceImpl(new ResourceMetadataLoader(MultiFileMetadataSourceImplTest.class));
    PhoneMetadata noShortNumberMetadata = multiFileMetadataSource.getShortNumberMetadataForRegion("XXX");
    assertNull(noShortNumberMetadata);
  }
}
