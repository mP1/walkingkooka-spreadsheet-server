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

package walkingkooka.spreadsheet.server.context.hateos;

import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.server.context.SpreadsheetContext;

import java.util.Objects;

/**
 * An abstract {@link HateosHandler} that includes uses a {@link SpreadsheetContext} to do things.
 */
abstract class SpreadsheetContextHateosHandler<I extends Comparable<I>, V, C>
        implements HateosHandler<I, V, C> {

    /**
     * Checks required factory method parameters are not null.
     */
    static void checkContext(final SpreadsheetContext context) {
        Objects.requireNonNull(context, "context");
    }

    /**
     * Package private to limit sub classing.
     */
    SpreadsheetContextHateosHandler(final SpreadsheetContext context) {
        super();
        this.context = context;
    }

    final SpreadsheetContext context;

    @Override
    public final String toString() {
        return this.context + " " + this.operation();
    }

    abstract String operation();
}
