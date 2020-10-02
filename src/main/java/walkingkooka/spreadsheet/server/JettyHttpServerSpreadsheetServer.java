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
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetRangeStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetReferenceStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.FunctionExpressionName;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
        final SpreadsheetMetadata metadataWithDefaults = SpreadsheetMetadata.NON_LOCALE_DEFAULTS
                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("en"));

        return (locale) ->
                store.save(locale.map(l -> metadataWithDefaults.set(SpreadsheetMetadataPropertyName.LOCALE, l).loadFromLocale()).orElse(metadataWithDefaults));

    }

    private static Function<BigDecimal, Fraction> fractioner() {
        return (n) -> {
            throw new UnsupportedOperationException();
        };
    }

    private static Function<SpreadsheetId, BiFunction<FunctionExpressionName, List<Object>, Object>> idToFunctions() {
        return (id) -> JettyHttpServerSpreadsheetServer::functions;
    }

    /**
     * TODO Implement a real function lookup, that only exposes functions that are enabled for a single spreadsheet.
     */
    private static Object functions(final FunctionExpressionName functionName, final List<Object> parameters) {
        throw new UnsupportedOperationException("Unknown function: " + functionName + "(" + parameters.stream().map(Object::toString).collect(Collectors.joining(",")) + ")");
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
                SpreadsheetReferenceStores.treeMap(),
                SpreadsheetGroupStores.treeMap(),
                SpreadsheetLabelStores.treeMap(),
                SpreadsheetReferenceStores.treeMap(),
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
