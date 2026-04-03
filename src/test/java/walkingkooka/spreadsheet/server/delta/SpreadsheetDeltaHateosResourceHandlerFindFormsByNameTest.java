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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetCellQueryRequest;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.validation.form.SpreadsheetForms;
import walkingkooka.validation.form.FormName;

import java.util.Map;
import java.util.Optional;

public final class SpreadsheetDeltaHateosResourceHandlerFindFormsByNameTest extends SpreadsheetDeltaHateosResourceHandlerTestCase2<SpreadsheetDeltaHateosResourceHandlerFindFormsByName,
    FormName> {

    private final static int OFFSET = 12;
    private final static int COUNT = 34;

    private final static FormName FORM = FormName.with("Form123");

    @Test
    public void testHandleAll() {
        final SpreadsheetDelta expected = SpreadsheetDelta.EMPTY.setForms(
            Sets.of(
                SpreadsheetForms.form(FORM)
            )
        );

        final String text = FORM.text();

        this.handleAllAndCheck(
            Optional.empty(), // resource
            Maps.of(
                SpreadsheetCellQueryRequest.OFFSET, Lists.of("" + OFFSET),
                SpreadsheetCellQueryRequest.COUNT, Lists.of("" + COUNT)
            ), // parameters
            UrlPath.parse("/" + text),
            this.context(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta findFormsByName(final String t,
                                                             final int offset,
                                                             final int count,
                                                             final SpreadsheetEngineContext context) {
                        checkEquals(text, t, "text");
                        checkEquals(OFFSET, offset, "offset");
                        checkEquals(COUNT, count, "count");
                        return expected;
                    }
                }
            ),
            Optional.of(
                expected
            )
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHandler(),
            SpreadsheetEngine.class.getSimpleName() + ".findFormsByName"
        );
    }

    @Override
    public SpreadsheetDeltaHateosResourceHandlerFindFormsByName createHandler() {
        return SpreadsheetDeltaHateosResourceHandlerFindFormsByName.INSTANCE;
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public FormName id() {
        return FORM;
    }

    @Override
    public Range<FormName> range() {
        return Range.singleton(FORM);
    }

    @Override
    public Optional<SpreadsheetDelta> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.empty();
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return SpreadsheetEngineHateosResourceHandlerContexts.fake();
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerFindFormsByName> type() {
        return SpreadsheetDeltaHateosResourceHandlerFindFormsByName.class;
    }
}
