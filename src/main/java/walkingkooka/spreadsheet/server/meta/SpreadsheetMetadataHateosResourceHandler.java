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

package walkingkooka.spreadsheet.server.meta;

import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.store.Store;

/**
 * A {@link HateosResourceHandler} that requires a {@link Store metadata}.
 */
abstract class SpreadsheetMetadataHateosResourceHandler implements HateosResourceHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet, SpreadsheetMetadataHateosResourceHandlerContext> {

    /**
     * {@see SpreadsheetMetadataHateosResourceHandlerDelete}
     */
    static SpreadsheetMetadataHateosResourceHandlerDelete delete() {
        return SpreadsheetMetadataHateosResourceHandlerDelete.INSTANCE;
    }

    /**
     * {@see SpreadsheetMetadataHateosResourceHandlerLoad}
     */
    static SpreadsheetMetadataHateosResourceHandlerLoad load() {
        return SpreadsheetMetadataHateosResourceHandlerLoad.INSTANCE;
    }

    /**
     * {@see SpreadsheetMetadataHateosResourceHandlerSaveOrUpdate}
     */
    static SpreadsheetMetadataHateosResourceHandlerSaveOrUpdate saveOrUpdate() {
        return SpreadsheetMetadataHateosResourceHandlerSaveOrUpdate.INSTANCE;
    }

    SpreadsheetMetadataHateosResourceHandler() {
        super();
    }

    @Override
    public final String toString() {
        return this.operation();
    }

    abstract String operation();
}
