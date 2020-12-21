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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;

import static org.junit.jupiter.api.Assertions.assertThrows;


public final class MultiParseResponseTest extends ParserTestCase2<MultiParseResponse> {

    @Test
    public void testWithNullRequests() {
        assertThrows(NullPointerException.class, () -> MultiParseResponse.with(null));
    }

    @Test
    public void testDifferentRequests() {
        this.checkNotEquals(MultiParseResponse.with(
                Lists.of(
                        "different"
                )
        ));
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createObject(), "[hello]");
    }

    @Override
    public MultiParseResponse createObject() {
        return MultiParseResponse.with(Lists.of("hello"));
    }

    @Override
    public Class<MultiParseResponse> type() {
        return MultiParseResponse.class;
    }
}
