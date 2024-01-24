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

package walkingkooka.spreadsheet.server.engine;

import walkingkooka.Cast;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteTest extends SpreadsheetEngineHateosHandlerTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDeltaDelete<?>> {

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaDelete<?>> type() {
        return Cast.to(SpreadsheetEngineHateosHandlerSpreadsheetDeltaDelete.class);
    }
}