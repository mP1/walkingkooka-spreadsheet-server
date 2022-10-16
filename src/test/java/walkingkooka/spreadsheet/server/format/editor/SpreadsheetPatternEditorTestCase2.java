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

package walkingkooka.spreadsheet.server.format.editor;

import org.junit.jupiter.api.Test;
import walkingkooka.HashCodeEqualsDefinedTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;

public abstract class SpreadsheetPatternEditorTestCase2<T> extends SpreadsheetPatternEditorTestCase<T> implements HashCodeEqualsDefinedTesting2<T>,
        JsonNodeMarshallingTesting<T> {

    SpreadsheetPatternEditorTestCase2() {
        super();
    }

    @Test
    public final void testJsonRoundtrip() {
        this.marshallRoundTripTwiceAndCheck(this.createObject());
    }

    @Override
    public final T createJsonNodeMarshallingValue() {
        return this.createObject();
    }

    // ClassTesting......................................................................................................

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
