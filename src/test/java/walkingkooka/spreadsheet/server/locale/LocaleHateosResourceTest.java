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

package walkingkooka.spreadsheet.server.locale;

import org.junit.jupiter.api.Test;
import walkingkooka.compare.ComparableTesting2;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.text.HasTextTesting;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.util.HasLocaleTesting;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class LocaleHateosResourceTest implements ComparableTesting2<LocaleHateosResource>,
    HasTextTesting,
    HasLocaleTesting,
    TreePrintableTesting,
    JsonNodeMarshallingTesting<LocaleHateosResource>,
    ClassTesting2<LocaleHateosResource> {

    private final static Locale LOCALE = Locale.forLanguageTag("EN-AU");

    private final static LocaleLanguageTag LOCALE_TAG = LocaleLanguageTag.fromLocale(LOCALE);

    private final static String TEXT = "Australian English";

    @Test
    public void testWithNullLocaleLanguageTagFails() {
        assertThrows(
            NullPointerException.class,
            () -> LocaleHateosResource.with(
                null,
                TEXT
            )
        );
    }

    @Test
    public void testWithNullTextFails() {
        assertThrows(
            NullPointerException.class,
            () -> LocaleHateosResource.with(
                LOCALE_TAG,
                null
            )
        );
    }

    @Test
    public void testWithEmptyTextFails() {
        assertThrows(
            IllegalArgumentException.class,
            () -> LocaleHateosResource.with(
                LOCALE_TAG,
                ""
            )
        );
    }


    @Test
    public void testWith() {
        final LocaleHateosResource resource = LocaleHateosResource.with(
            LOCALE_TAG,
            TEXT
        );
        this.checkEquals(
            LOCALE_TAG,
            resource.value()
        );

        this.checkEquals(
            "en-AU",
                resource.hateosLinkId()
        );

        this.textAndCheck(
            resource,
            TEXT
        );
    }

    // HasText..........................................................................................................

    @Test
    public void testText() {
        this.textAndCheck(
            LocaleHateosResource.with(
                LOCALE_TAG,
                TEXT
            ),
            TEXT
        );
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrintable() {
        this.treePrintAndCheck(
            LocaleHateosResource.with(
                LOCALE_TAG,
                TEXT
            ),
            "en-AU\n" +
                "  Australian English\n"
        );
    }

    // comparable.......................................................................................................

    @Test
    public void testComparableLess() {
        this.compareToAndCheckLess(
            LocaleHateosResource.with(
                LocaleLanguageTag.fromLocale(
                    Locale.FRANCE
                ),
                "France ...."
            )
        );
    }

    @Override
    public LocaleHateosResource createComparable() {
        return LocaleHateosResource.with(
            LOCALE_TAG,
            TEXT
        );
    }

    // json.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            this.createJsonNodeMarshallingValue(),
            "{\n" +
                "  \"locale\": \"en-AU\",\n" +
                "  \"text\": \"Australian English\"\n" +
                "}"
        );
    }

    @Override
    public LocaleHateosResource unmarshall(final JsonNode node,
                                           final JsonNodeUnmarshallContext context) {
        return LocaleHateosResource.unmarshall(
            node,
            context
        );
    }

    @Override
    public LocaleHateosResource createJsonNodeMarshallingValue() {
        return LocaleHateosResource.with(
            LOCALE_TAG,
            TEXT
        );
    }

    // class............................................................................................................

    @Override
    public Class<LocaleHateosResource> type() {
        return LocaleHateosResource.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
