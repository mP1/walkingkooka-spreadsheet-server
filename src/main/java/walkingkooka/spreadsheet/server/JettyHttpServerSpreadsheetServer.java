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

package walkingkooka.spreadsheet.server;

import javaemul.internal.annotations.GwtIncompatible;
import walkingkooka.Either;
import walkingkooka.collect.map.Maps;
import walkingkooka.math.Fraction;
import walkingkooka.net.HostAddress;
import walkingkooka.net.IpPort;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlScheme;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.apache.tika.ApacheTikas;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.net.http.server.WebFile;
import walkingkooka.net.http.server.WebFiles;
import walkingkooka.net.http.server.jetty.JettyHttpServer;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetName;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetRangeStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionContext;
import walkingkooka.tree.expression.function.UnknownExpressionFunctionException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Creates a {@link SpreadsheetServer} with memory stores using a Jetty server using the scheme/host/port from cmd line arguments.
 */
public final class JettyHttpServerSpreadsheetServer implements PublicStaticHelper {

    /**
     * Starts a server on the scheme/host/port passed as arguments, serving files from the current directory.
     */
    public static void main(final String[] args) throws Exception {
        switch (args.length) {
            case 0:
                throw new IllegalArgumentException("Missing scheme, host, port for jetty HttpServer");
            case 1:
                throw new IllegalArgumentException("Missing host, port for jetty HttpServer");
            case 2:
                throw new IllegalArgumentException("Missing port for jetty HttpServer");
            default:
                startJettyHttpServer(args);
                break;
        }
    }

    private static void startJettyHttpServer(final String[] args) throws Exception {
        final UrlScheme scheme;
        try {
            scheme = UrlScheme.with(args[0]);
        } catch (final IllegalArgumentException cause) {
            System.err.println("Invalid scheme: " + cause.getMessage());
            throw cause;
        }

        final HostAddress host;
        try {
            host = HostAddress.with(args[1]);
        } catch (final IllegalArgumentException cause) {
            System.err.println("Invalid hostname: " + cause.getMessage());
            throw cause;
        }
        final IpPort port;
        try {
            port = IpPort.with(Integer.parseInt(args[2]));
        } catch (final RuntimeException cause) {
            System.err.println("Invalid port: " + cause.getMessage());
            throw cause;
        }

        final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();

        final SpreadsheetServer server = SpreadsheetServer.with(scheme,
                host,
                port,
                createMetadata(metadataStore),
                fractioner(),
                idToFunctions(),
                idToRepository(Maps.concurrent(), storeRepositorySupplier(metadataStore)),
                fileServer(Paths.get(".")),
                jettyHttpServer(host, port));
        server.start();
    }

    /**
     * Creates a function which merges the given {@link Locale} and then saves it to the {@link SpreadsheetMetadataStore}.
     */
    private static Function<Optional<Locale>, SpreadsheetMetadata> createMetadata(final SpreadsheetMetadataStore store) {
        // TODO https://github.com/mP1/walkingkooka-spreadsheet-server/issues/134
        // JettyHttpServerSpreadsheetServer: retrieve user from context when creating initial SpreadsheetMetadata
        final EmailAddress user = EmailAddress.parse("user123@example.com");

        // https://github.com/mP1/walkingkooka-spreadsheet-server/issues/136
        // JettyHttpServerSpreadsheetServer: Accept default Locale parameter
        final Locale defaultLocale = Locale.forLanguageTag("EN-AU");

        // if a Locale is given load a Metadata with those defaults.
        return (userLocale) -> {
            final LocalDateTime now = LocalDateTime.now();

            return store.save(prepareInitialMetadata(
                    user,
                    now,
                    userLocale,
                    defaultLocale)
            );
        };
    }

    /**
     * The default name given to all empty spreadsheets
     */
    private final static SpreadsheetName DEFAULT_NAME = SpreadsheetName.with("Untitled");

