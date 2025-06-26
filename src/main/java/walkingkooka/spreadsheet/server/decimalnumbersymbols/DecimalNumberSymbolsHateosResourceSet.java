package walkingkooka.spreadsheet.server.decimalnumbersymbols;

import walkingkooka.collect.iterator.Iterators;
import walkingkooka.collect.set.ImmutableSortedSetDefaults;
import walkingkooka.collect.set.SortedSets;
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
 * An immutable {@link Set} containing unique {@link DecimalNumberSymbolsHateosResource resources}.
 */
public final class DecimalNumberSymbolsHateosResourceSet extends AbstractSet<DecimalNumberSymbolsHateosResource>
    implements ImmutableSortedSetDefaults<DecimalNumberSymbolsHateosResourceSet, DecimalNumberSymbolsHateosResource>,
    TreePrintable {

    /**
     * An empty {@link DecimalNumberSymbolsHateosResourceSet}.
     */
    public static final DecimalNumberSymbolsHateosResourceSet EMPTY = new DecimalNumberSymbolsHateosResourceSet(SortedSets.empty());

    /**
     * Factory that creates {@link DecimalNumberSymbolsHateosResourceSet} with the given resources.
     */
    public static DecimalNumberSymbolsHateosResourceSet with(final SortedSet<DecimalNumberSymbolsHateosResource> resources) {
        return EMPTY.setElements(resources);
    }

    private static DecimalNumberSymbolsHateosResourceSet withCopy(final SortedSet<DecimalNumberSymbolsHateosResource> resources) {
        return resources.isEmpty() ?
            EMPTY :
            new DecimalNumberSymbolsHateosResourceSet(resources);
    }

    private DecimalNumberSymbolsHateosResourceSet(final SortedSet<DecimalNumberSymbolsHateosResource> resources) {
        super();
        this.resources = resources;
    }

    // ImmutableSortedSet...............................................................................................

    @Override
    public Iterator<DecimalNumberSymbolsHateosResource> iterator() {
        return Iterators.readOnly(
            this.resources.iterator()
        );
    }

    @Override
    public int size() {
        return this.resources.size();
    }

    @Override
    public Comparator<DecimalNumberSymbolsHateosResource> comparator() {
        return null;
    }

    @Override
    public DecimalNumberSymbolsHateosResourceSet subSet(final DecimalNumberSymbolsHateosResource from,
                                                        final DecimalNumberSymbolsHateosResource to) {
        return withCopy(
            this.resources.subSet(
                from,
                to
            )
        );
    }

    @Override
    public DecimalNumberSymbolsHateosResourceSet headSet(final DecimalNumberSymbolsHateosResource resource) {
        return withCopy(
            this.resources.headSet(resource)
        );
    }

    @Override
    public DecimalNumberSymbolsHateosResourceSet tailSet(final DecimalNumberSymbolsHateosResource resource) {
        return withCopy(
            this.resources.tailSet(resource)
        );
    }

    @Override
    public DecimalNumberSymbolsHateosResource first() {
        return this.resources.first();
    }

    @Override
    public DecimalNumberSymbolsHateosResource last() {
        return this.resources.last();
    }

    @Override
    public SortedSet<DecimalNumberSymbolsHateosResource> toSet() {
        return new TreeSet<>(this.resources);
    }

    @Override
    public DecimalNumberSymbolsHateosResourceSet setElements(final SortedSet<DecimalNumberSymbolsHateosResource> resources) {
        final DecimalNumberSymbolsHateosResourceSet decimalNumberSymbolsHateosResourceSet;

        if (resources instanceof DecimalNumberSymbolsHateosResourceSet) {
            decimalNumberSymbolsHateosResourceSet = (DecimalNumberSymbolsHateosResourceSet) resources;
        } else {
            final TreeSet<DecimalNumberSymbolsHateosResource> copy = new TreeSet<>(
                Objects.requireNonNull(resources, "resources")
            );
            decimalNumberSymbolsHateosResourceSet = this.resources.equals(copy) ?
                this :
                withCopy(copy);
        }

        return decimalNumberSymbolsHateosResourceSet;
    }

    private final SortedSet<DecimalNumberSymbolsHateosResource> resources;

    @Override
    public void elementCheck(final DecimalNumberSymbolsHateosResource resource) {
        Objects.requireNonNull(resource, "resource");
    }

    // TreePrintable....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        for (final DecimalNumberSymbolsHateosResource resource : this) {
            resource.printTree(printer);
        }
    }

    // Json.............................................................................................................

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallCollection(this);
    }

    static DecimalNumberSymbolsHateosResourceSet unmarshall(final JsonNode node,
                                                            final JsonNodeUnmarshallContext context) {
        return with(
            new TreeSet<>(
                context.unmarshallSet(
                    node,
                    DecimalNumberSymbolsHateosResource.class
                )
            )
        );
    }

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(DecimalNumberSymbolsHateosResourceSet.class),
            DecimalNumberSymbolsHateosResourceSet::unmarshall,
            DecimalNumberSymbolsHateosResourceSet::marshall,
            DecimalNumberSymbolsHateosResourceSet.class
        );
    }
}
