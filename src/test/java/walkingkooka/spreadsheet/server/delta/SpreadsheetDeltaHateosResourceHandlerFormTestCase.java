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

package walkingkooka.spreadsheet.server.delta;

import walkingkooka.ToStringTesting;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.spreadsheet.validation.form.store.SpreadsheetFormStore;
import walkingkooka.validation.form.FormName;

public abstract class SpreadsheetDeltaHateosResourceHandlerFormTestCase<H extends SpreadsheetDeltaHateosResourceHandler<FormName>>
    extends SpreadsheetDeltaHateosResourceHandlerTestCase2<H, FormName>
    implements HateosResourceHandlerTesting<H, FormName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    ToStringTesting<H> {

    SpreadsheetDeltaHateosResourceHandlerFormTestCase() {
        super();
    }

    final TestSpreadsheetEngineHateosResourceHandlerContext context(final SpreadsheetFormStore store) {
        return this.context(
            this.engine(),
            store
        );
    }

    abstract SpreadsheetEngine engine();

    final TestSpreadsheetEngineHateosResourceHandlerContext context(final SpreadsheetEngine engine,
                                                                    final SpreadsheetFormStore store) {
        return new TestSpreadsheetEngineHateosResourceHandlerContext() {

            @Override
            public SpreadsheetEngine spreadsheetEngine() {
                return engine;
            }

            @Override
            public SpreadsheetStoreRepository storeRepository() {
                return new FakeSpreadsheetStoreRepository() {
                    @Override
                    public SpreadsheetFormStore forms() {
                        return store;
                    }
                };
            }

            @Override
            public SpreadsheetMetadata spreadsheetMetadata() {
                return SpreadsheetMetadata.EMPTY;
            }
        };
    }

    static class TestSpreadsheetEngineHateosResourceHandlerContext extends FakeSpreadsheetEngineHateosResourceHandlerContext {
        @Override
        public MediaType contentType() {
            return CONTENT_TYPE;
        }
    }

    @Override
    public final SpreadsheetEngineHateosResourceHandlerContext context() {
        return CONTEXT;
    }
}
