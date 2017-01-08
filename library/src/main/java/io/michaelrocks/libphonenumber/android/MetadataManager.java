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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.michaelrocks.libphonenumber.android.Phonemetadata.PhoneMetadata;
import io.michaelrocks.libphonenumber.android.Phonemetadata.PhoneMetadataCollection;

/**
 * Manager for loading metadata for alternate formats and short numbers. We also declare some
 * constants for phone number metadata loading, to more easily maintain all three types of metadata
 * together.
 * TODO: Consider managing phone number metadata loading here too.
 */
final class MetadataManager {
  static final String MULTI_FILE_PHONE_NUMBER_METADATA_FILE_PREFIX =
      "/io/michaelrocks/libphonenumber/android/data/PhoneNumberMetadataProto";
  static final String ALTERNATE_FORMATS_FILE_PREFIX =
      "/io/michaelrocks/libphonenumber/android/data/PhoneNumberAlternateFormatsProto";
  static final String SHORT_NUMBER_METADATA_FILE_PREFIX =
      "/io/michaelrocks/libphonenumber/android/data/ShortNumberMetadataProto";

  private static final Logger logger = Logger.getLogger(MetadataManager.class.getName());

  // The {@link MetadataLoader} used to inject alternative metadata sources.
  private final MetadataLoader metadataLoader;

  // A mapping from a country calling code to the alternate formats for that country calling code.
  private final ConcurrentHashMap<Integer, PhoneMetadata> alternateFormatsMap =
      new ConcurrentHashMap<Integer, PhoneMetadata>();

  // A mapping from a region code to the short number metadata for that region code.
  private final ConcurrentHashMap<String, PhoneMetadata> shortNumberMetadataMap =
      new ConcurrentHashMap<String, PhoneMetadata>();

  // The set of country calling codes for which there are alternate formats. For every country
  // calling code in this set there should be metadata linked into the resources.
  private final Set<Integer> alternateFormatsCountryCodes =
      AlternateFormatsCountryCodeSet.getCountryCodeSet();

  // The set of region codes for which there are short number metadata. For every region code in
  // this set there should be metadata linked into the resources.
  private final Set<String> shortNumberMetadataRegionCodes =
      ShortNumbersRegionCodeSet.getRegionCodeSet();

  MetadataManager(MetadataLoader metadataLoader) {
    this.metadataLoader = metadataLoader;
  }

  PhoneMetadata getAlternateFormatsForCountry(int countryCallingCode, String filePrefix) {
    if (!alternateFormatsCountryCodes.contains(countryCallingCode)) {
      return null;
    }
    return getMetadataFromMultiFilePrefix(countryCallingCode, alternateFormatsMap, filePrefix);
  }

  PhoneMetadata getShortNumberMetadataForRegion(String regionCode, String filePrefix) {
    if (!shortNumberMetadataRegionCodes.contains(regionCode)) {
      return null;
    }
    return getMetadataFromMultiFilePrefix(regionCode, shortNumberMetadataMap, filePrefix);
  }

  Set<String> getSupportedShortNumberRegions() {
    return Collections.unmodifiableSet(shortNumberMetadataRegionCodes);
  }

  /**
   * @param key  the lookup key for the provided map, typically a region code or a country calling
   *     code
   * @param map  the map containing mappings of already loaded metadata from their {@code key}. If
   *     this {@code key}'s metadata isn't already loaded, it will be added to this map after
   *     loading
   * @param filePrefix  the prefix of the file to load metadata from
   */
  <T> PhoneMetadata getMetadataFromMultiFilePrefix(T key,
      ConcurrentHashMap<T, PhoneMetadata> map, String filePrefix) {
    PhoneMetadata metadata = map.get(key);
    if (metadata != null) {
      return metadata;
    }
    // We assume key.toString() is well-defined.
    String fileName = filePrefix + "_" + key;
    List<PhoneMetadata> metadataList = getMetadataFromSingleFileName(fileName, metadataLoader);
    if (metadataList.size() > 1) {
      logger.log(Level.WARNING, "more than one metadata in file " + fileName);
    }
    metadata = metadataList.get(0);
    PhoneMetadata oldValue = map.putIfAbsent(key, metadata);
    return (oldValue != null) ? oldValue : metadata;
  }

  private static List<PhoneMetadata> getMetadataFromSingleFileName(String fileName,
      MetadataLoader metadataLoader) {
    InputStream source = metadataLoader.loadMetadata(fileName);
    if (source == null) {
      // Sanity check; this would only happen if we packaged jars incorrectly.
      throw new IllegalStateException("missing metadata: " + fileName);
    }
    PhoneMetadataCollection metadataCollection = loadMetadataAndCloseInput(source);
    List<PhoneMetadata> metadataList = metadataCollection.getMetadataList();
    if (metadataList.size() == 0) {
      // Sanity check; this should not happen since we build with non-empty metadata.
      throw new IllegalStateException("empty metadata: " + fileName);
    }
    return metadataList;
  }

  /**
   * Loads and returns the metadata from the given stream and closes the stream.
   *
   * @param source  the non-null stream from which metadata is to be read
   * @return  the loaded metadata
   */
  private static PhoneMetadataCollection loadMetadataAndCloseInput(InputStream source) {
    ObjectInputStream ois = null;
    try {
      try {
        ois = new ObjectInputStream(source);
      } catch (IOException e) {
        throw new RuntimeException("cannot load/parse metadata", e);
      }
      PhoneMetadataCollection metadataCollection = new PhoneMetadataCollection();
      try {
        metadataCollection.readExternal(ois);
      } catch (IOException e) {
        throw new RuntimeException("cannot load/parse metadata", e);
      }
      return metadataCollection;
    } finally {
      try {
        if (ois != null) {
          // This will close all underlying streams as well, including source.
          ois.close();
        } else {
          source.close();
        }
      } catch (IOException e) {
        logger.log(Level.WARNING, "error closing input stream (ignored)", e);
      }
    }
  }
}
