/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.spreadsheet.server.plugin;

import walkingkooka.Binary;
import walkingkooka.net.header.Accept;
import walkingkooka.net.header.MediaTypeDetector;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.reflect.StaticHelper;

import java.io.IOException;

class PluginFileDownloadHttpHandlerFileExtractorGwt implements StaticHelper {

    static HttpEntity extractFile(final Binary binary,
                                  final String filename,
                                  final MediaTypeDetector contentTypeDetector,
                                  final Accept accept) throws IOException {
        throw new UnsupportedOperationException();
    }

    PluginFileDownloadHttpHandlerFileExtractorGwt() {
        throw new UnsupportedOperationException();
    }
}
