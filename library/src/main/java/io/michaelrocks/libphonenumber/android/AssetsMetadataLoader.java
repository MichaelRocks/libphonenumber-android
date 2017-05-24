/*
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

import android.content.res.AssetManager;

import com.google.i18n.phonenumbers.MetadataLoader;
import java.io.IOException;
import java.io.InputStream;

class AssetsMetadataLoader implements MetadataLoader {
  private final AssetManager assetManager;

  AssetsMetadataLoader(final AssetManager assetManager) {
    this.assetManager = assetManager;
  }

  @Override
  public InputStream loadMetadata(final String metadataFileName) {
    String assetFileName = metadataFileName.substring(1);
    try {
      return assetManager.open(assetFileName);
    } catch (final IOException exception) {
      return null;
    }
  }
}
