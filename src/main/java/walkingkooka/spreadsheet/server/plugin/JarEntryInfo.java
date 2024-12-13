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

package walkingkooka.spreadsheet.server.plugin;

import walkingkooka.Cast;
import walkingkooka.ToStringBuilder;
import walkingkooka.ToStringBuilderOption;
import walkingkooka.naming.PathSeparator;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonObject;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * An individual file or directory entry within a JAR file.
 */
public final class JarEntryInfo implements TreePrintable {

    public final static PathSeparator SEPARATOR = PathSeparator.requiredAtStart('/');

    public static JarEntryInfo with(final JarEntryInfoName name,
                                    final OptionalLong size,
                                    final OptionalLong compressedSize,
                                    final OptionalInt method,
                                    final OptionalLong crc,
                                    final Optional<LocalDateTime> create,
                                    final Optional<LocalDateTime> lastModified) {
        return new JarEntryInfo(
                Objects.requireNonNull(name, "name"),
                checkPositiveNumber(size, "size"),
                checkPositiveNumber(compressedSize, "compressedSize"),
                checkPositiveNumber(method, "method"),
                checkPositiveNumber(crc, "crc"),
                Objects.requireNonNull(create, "create"),
                Objects.requireNonNull(lastModified, "lastModified")
        );
    }

    private static OptionalInt checkPositiveNumber(final OptionalInt value,
                                                   final String label) {
        if (value.isPresent()) {
            final int intValue = value.getAsInt();
            if (intValue < 0) {
                throw new IllegalArgumentException("Invalid " + label + " " + intValue + " < 0");
            }
        }
        return value;
    }

    private static OptionalLong checkPositiveNumber(final OptionalLong value,
                                                    final String label) {
        if (value.isPresent()) {
            final long longValue = value.getAsLong();
            if (longValue < 0) {
                throw new IllegalArgumentException("Invalid " + label + " " + longValue + " < 0");
            }
        }
        return value;
    }

    private JarEntryInfo(final JarEntryInfoName name,
                         final OptionalLong size,
                         final OptionalLong compressedSize,
                         final OptionalInt method,
                         final OptionalLong crc,
                         final Optional<LocalDateTime> create,
                         final Optional<LocalDateTime> lastModified) {
        this.name = name;
        this.size = size;
        this.compressedSize = compressedSize;
        this.method = method;
        this.crc = crc;
        this.create = create;
        this.lastModified = lastModified;
    }

    public JarEntryInfoName name() {
        return this.name;
    }

    private final JarEntryInfoName name;

    public boolean isDirectory() {
        return this.name.isDirectory();
    }

    public OptionalLong size() {
        return this.size;
    }

    private final OptionalLong size;

    public OptionalLong compressedSize() {
        return this.compressedSize;
    }

    private final OptionalLong compressedSize;

    public OptionalInt method() {
        return this.method;
    }

    private final OptionalInt method;

    public OptionalLong crc() {
        return this.crc;
    }

    private final OptionalLong crc;

    public Optional<LocalDateTime> create() {
        return this.create;
    }

    private final Optional<LocalDateTime> create;

    public Optional<LocalDateTime> lastModified() {
        return this.lastModified;
    }

    private final Optional<LocalDateTime> lastModified;

