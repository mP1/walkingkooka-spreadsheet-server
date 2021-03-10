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

package walkingkooka.spreadsheet.server.label.hateos;

import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;

import java.util.function.BiConsumer;

public final class SpreadsheetLabelHateosHandlers implements PublicStaticHelper {

    /**
     * {@see SpreadsheetLabelHateosHandlerDelete}
     */
    public static HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> delete(final SpreadsheetLabelStore store) {
        return SpreadsheetLabelHateosHandlerDelete.with(store);
    }

    /**
     * {@see SpreadsheetLabelHateosHandlerLoad}
     */
    public static HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> load(final SpreadsheetLabelStore store) {
        return SpreadsheetLabelHateosHandlerLoad.with(store);
    }

    /**
     * {@see SpreadsheetLabelHateosHandlersRouter}
     */
    public static Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> router(final AbsoluteUrl baseUrl,
                                                                                                final HateosContentType contentType,
                                                                                                final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> delete,
                                                                                                final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> load,
                                                                                                final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> saveOrUpdate) {
        return SpreadsheetLabelHateosHandlersRouter.with(
                baseUrl,
                contentType,
                delete,
                load,
                saveOrUpdate
        );
    }

    /**
     * {@see SpreadsheetLabelHateosHandlerSaveOrUpdate}
     */
    public static HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> saveOrUpdate(final SpreadsheetLabelStore store) {
        return SpreadsheetLabelHateosHandlerSaveOrUpdate.with(store);
    }


    /**
     * Stop creation
     */
    private SpreadsheetLabelHateosHandlers() {
        throw new UnsupportedOperationException();
    }
}
