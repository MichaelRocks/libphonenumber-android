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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.michaelrocks.libphonenumber.android.Phonemetadata.PhoneMetadata;
import io.michaelrocks.libphonenumber.android.Phonemetadata.PhoneMetadataCollection;

/**
 * Implementation of {@link MetadataSource} that reads from multiple resource files.
 */
final class MultiFileMetadataSourceImpl implements MetadataSource {

  private static final Logger LOGGER =
      Logger.getLogger(MultiFileMetadataSourceImpl.class.getName());

  private static final String META_DATA_FILE_PREFIX =
      "/io/michaelrocks/libphonenumber/android/data/PhoneNumberMetadataProto";
  private static final String ALTERNATE_FORMATS_FILE_PREFIX =
      "/io/michaelrocks/libphonenumber/android/data/PhoneNumberAlternateFormatsProto";
  private static final String SHORT_NUMBER_METADATA_FILE_PREFIX =
      "/io/michaelrocks/libphonenumber/android/data/ShortNumberMetadataProto";

  // The size of the byte buffer used for deserializing the phone number metadata files for each region.
  private static final int MULTI_FILE_BUFFER_SIZE = 16 * 1024;

  // A mapping from a region code to the PhoneMetadata for that region.
  private final ConcurrentHashMap<String, PhoneMetadata> geographicalRegions =
      new ConcurrentHashMap<String, PhoneMetadata>();

  // A mapping from a country calling code for a non-geographical entity to the PhoneMetadata for
  // that country calling code. Examples of the country calling codes include 800 (International
  // Toll Free Service) and 808 (International Shared Cost Service).
  private final ConcurrentHashMap<Integer, PhoneMetadata> nonGeographicalRegions =
      new ConcurrentHashMap<Integer, PhoneMetadata>();

  private final Map<Integer, PhoneMetadata> alternateFormats =
      Collections.synchronizedMap(new HashMap<Integer, PhoneMetadata>());
  private final Map<String, PhoneMetadata> shortNumbers =
      Collections.synchronizedMap(new HashMap<String, PhoneMetadata>());

  // A set of which country calling codes there are alternate format data for. If the set has an
  // entry for a code, then there should be data for that code linked into the resources.
  private final Set<Integer> countryCodeSet =
      AlternateFormatsCountryCodeSet.getCountryCodeSet();

  // A set of which region codes there are short number data for. If the set has an entry for a
  // code, then there should be data for that code linked into the resources.
  private final Set<String> regionCodeSet = ShortNumbersRegionCodeSet.getRegionCodeSet();

  // The prefix of the metadata files from which region data is loaded.
  private final String filePrefix;
  // The prefix of the metadata files from which alternate format data is loaded.
  private final String alternateFormatsFilePrefix;
  // The prefix of the metadata files from which short number data is loaded.
  private final String shortNumberFilePrefix;

  // The metadata loader used to inject alternative metadata sources.
  private final MetadataLoader metadataLoader;

  // It is assumed that metadataLoader is not null. If needed, checks should happen before passing
  // here.
  public MultiFileMetadataSourceImpl(String filePrefix, String alternateFormatsFilePrefix, String shortNumberFilePrefix,
      MetadataLoader metadataLoader) {
    this.filePrefix = filePrefix;
    this.alternateFormatsFilePrefix = alternateFormatsFilePrefix;
    this.shortNumberFilePrefix = shortNumberFilePrefix;
    this.metadataLoader = metadataLoader;
  }

  // It is assumed that metadataLoader is not null. If needed, checks should happen before passing
  // here.
  public MultiFileMetadataSourceImpl(MetadataLoader metadataLoader) {
    this(META_DATA_FILE_PREFIX, ALTERNATE_FORMATS_FILE_PREFIX, SHORT_NUMBER_METADATA_FILE_PREFIX, metadataLoader);
  }

  @Override
  public PhoneMetadata getMetadataForRegion(String regionCode) {
    PhoneMetadata metadata = geographicalRegions.get(regionCode);
    return (metadata != null) ? metadata : loadMetadataFromFile(
        regionCode, geographicalRegions, filePrefix, metadataLoader);
  }

  @Override
  public PhoneMetadata getMetadataForNonGeographicalRegion(int countryCallingCode) {
    PhoneMetadata metadata = nonGeographicalRegions.get(countryCallingCode);
    if (metadata != null) {
      return metadata;
    }
    if (isNonGeographical(countryCallingCode)) {
      return loadMetadataFromFile(
          countryCallingCode, nonGeographicalRegions, filePrefix, metadataLoader);
    }
    // The given country calling code was for a geographical region.
    return null;
  }

