package walkingkooka.spreadsheet.server.locale;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.set.ImmutableSortedSetTesting;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Arrays;
import java.util.Locale;
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