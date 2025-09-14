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

package walkingkooka.spreadsheet.server.datetimesymbols;

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
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable {@link Set} containing unique {@link DateTimeSymbolsHateosResource resources}.
 */
public final class DateTimeSymbolsHateosResourceSet extends AbstractSet<DateTimeSymbolsHateosResource>
    implements ImmutableSortedSetDefaults<DateTimeSymbolsHateosResourceSet, DateTimeSymbolsHateosResource>,
    TreePrintable {

    /**
     * An empty {@link DateTimeSymbolsHateosResourceSet}.
     */
    public static final DateTimeSymbolsHateosResourceSet EMPTY = new DateTimeSymbolsHateosResourceSet(SortedSets.empty());

    /**
     * Factory that creates {@link DateTimeSymbolsHateosResourceSet} with the given resources.
     */
    public static DateTimeSymbolsHateosResourceSet with(final Collection<DateTimeSymbolsHateosResource> resources) {
        return EMPTY.setElements(resources);
    }

    private static DateTimeSymbolsHateosResourceSet withCopy(final SortedSet<DateTimeSymbolsHateosResource> resources) {
        return resources.isEmpty() ?
            EMPTY :
            new DateTimeSymbolsHateosResourceSet(resources);
    }

    private DateTimeSymbolsHateosResourceSet(final SortedSet<DateTimeSymbolsHateosResource> resources) {
        super();
        this.resources = resources;
    }

    // ImmutableSortedSet...............................................................................................

    @Override
    public Iterator<DateTimeSymbolsHateosResource> iterator() {
        return Iterators.readOnly(
            this.resources.iterator()
        );
    }

    @Override
    public int size() {
        return this.resources.size();
    }

    @Override
    public Comparator<DateTimeSymbolsHateosResource> comparator() {
        return null;
    }

    @Override
    public DateTimeSymbolsHateosResourceSet subSet(final DateTimeSymbolsHateosResource from,
                                                   final DateTimeSymbolsHateosResource to) {
        return withCopy(
            this.resources.subSet(
                from,
                to
            )
        );
    }

    @Override
    public DateTimeSymbolsHateosResourceSet headSet(final DateTimeSymbolsHateosResource resource) {
        return withCopy(
            this.resources.headSet(resource)
        );
    }

    @Override
    public DateTimeSymbolsHateosResourceSet tailSet(final DateTimeSymbolsHateosResource resource) {
        return withCopy(
            this.resources.tailSet(resource)
        );
    }

    @Override
    public DateTimeSymbolsHateosResource first() {
        return this.resources.first();
    }

    @Override
    public DateTimeSymbolsHateosResource last() {
        return this.resources.last();
    }

    @Override
    public SortedSet<DateTimeSymbolsHateosResource> toSet() {
        return new TreeSet<>(this.resources);
    }

    @Override
    public DateTimeSymbolsHateosResourceSet setElements(final Collection<DateTimeSymbolsHateosResource> resources) {
        final DateTimeSymbolsHateosResourceSet dateTimeSymbolsHateosResourceSet;

        if (resources instanceof DateTimeSymbolsHateosResourceSet) {
            dateTimeSymbolsHateosResourceSet = (DateTimeSymbolsHateosResourceSet) resources;
        } else {
            final TreeSet<DateTimeSymbolsHateosResource> copy = new TreeSet<>(
                Objects.requireNonNull(resources, "resources")
            );
            dateTimeSymbolsHateosResourceSet = this.resources.equals(copy) ?
                this :
                withCopy(copy);
        }

        return dateTimeSymbolsHateosResourceSet;
    }

    private final SortedSet<DateTimeSymbolsHateosResource> resources;

    @Override
    public void elementCheck(final DateTimeSymbolsHateosResource resource) {
        Objects.requireNonNull(resource, "resource");
    }

    // TreePrintable....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        for (final DateTimeSymbolsHateosResource resource : this) {
            resource.printTree(printer);
        }
    }

    // Json.............................................................................................................

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallCollection(this);
    }

    static DateTimeSymbolsHateosResourceSet unmarshall(final JsonNode node,
                                                       final JsonNodeUnmarshallContext context) {
        return with(
            new TreeSet<>(
                context.unmarshallSet(
                    node,
                    DateTimeSymbolsHateosResource.class
                )
            )
        );
    }

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(DateTimeSymbolsHateosResourceSet.class),
            DateTimeSymbolsHateosResourceSet::unmarshall,
            DateTimeSymbolsHateosResourceSet::marshall,
            DateTimeSymbolsHateosResourceSet.class
        );
    }
}
