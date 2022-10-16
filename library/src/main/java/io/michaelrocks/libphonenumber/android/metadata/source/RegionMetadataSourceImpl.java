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

import io.michaelrocks.libphonenumber.android.MetadataLoader;
import io.michaelrocks.libphonenumber.android.Phonemetadata.PhoneMetadata;
import io.michaelrocks.libphonenumber.android.internal.GeoEntityUtility;
import io.michaelrocks.libphonenumber.android.metadata.init.MetadataParser;

/**
 * Implementation of {@link RegionMetadataSource} guarded by {@link MetadataBootstrappingGuard}
 *
 * <p>By default, a {@link BlockingMetadataBootstrappingGuard} will be used, but any custom
 * implementation can be injected.
 */
public final class RegionMetadataSourceImpl implements RegionMetadataSource {

  private final PhoneMetadataFileNameProvider phoneMetadataFileNameProvider;
  private final MetadataBootstrappingGuard<MapBackedMetadataContainer<String>>
      bootstrappingGuard;

  public RegionMetadataSourceImpl(
      PhoneMetadataFileNameProvider phoneMetadataFileNameProvider,
      MetadataBootstrappingGuard<MapBackedMetadataContainer<String>> bootstrappingGuard) {
    this.phoneMetadataFileNameProvider = phoneMetadataFileNameProvider;
    this.bootstrappingGuard = bootstrappingGuard;
  }

  public RegionMetadataSourceImpl(
      PhoneMetadataFileNameProvider phoneMetadataFileNameProvider,
      MetadataLoader metadataLoader,
      MetadataParser metadataParser) {
    this(
        phoneMetadataFileNameProvider,
        new BlockingMetadataBootstrappingGuard<>(
            metadataLoader, metadataParser, MapBackedMetadataContainer.byRegionCode()));
  }

  @Override
  public PhoneMetadata getMetadataForRegion(String regionCode) {
    if (!GeoEntityUtility.isGeoEntity(regionCode)) {
      throw new IllegalArgumentException(regionCode + " region code is a non-geo entity");
    }
    return bootstrappingGuard
        .getOrBootstrap(phoneMetadataFileNameProvider.getFor(regionCode))
        .getMetadataBy(regionCode);
  }
}
