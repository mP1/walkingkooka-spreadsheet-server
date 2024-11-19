package test;

import com.google.gwt.junit.client.GWTTestCase;

import walkingkooka.Cast;
import walkingkooka.collect.list.Lists;
import walkingkooka.convert.Converters;
import walkingkooka.convert.provider.ConverterProviders;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.net.HostAddress;
import walkingkooka.net.IpPort;
import walkingkooka.net.Url;
import walkingkooka.net.UrlScheme;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.Accept;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProviders;
import walkingkooka.spreadsheet.export.SpreadsheetExporterProviders;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviders;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.importer.SpreadsheetImporterProviders;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.parser.SpreadsheetParserProviders;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.server.SpreadsheetHttpServer;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionAliasSet;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProviders;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;
import walkingkooka.tree.text.Length;
import walkingkooka.tree.text.TextStyle;
import walkingkooka.tree.text.TextStylePropertyName;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

@walkingkooka.j2cl.locale.LocaleAware
public class TestGwtTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "test.Test";
    }

    public void testAssertEquals() {
        assertEquals(
                1,
                1
        );
    }

    public void testCreateSpreadsheet() {
        final TestHttpServer httpServer = new TestHttpServer();
        spreadsheetHttpServer(httpServer);

        final HttpResponse response = HttpResponses.recording();

        httpServer.handler.handle(
                HttpRequests.value(
                        HttpMethod.POST,
                        HttpTransport.SECURED,
                        Url.parseRelative("/api/spreadsheet"),
                        HttpProtocolVersion.VERSION_1_0,
                        HttpEntity.EMPTY.addHeader(
                                HttpHeaderName.ACCEPT,
                                Accept.with(
                                        Lists.of(MediaType.APPLICATION_JSON)
                                )
                        )
                ),
                response
        );

        checkEquals(
                Optional.of(
                        HttpStatusCode.CREATED.status()
                ),
                response.status(),
                "POST /api/spreadsheet status"
        );

        checkEquals(
                HttpEntity.EMPTY.setContentType(
                        MediaType.APPLICATION_JSON.setCharset(CharsetName.UTF_8)
                ).addHeader(
                        HttpHeaderName.with("X-Content-Type-Name"),
                        Cast.to(SpreadsheetMetadata.class.getSimpleName())
                ).setBodyText(
                        "{\n" +
                                "  \"spreadsheet-id\": \"1\",\n" +
                                "  \"cell-character-width\": 10,\n" +
                                "  \"create-date-time\": \"1999-12-31T12:58:59\",\n" +
                                "  \"creator\": \"creator@example.com\",\n" +
                                "  \"currency-symbol\": \"$AUD\",\n" +
                                "  \"date-formatter\": \"date-format-pattern DD/MM/YYYY\",\n" +
                                "  \"date-parser\": \"date-parse-pattern DD/MM/YYYYDDMMYYYY\",\n" +
                                "  \"date-time-formatter\": \"date-time-format-pattern DD/MM/YYYY hh:mm\",\n" +
                                "  \"date-time-offset\": \"0\",\n" +
                                "  \"date-time-parser\": \"date-time-parse-pattern DD/MM/YYYY hh:mmDDMMYYYYHHMMDDMMYYYY HHMM\",\n" +
                                "  \"decimal-separator\": \".\",\n" +
                                "  \"default-year\": 1900,\n" +
                                "  \"exponent-symbol\": \"E\",\n" +
                                "  \"expression-number-kind\": \"DOUBLE\",\n" +
                                "  \"formula-converter\": \"general\",\n" +
                                "  \"formula-functions\": \"\",\n" +
                                "  \"frozen-columns\": \"A:B\",\n" +
                                "  \"frozen-rows\": \"1:2\",\n" +
                                "  \"general-number-format-digit-count\": 8,\n" +
                                "  \"group-separator\": \",\",\n" +
                                "  \"locale\": \"en-AU\",\n" +
                                "  \"modified-by\": \"modified@example.com\",\n" +
                                "  \"modified-date-time\": \"1999-12-31T12:58:59\",\n" +
                                "  \"negative-sign\": \"-\",\n" +
                                "  \"number-formatter\": \"number-format-pattern #0.0\",\n" +
                                "  \"number-parser\": \"number-parse-pattern #\",\n" +
                                "  \"percentage-symbol\": \"%\",\n" +
                                "  \"positive-sign\": \"+\",\n" +
                                "  \"precision\": 123,\n" +
                                "  \"rounding-mode\": \"FLOOR\",\n" +
                                "  \"style\": {\n" +
                                "    \"height\": \"50px\",\n" +
                                "    \"width\": \"50px\"\n" +
                                "  },\n" +
                                "  \"text-formatter\": \"text-format-pattern @@\",\n" +
                                "  \"time-formatter\": \"time-format-pattern hh:mm\",\n" +
                                "  \"time-parser\": \"time-parse-pattern hh:mmhh:mm:ss.000\",\n" +
                                "  \"two-digit-year\": 31,\n" +
                                "  \"value-separator\": \",\"\n" +
                                "}"
                ).setContentLength(),
                response.entity(),
                "POST /api/spreadsheet response"
        );
    }

    private static SpreadsheetHttpServer spreadsheetHttpServer(final TestHttpServer httpServer) {
        final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();

        final LocalDateTime now = LocalDateTime.of(1999, 12, 31, 12, 58, 59);

        final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.CELL_CHARACTER_WIDTH, 10)
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, now)
                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("creator@example.com"))
                .set(SpreadsheetMetadataPropertyName.CURRENCY_SYMBOL, "$AUD")
                .set(SpreadsheetMetadataPropertyName.DATE_FORMATTER, SpreadsheetPattern.parseDateFormatPattern("DD/MM/YYYY").spreadsheetFormatterSelector())
                .set(SpreadsheetMetadataPropertyName.DATE_PARSER, SpreadsheetPattern.parseDateParsePattern("DD/MM/YYYYDDMMYYYY").spreadsheetParserSelector())
                .set(SpreadsheetMetadataPropertyName.DATETIME_OFFSET, Converters.JAVA_EPOCH_OFFSET)
                .set(SpreadsheetMetadataPropertyName.DATE_TIME_FORMATTER, SpreadsheetPattern.parseDateTimeFormatPattern("DD/MM/YYYY hh:mm").spreadsheetFormatterSelector())
                .set(SpreadsheetMetadataPropertyName.DATE_TIME_PARSER, SpreadsheetPattern.parseDateTimeParsePattern("DD/MM/YYYY hh:mmDDMMYYYYHHMMDDMMYYYY HHMM").spreadsheetParserSelector())
                .set(SpreadsheetMetadataPropertyName.DECIMAL_SEPARATOR, '.')
                .set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 1900)
                .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, ExpressionNumberKind.DOUBLE)
                .set(SpreadsheetMetadataPropertyName.EXPONENT_SYMBOL, "E")
                .set(SpreadsheetMetadataPropertyName.FORMULA_CONVERTER, ConverterSelector.parse("general"))
                .set(SpreadsheetMetadataPropertyName.FORMULA_FUNCTIONS, ExpressionFunctionAliasSet.parse(""))
                .set(SpreadsheetMetadataPropertyName.FROZEN_COLUMNS, SpreadsheetSelection.parseColumnRange("A:B"))
                .set(SpreadsheetMetadataPropertyName.FROZEN_ROWS, SpreadsheetSelection.parseRowRange("1:2"))
                .set(SpreadsheetMetadataPropertyName.GROUP_SEPARATOR, ',')
                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, EmailAddress.parse("modified@example.com"))
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, now)
                .set(SpreadsheetMetadataPropertyName.NEGATIVE_SIGN, '-')
                .set(SpreadsheetMetadataPropertyName.NUMBER_FORMATTER, SpreadsheetPattern.parseNumberFormatPattern("#0.0").spreadsheetFormatterSelector())
                .set(SpreadsheetMetadataPropertyName.GENERAL_NUMBER_FORMAT_DIGIT_COUNT, 8)
                .set(SpreadsheetMetadataPropertyName.NUMBER_PARSER, SpreadsheetPattern.parseNumberParsePattern("#").spreadsheetParserSelector())
                .set(SpreadsheetMetadataPropertyName.PERCENTAGE_SYMBOL, '%')
                .set(SpreadsheetMetadataPropertyName.POSITIVE_SIGN, '+')
                .set(SpreadsheetMetadataPropertyName.PRECISION, 123)
                .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.FLOOR)
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(123))
                .set(
                        SpreadsheetMetadataPropertyName.STYLE,
                        TextStyle.EMPTY.set(TextStylePropertyName.WIDTH, Length.pixel(50.0))
                                .set(TextStylePropertyName.HEIGHT, Length.pixel(50.0)))
                .set(SpreadsheetMetadataPropertyName.TEXT_FORMATTER, SpreadsheetPattern.parseTextFormatPattern("@@").spreadsheetFormatterSelector())
                .set(SpreadsheetMetadataPropertyName.TIME_FORMATTER, SpreadsheetPattern.parseTimeFormatPattern("hh:mm").spreadsheetFormatterSelector())
                .set(SpreadsheetMetadataPropertyName.TIME_PARSER, SpreadsheetPattern.parseTimeParsePattern("hh:mmhh:mm:ss.000").spreadsheetParserSelector())
                .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 31)
                .set(SpreadsheetMetadataPropertyName.VALUE_SEPARATOR, ',');

        final SpreadsheetStoreRepository repo = SpreadsheetStoreRepositories.basic(
                SpreadsheetCellStores.treeMap(),
                SpreadsheetExpressionReferenceStores.treeMap(),
                SpreadsheetColumnStores.treeMap(),
                SpreadsheetGroupStores.fake(),
                SpreadsheetLabelStores.treeMap(),
                SpreadsheetExpressionReferenceStores.treeMap(),
                SpreadsheetMetadataStores.fake(),
                SpreadsheetCellRangeStores.treeMap(),
                SpreadsheetCellRangeStores.treeMap(),
                SpreadsheetRowStores.treeMap(),
                SpreadsheetUserStores.fake()
        );

        final AtomicLong nextId = new AtomicLong();

        final SpreadsheetFormatterProvider spreadsheetFormatterProvider = SpreadsheetFormatterProviders.spreadsheetFormatPattern();

        return SpreadsheetHttpServer.with(
                UrlScheme.HTTPS,
                HostAddress.with("example.com"),
                IpPort.HTTPS,
                Indentation.SPACES2,
                LineEnding.NL,
                () -> now, // now
                SpreadsheetProviders.fake(),
                (l) -> metadata.set(
                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                        SpreadsheetId.with(nextId.incrementAndGet())
                ),
                metadataStore,
                JsonNodeMarshallUnmarshallContexts.basic(
                        JsonNodeMarshallContexts.basic(),
                        JsonNodeUnmarshallContexts.basic(
                                metadata.expressionNumberKind(),
                                metadata.mathContext()
                        )
                ),
                (id) -> metadata.spreadsheetProvider(
                        SpreadsheetProviders.basic(
                                ConverterProviders.converters(),
                                ExpressionFunctionProviders.expressionFunctions(),
                                SpreadsheetComparatorProviders.spreadsheetComparators(),
                                SpreadsheetExporterProviders.spreadsheetExport(),
                                spreadsheetFormatterProvider,
                                SpreadsheetImporterProviders.spreadsheetImport(),
                                SpreadsheetParserProviders.spreadsheetParsePattern(
                                        spreadsheetFormatterProvider
                                )
                        )
                ),
                (id) -> repo, // spreadsheetIdToStoreRepository
                (url) -> {
                    throw new UnsupportedOperationException(); // fileServer
                },
                (handler) -> {
                    httpServer.handler = handler;
                    return httpServer;
                }
        );
    }

    static final class TestHttpServer implements HttpServer {

        TestHttpServer() {
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        HttpHandler handler;
    }

    private static void checkEquals(final Object expected,
                                    final Object actual,
                                    final String message) {
        assertEquals(
                message,
                expected,
                actual
        );
    }
}