    // HashCodeEqualsDefined..........................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(
                this.name,
                this.size,
                this.compressedSize,
                this.method,
                this.crc,
                this.create,
                this.lastModified
        );
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
                other instanceof JarEntryInfo &&
                        this.equals0(Cast.to(other));
    }

    private boolean equals0(final JarEntryInfo other) {
        return this.name.equals(other.name) &&
                this.size.equals(other.size) &&
                this.compressedSize.equals(other.compressedSize) &&
                this.method.equals(other.method) &&
                this.crc.equals(other.crc) &&
                this.create.equals(other.create) &&
                this.lastModified.equals(other.lastModified);
    }

    @Override
    public String toString() {
        return ToStringBuilder.empty()
                .disable(ToStringBuilderOption.SKIP_IF_DEFAULT_VALUE)
                .value(this.name.value()) // String value will add quotes
                .value(this.isDirectory() ? "(directory)" : "(file)")
                .label("size")
                .value(this.size)
                .label("compressedSize")
                .value(this.compressedSize)
                .label("method")
                .value(this.method)
                .label("crc")
                .value(this.crc)
                .label("create")
                .value(this.create)
                .label("lastModified")
                .value(this.lastModified)
                .build();
    }

    // JsonNodeContext...................................................................................................

    /**
     * Factory that creates a {@link JarEntryInfo} parse a {@link JsonNode}.
     */
    static JarEntryInfo unmarshall(final JsonNode node,
                                   final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(node, "node");

        JarEntryInfoName name = null;
        Long size = null;
        Long compressedSize = null;
        Integer method = null;
        Long crc = null;
        LocalDateTime create = null;
        LocalDateTime lastModified = null;

        for (final JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName jsonPropertyName = child.name();
            switch (jsonPropertyName.value()) {
                case NAME_PROPERTY_STRING:
                    name = context.unmarshall(
                            child,
                            JarEntryInfoName.class
                    );
                    break;
                case SIZE_PROPERTY_STRING:
                    size = context.unmarshall(
                            child,
                            Long.class
                    );
                    break;
                case COMPRESSED_SIZE_PROPERTY_STRING:
                    compressedSize = context.unmarshall(
                            child,
                            Long.class
                    );
                    break;
                case METHOD_PROPERTY_STRING:
                    method = context.unmarshall(
                            child,
                            Integer.class
                    );
                    break;
                case CRC_PROPERTY_STRING:
                    crc = context.unmarshall(
                            child,
                            Long.class
                    );
                    break;
                case CREATE_PROPERTY_STRING:
                    create = context.unmarshall(
                            child,
                            LocalDateTime.class
                    );
                    break;
                case LAST_MODIFIED_PROPERTY_STRING:
                    lastModified = context.unmarshall(
                            child,
                            LocalDateTime.class
                    );
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(
                            jsonPropertyName,
                            node
                    );
                    break;
            }
        }

        if (null == name) {
            JsonNodeUnmarshallContext.requiredPropertyMissing(NAME_PROPERTY, node);
        }

        return with(
                name,
                null != size ?
                        OptionalLong.of(size.longValue()) :
                        OptionalLong.empty(),
                null != compressedSize ?
                        OptionalLong.of(compressedSize.longValue()) :
                        OptionalLong.empty(),
                null != method ?
                        OptionalInt.of(method.intValue()) :
                        OptionalInt.empty(),
                null != crc ?
                        OptionalLong.of(crc.longValue()) :
                        OptionalLong.empty(),
                Optional.ofNullable(create),
                Optional.ofNullable(lastModified)
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        JsonObject json = JsonNode.object()
                .set(
                        NAME_PROPERTY,
                        context.marshall(this.name)
                );

        {
            final OptionalLong size = this.size;
            if (size.isPresent()) {
                json = json.set(
                        SIZE_PROPERTY,
                        context.marshall(size.getAsLong())
                );
            }
        }

        {
            final OptionalLong compressedSize = this.compressedSize;
            if (compressedSize.isPresent()) {
                json = json.set(
                        COMPRESSED_SIZE_PROPERTY,
                        context.marshall(compressedSize.getAsLong())
                );
            }
        }

        {
            final OptionalInt method = this.method;
            if (method.isPresent()) {
                json = json.set(
                        METHOD_PROPERTY,
                        context.marshall(method.getAsInt())
                );
            }
        }

        {
            final OptionalLong crc = this.crc;
            if (crc.isPresent()) {
                json = json.set(
                        CRC_PROPERTY,
                        context.marshall(crc.getAsLong())
                );
            }
        }

        {
            final LocalDateTime create = this.create.orElse(null);
            if (null != create) {
                json = json.set(
                        CREATE_PROPERTY,
                        context.marshall(create)
                );
            }
        }
        {
            final LocalDateTime lastModified = this.lastModified.orElse(null);
            if (null != lastModified) {
                json = json.set(
                        LAST_MODIFIED_PROPERTY,
                        context.marshall(lastModified)
                );
            }
        }

        return json;
    }

    private final static String NAME_PROPERTY_STRING = "name";

    private final static String SIZE_PROPERTY_STRING = "size";

    private final static String COMPRESSED_SIZE_PROPERTY_STRING = "compressedSize";

    private final static String METHOD_PROPERTY_STRING = "method";

    private final static String CRC_PROPERTY_STRING = "crc";

    private final static String CREATE_PROPERTY_STRING = "create";

    private final static String LAST_MODIFIED_PROPERTY_STRING = "lastModified";

    final static JsonPropertyName NAME_PROPERTY = JsonPropertyName.with(NAME_PROPERTY_STRING);

    final static JsonPropertyName SIZE_PROPERTY = JsonPropertyName.with(SIZE_PROPERTY_STRING);

    final static JsonPropertyName COMPRESSED_SIZE_PROPERTY = JsonPropertyName.with(COMPRESSED_SIZE_PROPERTY_STRING);

    final static JsonPropertyName METHOD_PROPERTY = JsonPropertyName.with(METHOD_PROPERTY_STRING);

    final static JsonPropertyName CRC_PROPERTY = JsonPropertyName.with(CRC_PROPERTY_STRING);

    final static JsonPropertyName CREATE_PROPERTY = JsonPropertyName.with(CREATE_PROPERTY_STRING);

    final static JsonPropertyName LAST_MODIFIED_PROPERTY = JsonPropertyName.with(LAST_MODIFIED_PROPERTY_STRING);

    static {
        LocalDateTime.now();

        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(JarEntryInfo.class),
                JarEntryInfo::unmarshall,
                JarEntryInfo::marshall,
                JarEntryInfo.class
        );
    }

    // TreePrintable....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        printer.print(this.name.value());
        printer.println(this.isDirectory() ? " (directory)" : " (file)");

        printer.indent();
        {
            {
                final OptionalLong size = this.size;
                if (size.isPresent()) {
                    printer.println("size: " + size.getAsLong());
                }
            }

            {
                final OptionalLong compressedSize = this.compressedSize;
                if (compressedSize.isPresent()) {
                    printer.println("compressedSize: " + compressedSize.getAsLong());
                }
            }

            {
                final OptionalInt method = this.method;
                if (method.isPresent()) {
                    printer.println("method: " + method.getAsInt());
                }
            }

            {
                final OptionalLong crc = this.crc;
                if (crc.isPresent()) {
                    printer.println("crc: " + crc.getAsLong());
                }
            }

            {
                Optional<LocalDateTime> create = this.create;
                if (create.isPresent()) {
                    printer.println("create: " + create.get());
                }
            }
            {
                Optional<LocalDateTime> lastModified = this.lastModified;
                if (lastModified.isPresent()) {
                    printer.println("lastModified: " + lastModified.get());
                }
            }
        }
        printer.outdent();
    }
}
