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
import walkingkooka.collect.set.ImmutableSortedSetTesting;
import walkingkooka.collect.set.Sets;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.locale.FakeLocaleContext;
import walkingkooka.locale.LocaleContext;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class LocaleHateosResourceSetTest implements ImmutableSortedSetTesting<LocaleHateosResourceSet, LocaleHateosResource>,
    TreePrintableTesting,
    JsonNodeMarshallingTesting<LocaleHateosResourceSet> {

    private final static LocaleHateosResource EN_AU = LocaleHateosResource.fromLocale(
        Locale.forLanguageTag("EN-AU")
    );

    private final static LocaleHateosResource EN_NZ = LocaleHateosResource.fromLocale(
        Locale.forLanguageTag("EN-NZ")
    );

    @Test
    public void testWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> LocaleHateosResourceSet.with(null)
        );
    }

    @Test
    public void testDeleteBecomesEmpty() {
        assertSame(
            LocaleHateosResourceSet.EMPTY,
            LocaleHateosResourceSet.EMPTY.concat(EN_AU)
                .delete(EN_AU)
        );
    }

    @Test
    public void testSetElementsWithLocaleHateosResourceSet() {
        final LocaleHateosResourceSet set = LocaleHateosResourceSet.with(
            SortedSets.of(
                LocaleHateosResource.fromLocale(
                    Locale.US
                )
            )
        );

        assertSame(
            set,
            set.setElements(set)
        );
    }

    @Test
    public void testSetElementsWithLocaleHateosResourceSet2() {
        final LocaleHateosResourceSet set = this.createSet();
        final LocaleHateosResourceSet set2 = this.createSet();

        assertSame(
            set2,
            set.setElements(
                set2
            )
        );
    }

    @Override
    public LocaleHateosResourceSet createSet() {
        final SortedSet<LocaleHateosResource> sortedSet = SortedSets.tree();

        sortedSet.add(
            LocaleHateosResource.fromLocale(Locale.ENGLISH)
        );
        sortedSet.add(EN_AU);
        sortedSet.add(EN_NZ);

        return LocaleHateosResourceSet.with(
            SortedSets.of(
                LocaleHateosResource.fromLocale(Locale.ENGLISH),
                EN_AU,
                EN_NZ
            )
        );
    }

    // filter...........................................................................................................

    private final static Locale ENAU = Locale.forLanguageTag("en-AU");
    private final static Locale ENNZ = Locale.forLanguageTag("en-NZ");
    private final static Locale FR = Locale.FRENCH;

    private final static String ENGLISH_AUSTRALIA_TEXT = "English (Australia)";
    private final static String ENGLISH_NEW_ZEALAND_TEXT = "English (New Zealand)";
    private final static String FRENCH_TEXT = "French 123";

    private final static LocaleContext CONTEXT = new FakeLocaleContext() {

        @Override
        public Set<Locale> findByLocaleText(final String text,
                                            final int offset,
                                            final int count) {
            return Sets.of(
                ENAU,
                ENNZ,
                FR
            );
        }

        @Override
        public Optional<String> localeText(final Locale locale) {
            return Optional.ofNullable(
                ENAU.equals(locale) ?
                    ENGLISH_AUSTRALIA_TEXT :
                    ENNZ.equals(locale) ?
                        ENGLISH_NEW_ZEALAND_TEXT :
                        FR.equals(locale) ?
                            FRENCH_TEXT :
                            null
            );
        }
    };

    @Test
    public void testFilterMatchesNone() {
        this.filterAndCheck(
            "Z",
            CONTEXT
        );
    }

    @Test
    public void testFilterMatchesSome() {
        this.filterAndCheck(
            "English",
            CONTEXT,
            LocaleHateosResource.with(
                LocaleTag.fromLocale(ENAU),
                ENGLISH_AUSTRALIA_TEXT
            ),
            LocaleHateosResource.with(
                LocaleTag.fromLocale(ENNZ),
                ENGLISH_NEW_ZEALAND_TEXT
            )
        );
    }

    @Test
    public void testFilterMatchesSome2() {
        this.filterAndCheck(
            FRENCH_TEXT,
            CONTEXT,
            LocaleHateosResource.with(
                LocaleTag.fromLocale(FR),
                FRENCH_TEXT
            )
        );
    }

    private void filterAndCheck(final String startsWith,
                                final LocaleContext context,
                                final LocaleHateosResource... expected) {
        this.filterAndCheck(
            startsWith,
            context,
            Sets.of(expected)
        );
    }

    private void filterAndCheck(final String startsWith,
                                final LocaleContext context,
                                final Set<LocaleHateosResource> expected) {
        this.checkEquals(
            expected,
            LocaleHateosResourceSet.filter(
                startsWith,
                context
            )
        );
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
            this.createSet(),
            "en\n" +
                "  English\n" +
                "en-AU\n" +
                "  English (Australia)\n" +
                "en-NZ\n" +
                "  English (New Zealand)\n"
        );
    }

    // json.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            this.createJsonNodeMarshallingValue(),
            "[\n" +
                "  {\n" +
                "    \"localeTag\": \"en\",\n" +
                "    \"text\": \"English\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"localeTag\": \"en-AU\",\n" +
                "    \"text\": \"English (Australia)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"localeTag\": \"en-NZ\",\n" +
                "    \"text\": \"English (New Zealand)\"\n" +
                "  }\n" +
                "]"
        );
    }

    @Test
    public void testUnmarshall() {
        this.unmarshallAndCheck(
            "[\n" +
                "  {\n" +
                "    \"localeTag\": \"en\",\n" +
                "    \"text\": \"English\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"localeTag\": \"en-AU\",\n" +
                "    \"text\": \"English (Australia)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"localeTag\": \"en-NZ\",\n" +
                "    \"text\": \"English (New Zealand)\"\n" +
                "  }\n" +
                "]",
            this.createJsonNodeMarshallingValue()
        );
    }

    @Test
    public void testMarshallUnmarshallAllAvailableLocaleHateosResources() {
        final SortedSet<LocaleHateosResource> locales = SortedSets.tree();
        locales.addAll(
            Arrays.stream(
                Locale.getAvailableLocales()
            ).map(LocaleHateosResource::fromLocale)
                .collect(Collectors.toList())
        );

        this.checkNotEquals(
            SortedSets.empty(),
            locales
        );

        this.marshallRoundTripTwiceAndCheck(
            LocaleHateosResourceSet.with(locales)
        );
    }

    @Override
    public LocaleHateosResourceSet unmarshall(final JsonNode jsonNode,
                                              final JsonNodeUnmarshallContext context) {
        return LocaleHateosResourceSet.unmarshall(
            jsonNode,
            context
        );
    }

    @Override
    public LocaleHateosResourceSet createJsonNodeMarshallingValue() {
        return this.createSet();
    }

    // class............................................................................................................

    @Override
    public Class<LocaleHateosResourceSet> type() {
        return LocaleHateosResourceSet.class;
    }
}