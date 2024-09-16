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
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.server.FakeSpreadsheetHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;

public abstract class SpreadsheetDeltaHateosResourceHandlerLabelTestCase<H extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetLabelName>>
        extends SpreadsheetDeltaHateosResourceHandlerTestCase2<H, SpreadsheetLabelName>
        implements HateosResourceHandlerTesting<H, SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetHateosResourceHandlerContext>,
        ToStringTesting<H> {

    SpreadsheetDeltaHateosResourceHandlerLabelTestCase() {
        super();
    }

    final TestSpreadsheetHateosResourceHandlerContext context(final SpreadsheetLabelStore store) {
        return new TestSpreadsheetHateosResourceHandlerContext() {

            @Override
            public SpreadsheetStoreRepository storeRepository() {
                return new FakeSpreadsheetStoreRepository() {
                    @Override
                    public SpreadsheetLabelStore labels() {
                        return store;
                    }
                };
            }
        };
    }

    static class TestSpreadsheetHateosResourceHandlerContext extends FakeSpreadsheetHateosResourceHandlerContext {
        @Override
        public MediaType contentType() {
            return CONTENT_TYPE;
        }
    }
}
