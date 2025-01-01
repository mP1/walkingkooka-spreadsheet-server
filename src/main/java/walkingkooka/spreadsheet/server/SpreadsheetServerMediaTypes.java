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

package walkingkooka.spreadsheet.server;

import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.reflect.PublicStaticHelper;

/**
 * A collection of constants for server.
 */
public final class SpreadsheetServerMediaTypes implements PublicStaticHelper {

    /**
     * The json media type for all {@link walkingkooka.net.http.server.hateos.HateosResourceHandler}.
     */
    public final static MediaType CONTENT_TYPE = HateosResourceHandlerContext.HATEOS_DEFAULT_CONTENT_TYPE;

    /**
     * The content-type for binary non multi-part requests.
     */
    public final static MediaType BINARY = MediaType.BINARY;

    /**
     * The content-type for binary payloads that are base64 encoded.
     */
    public final static MediaType BASE64 = MediaType.TEXT_BASE64;

    /**
     * Stop creation
     */
    private SpreadsheetServerMediaTypes() {
        throw new UnsupportedOperationException();
    }
}
