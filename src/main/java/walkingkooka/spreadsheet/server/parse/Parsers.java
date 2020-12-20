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

package walkingkooka.spreadsheet.server.parse;

import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;

import java.util.function.Function;

public final class Parsers implements PublicStaticHelper {

    /**
     * {@see MultiParseter}
     */
    public static Function<MultiParseRequest, MultiParseResponse> multiParsers(final SpreadsheetEngineContext engineContext) {
        return MultiParser.with(engineContext);
    }

    private Parsers() {
        throw new UnsupportedOperationException();
    }
}
