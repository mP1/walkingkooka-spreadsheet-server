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
import walkingkooka.net.header.ContentDispositionFileName;
import walkingkooka.net.header.ContentDispositionType;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.reflect.StaticHelper;

final class PluginHttpEntity implements StaticHelper {

    /**
     * Utility that creates a {@link HttpEntity} with binary content-type, filename and archive as the body.
     */
    static HttpEntity httpEntity(final ContentDispositionFileName filename,
                                 final Binary archive) {
        return HttpEntity.EMPTY
            .setContentType(MediaType.BINARY)
            .addHeader(
                HttpHeaderName.CONTENT_DISPOSITION,
                ContentDispositionType.ATTACHMENT.setFilename(filename)
            ).setBody(archive)
            .setContentLength();
    }

    /**
     * Stop creation
     */
    private PluginHttpEntity() {
        throw new UnsupportedOperationException();
    }
}
