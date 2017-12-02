package pl.devthoughts.todos.modules;

import akka.util.ByteString;

import io.vavr.collection.List;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import pl.devthoughts.todos.controllers.TodoItemRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import play.api.http.ParserConfiguration;
import play.mvc.Http;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.devthoughts.todos.assertj.TodoItemRequestAssert.assertThat;
import static pl.devthoughts.todos.controllers.TodoItemRequest.DUE_DATE_FORMAT;
import static pl.devthoughts.todos.modules.OpenCsvBodyParser.NON_CSV_MIME_TYPE_ERR_MSG;
import static pl.devthoughts.todos.modules.OpenCsvBodyParser.NO_BODY_ERR_MSG;
import static pl.devthoughts.todos.modules.OpenCsvBodyParser.TEXT_CSV_MIME_TYPE;

public class OpenCsvBodyParserTest {

    private static final List<String> NON_CSV_MIMES =
        List.of("application/json", "text/plain", "text/xml");

    private static final ParserConfiguration
        PARSER_CONFIGURATION =
        new ParserConfiguration(10000, 10000L);

    @Test
    public void should_reject_request_with_no_content_type_header() {
        Http.RequestHeader request = new Http.RequestBuilder().build();

        assertThatThrownBy(() -> bodyParser().parse(request, ByteString.empty()))
            .hasMessage(NON_CSV_MIME_TYPE_ERR_MSG);
    }

    @Test
    public void should_not_accept_non_csv_mime_type() {
        Http.RequestHeader request =
            new Http.RequestBuilder().header("Content-Type", nonCsvMimeType()).build();

        assertThatThrownBy(() -> bodyParser().parse(request, ByteString.empty()))
            .hasMessage(NON_CSV_MIME_TYPE_ERR_MSG);
    }

    @Test
    public void should_reject_request_with_no_body() {
        Http.RequestHeader request =
            new Http.RequestBuilder().header("Content-Type", TEXT_CSV_MIME_TYPE).build();

        assertThatThrownBy(() -> bodyParser().parse(request, ByteString.empty()))
            .hasMessage(NO_BODY_ERR_MSG);
    }

    @Test
    public void should_reject_csv_body_with_no_csv_header() throws Exception {
        ByteString
            bodyContent =
            ByteString.fromString("Do something,2017-09-16 23:59");

        Http.RequestHeader request =
            new Http.RequestBuilder()
                .bodyRaw(bodyContent)
                .header("Content-Type", TEXT_CSV_MIME_TYPE)
                .build();

        assertThatThrownBy(() -> bodyParser().parse(request, bodyContent))
            .hasMessage("Error capturing CSV header!");
    }

    @Test
    public void should_parse_request_with_csv_body() throws Exception {
        ByteString bodyContent = ByteString.fromString("name,dueDate\nDo something,2017-09-16 23:59");

        Http.RequestHeader request =
            new Http.RequestBuilder()
                .bodyRaw(bodyContent)
                .header("Content-Type", TEXT_CSV_MIME_TYPE)
                .build();

        final java.util.List<TodoItemRequest> requests = bodyParser().parse(request, bodyContent);

        assertThat(requests.get(0))
            .hasName("Do something")
            .hasDueDate(fromString("2017-09-16 23:59"));

    }

    @NotNull
    private OpenCsvBodyParser bodyParser() {
        return new OpenCsvBodyParser(PARSER_CONFIGURATION, new DummyErrorHandler());
    }

    @NotNull
    private LocalDateTime fromString(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DUE_DATE_FORMAT);
        return LocalDateTime.parse(date, formatter);
    }

    @NotNull
    private String nonCsvMimeType() {
        return NON_CSV_MIMES.shuffle().head();
    }

}