  @Override
  public PhoneMetadata getAlternateFormatsForCountry(int countryCallingCode) {
    if (!countryCodeSet.contains(countryCallingCode)) {
      return null;
    }
    synchronized (alternateFormats) {
      if (!alternateFormats.containsKey(countryCallingCode)) {
        loadAlternateFormatsMetadataFromFile(countryCallingCode);
      }
    }
    return alternateFormats.get(countryCallingCode);
  }

  @Override
  public PhoneMetadata getShortNumberMetadataForRegion(String regionCode) {
    if (!regionCodeSet.contains(regionCode)) {
      return null;
    }
    synchronized (shortNumbers) {
      if (!shortNumbers.containsKey(regionCode)) {
        loadShortNumberMetadataFromFile(regionCode);
      }
    }
    return shortNumbers.get(regionCode);
  }

  // A country calling code is non-geographical if it only maps to the non-geographical region code,
  // i.e. "001".
  private boolean isNonGeographical(int countryCallingCode) {
    List<String> regionCodes =
        CountryCodeToRegionCodeMap.getCountryCodeToRegionCodeMap().get(countryCallingCode);
    return (regionCodes.size() == 1
        && PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY.equals(regionCodes.get(0)));
  }

  /**
   * @param key             The geographical region code or the non-geographical region's country
   *                        calling code.
   * @param map             The map to contain the mapping from {@code key} to the corresponding
   *                        metadata.
   * @param filePrefix      The prefix of the metadata files from which region data is loaded.
   * @param metadataLoader  The metadata loader used to inject alternative metadata sources.
   */
  // @VisibleForTesting
  static <T> PhoneMetadata loadMetadataFromFile(
      T key, ConcurrentHashMap<T, PhoneMetadata> map, String filePrefix,
      MetadataLoader metadataLoader) {
    // We assume key.toString() is well-defined.
    String fileName = filePrefix + "_" + key;
    InputStream source = metadataLoader.loadMetadata(fileName);
    if (source == null) {
      throw new IllegalStateException("missing metadata: " + fileName);
    }
    PhoneMetadataCollection metadataCollection = loadMetadataAndCloseInput(source);
    List<PhoneMetadata> metadataList = metadataCollection.getMetadataList();
    if (metadataList.isEmpty()) {
      // Sanity check; this should not happen since we only load things based on the expectation
      // that they are present, by checking the map of available data first.
      throw new IllegalStateException("empty metadata: " + fileName);
    }
    if (metadataList.size() > 1) {
      LOGGER.log(Level.WARNING, "invalid metadata (too many entries): " + fileName);
    }
    PhoneMetadata metadata = metadataList.get(0);
    PhoneMetadata oldValue = map.putIfAbsent(key, metadata);
    return (oldValue != null) ? oldValue : metadata;
  }

  private void loadAlternateFormatsMetadataFromFile(int countryCallingCode) {
    try {
      PhoneMetadataCollection alternateFormatsMetadata =
          loadMetadataFromFile(alternateFormatsFilePrefix + "_" + countryCallingCode);
      for (PhoneMetadata metadata : alternateFormatsMetadata.getMetadataList()) {
        alternateFormats.put(metadata.getCountryCode(), metadata);
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, e.toString());
    }
  }

  private void loadShortNumberMetadataFromFile(String regionCode) {
    try {
      PhoneMetadataCollection shortNumbersMetadata =
          loadMetadataFromFile(shortNumberFilePrefix + "_" + regionCode);
      for (PhoneMetadata metadata : shortNumbersMetadata.getMetadataList()) {
        shortNumbers.put(regionCode, metadata);
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, e.toString());
    }
  }

  private PhoneMetadataCollection loadMetadataFromFile(String fileName) throws IOException {
    InputStream source = metadataLoader.loadMetadata(fileName);
    if (source == null) {
      LOGGER.log(Level.SEVERE, "missing metadata: " + fileName);
      throw new IllegalStateException("missing metadata: " + fileName);
    }
    return loadMetadataAndCloseInput(source);
  }

  /**
   * Loads and returns the metadata object from the given stream and closes the stream.
   *
   * @param source  the non-null stream from which metadata is to be read
   * @return  the loaded metadata object
   */
  static PhoneMetadataCollection loadMetadataAndCloseInput(InputStream source) {
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
        LOGGER.log(Level.WARNING, "error closing input stream (ignored)", e);
      }
    }
  }
}