    /**
     * Prepares and merges the default and user locale, loading defaults and more.
     */
    static SpreadsheetMetadata prepareInitialMetadata(final EmailAddress user,
                                                      final LocalDateTime now,
                                                      final Optional<Locale> userLocale,
                                                      final Locale defaultLocale) {
        final Locale localeOrDefault = userLocale.orElse(defaultLocale);
        return SpreadsheetMetadata.NON_LOCALE_DEFAULTS
                .set(SpreadsheetMetadataPropertyName.CREATOR, user)
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, now)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, user)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, now)
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_NAME, DEFAULT_NAME)
                .set(SpreadsheetMetadataPropertyName.LOCALE, localeOrDefault)
                .setDefaults(
                        SpreadsheetMetadata.EMPTY
                                .set(SpreadsheetMetadataPropertyName.LOCALE, localeOrDefault)
                                .loadFromLocale()
                                .set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 1900)
                                .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, ExpressionNumberKind.DOUBLE)
                                .set(SpreadsheetMetadataPropertyName.PRECISION, MathContext.DECIMAL32.getPrecision())
                                .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetPattern.parseTextFormatPattern("@"))
                                .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 20)
                );
    }

    private static Function<BigDecimal, Fraction> fractioner() {
        return (n) -> {
            throw new UnsupportedOperationException();
        };
    }

    private static Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> idToFunctions() {
        return (id) -> JettyHttpServerSpreadsheetServer.functions(id);
    }

    /**
     * TODO Implement a real function lookup, that only exposes functions that are enabled for a single spreadsheet.
     */
    private static Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>> functions(final SpreadsheetId id) {
        return (n) -> {
            throw new UnknownExpressionFunctionException(n);
        };
    }

    /**
     * Retrieves from the cache or lazily creates a {@link SpreadsheetStoreRepository} for the given {@link SpreadsheetId}.
     */
    private static Function<SpreadsheetId, SpreadsheetStoreRepository> idToRepository(final Map<SpreadsheetId, SpreadsheetStoreRepository> idToRepository,
                                                                                      final Supplier<SpreadsheetStoreRepository> repositoryFactory) {
        return (id) -> {
            SpreadsheetStoreRepository repository = idToRepository.get(id);
            if (null == repository) {
                repository = repositoryFactory.get();
                idToRepository.put(id, repository); // TODO add locks etc.
            }
            return repository;
        };
    }

    /**
     * Creates a new {@link SpreadsheetStoreRepository} on demand
     */
    private static Supplier<SpreadsheetStoreRepository> storeRepositorySupplier(final SpreadsheetMetadataStore metadataStore) {
        return () -> SpreadsheetStoreRepositories.basic(
                SpreadsheetCellStores.treeMap(),
                SpreadsheetExpressionReferenceStores.treeMap(),
                SpreadsheetGroupStores.treeMap(),
                SpreadsheetLabelStores.treeMap(),
                SpreadsheetExpressionReferenceStores.treeMap(),
                metadataStore,
                SpreadsheetRangeStores.treeMap(),
                SpreadsheetRangeStores.treeMap(),
                SpreadsheetUserStores.treeMap());
    }

    /**
     * Creates a file server which serves files from the given {@link Path path}.
     */
    private static Function<UrlPath, Either<WebFile, HttpStatus>> fileServer(final Path path) {
        return (p) -> {
            final Path file = Paths.get(path.toString(), p.value());
            return Files.isRegularFile(file) ?
                    Either.left(webFile(file)) :
                    Either.right(HttpStatusCode.NOT_FOUND.status());
        };
    }

    private static WebFile webFile(final Path file) {
        return WebFiles.file(file,
                ApacheTikas.fileContentTypeDetector(),
                (b) -> Optional.empty());
    }

    /**
     * Creates a {@link JettyHttpServer} given the given host and port.
     */
    @GwtIncompatible
    private static Function<BiConsumer<HttpRequest, HttpResponse>, HttpServer> jettyHttpServer(final HostAddress host,
                                                                                               final IpPort port) {
        return (handler) -> JettyHttpServer.with(host, port, handler);
    }

    private JettyHttpServerSpreadsheetServer() {
        throw new UnsupportedOperationException();
    }
}
