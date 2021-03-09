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

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.net.http.server.hateos.HateosHandlerTesting;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class SpreadsheetLabelHateosHandlerTestCase2<H extends SpreadsheetLabelHateosHandler>
        extends SpreadsheetLabelHateosHandlerTestCase<H>
        implements HateosHandlerTesting<H, SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping>,
        ToStringTesting<H> {

    SpreadsheetLabelHateosHandlerTestCase2() {
        super();
    }

    @Test
    public final void testWithNullSpreadsheetLabelStoreFails() {
        assertThrows(NullPointerException.class, () -> this.createHandler(null));
    }

    @Override
    public final H createHandler() {
        return this.createHandler(this.store());
    }

    abstract H createHandler(final SpreadsheetLabelStore store);

    abstract SpreadsheetLabelStore store();
}
