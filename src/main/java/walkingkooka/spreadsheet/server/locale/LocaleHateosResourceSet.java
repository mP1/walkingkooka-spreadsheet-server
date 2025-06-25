package walkingkooka.spreadsheet.server.locale;

import walkingkooka.collect.iterator.Iterators;
import walkingkooka.collect.set.ImmutableSortedSetDefaults;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.text.CharacterConstant;
import walkingkooka.text.HasText;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable {@link Set} containing unique {@link LocaleHateosResource locales}.
 */
public final class LocaleHateosResourceSet extends AbstractSet<LocaleHateosResource>
    implements ImmutableSortedSetDefaults<LocaleHateosResourceSet, LocaleHateosResource>,
    HasText,
    TreePrintable {

    /**
     * An empty {@link LocaleHateosResourceSet}.
     */
    public static final LocaleHateosResourceSet EMPTY = new LocaleHateosResourceSet(SortedSets.empty());

    /**
     * The comma which separates the CSV text representation.
     */
    public static final CharacterConstant SEPARATOR = CharacterConstant.COMMA;

    /**
     * Factory that creates {@link LocaleHateosResourceSet} with the given locales.
     */
    public static LocaleHateosResourceSet with(final SortedSet<LocaleHateosResource> locales) {
        return EMPTY.setElements(locales);
    }

    private static LocaleHateosResourceSet withCopy(final SortedSet<LocaleHateosResource> locales) {
        return locales.isEmpty() ?
            EMPTY :
            new LocaleHateosResourceSet(locales);
    }

    /**
     * Accepts a string of csv {@link LocaleHateosResource} with optional whitespace around locales ignored.
     */
    public static LocaleHateosResourceSet parse(final String text) {
        Objects.requireNonNull(text, "text");

        final SortedSet<LocaleHateosResource> locales = SortedSets.tree();

        final int length = text.length();
        final StringBuilder locale = new StringBuilder();

        for (int i = 0; i < length; i++) {
            final char c = text.charAt(i);
            switch (c) {
                case ',':
                    append(
                        locale,
                        locales
                    );
                    locale.setLength(0);
                    break;
                default:
                    locale.append(c);
                    break;
            }
        }

        append(
            locale,
            locales
        );

        return withCopy(locales);
    }

    private static void append(final StringBuilder b,
                               final SortedSet<LocaleHateosResource> locales) {
        final String text = b.toString()
            .trim();
        if (false == text.isEmpty()) {
            locales.add(
                LocaleHateosResource.parse(text)
            );
        }
    }

    private LocaleHateosResourceSet(final SortedSet<LocaleHateosResource> locales) {
        super();
        this.locales = locales;
    }

    // ImmutableSortedSet...............................................................................................

    @Override
    public Iterator<LocaleHateosResource> iterator() {
        return Iterators.readOnly(
            this.locales.iterator()
        );
    }

    @Override
    public int size() {
        return this.locales.size();
    }

    @Override
    public Comparator<LocaleHateosResource> comparator() {
        return null;
    }

    @Override
    public LocaleHateosResourceSet subSet(LocaleHateosResource from,
                                          LocaleHateosResource to) {
        return withCopy(
            this.locales.subSet(
                from,
                to
            )
        );
    }

    @Override
    public LocaleHateosResourceSet headSet(LocaleHateosResource locale) {
        return withCopy(
            this.locales.headSet(locale)
        );
    }

    @Override
    public LocaleHateosResourceSet tailSet(LocaleHateosResource locale) {
        return withCopy(
            this.locales.tailSet(locale)
        );
    }

    @Override
    public LocaleHateosResource first() {
        return this.locales.first();
    }

    @Override
    public LocaleHateosResource last() {
        return this.locales.last();
    }

    @Override
    public SortedSet<LocaleHateosResource> toSet() {
        return new TreeSet<>(this.locales);
    }

    @Override
    public LocaleHateosResourceSet setElements(final SortedSet<LocaleHateosResource> locales) {
        final LocaleHateosResourceSet LocaleHateosResourceSet;

        if (locales instanceof LocaleHateosResourceSet) {
            LocaleHateosResourceSet = (LocaleHateosResourceSet) locales;
        } else {
            final TreeSet<LocaleHateosResource> copy = new TreeSet<>(
                Objects.requireNonNull(locales, "locales")
            );
            LocaleHateosResourceSet = this.locales.equals(copy) ?
                this :
                withCopy(copy);
        }

        return LocaleHateosResourceSet;
    }

    private SortedSet<LocaleHateosResource> locales;

    @Override
    public void elementCheck(final LocaleHateosResource locale) {
        Objects.requireNonNull(locale, "locale");
    }

    // HasText..........................................................................................................

    @Override
    public String text() {
        return SEPARATOR.toSeparatedString(
            this,
            LocaleHateosResource::text
        );
    }

    // TreePrintable....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        for (final LocaleHateosResource locale : this) {
            locale.printTree(printer);
        }
    }

    // Json.............................................................................................................

    /**
     * Returns a CSV string with locale tags separated by commas.
     */
    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.string(
            this.text()
        );
    }

    static void register() {
        // helps force registry of json marshaller
    }

    static LocaleHateosResourceSet unmarshall(final JsonNode node,
                                              final JsonNodeUnmarshallContext context) {
        return parse(
            node.stringOrFail()
        );
    }

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(LocaleHateosResourceSet.class),
            LocaleHateosResourceSet::unmarshall,
            LocaleHateosResourceSet::marshall,
            LocaleHateosResourceSet.class
        );
    }
}
