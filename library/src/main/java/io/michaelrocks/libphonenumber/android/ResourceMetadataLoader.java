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

import java.io.InputStream;

class ResourceMetadataLoader implements MetadataLoader {
  private final Class<?> loaderClass;

  public ResourceMetadataLoader() {
    this(ResourceMetadataLoader.class);
  }

  public ResourceMetadataLoader(Class<?> loaderClass) {
    this.loaderClass = loaderClass;
  }

  @Override
  public InputStream loadMetadata(String metadataFileName) {
    return loaderClass.getResourceAsStream(metadataFileName);
  }
}
