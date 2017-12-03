package pl.devthoughts.todos.modules.protobuf;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.testkit.TestProbe;
import akka.util.ByteString;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import pl.devthougths.todos.ProtobufTodoItem;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import play.core.j.JavaContextComponents;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import static java.util.concurrent.TimeUnit.SECONDS;
import static pl.devthoughts.todos.TimeUtils.asTimestamp;
import static pl.devthoughts.todos.TodosConfig.DUE_DATE_FORMAT;

public class ProtobufActionTest {

    private static final String PROTOBUF_MIME_TYPE = "application/x-protobuf";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DUE_DATE_FORMAT);

    private static final String ITEM_NAME = "Do something";
    private static final String DUE_DATE = "2017-09-16T23:59:00";

    private ActorSystem system;
    private Materializer mat;

    @Before
    public void setUp() {
        system = ActorSystem.create();
        mat = ActorMaterializer.create(system);
    }

    @After
    public void tearDown() {
        system.terminate();
    }

    @Test
    public void should_reject_request_with_no_content_type_header() throws Exception {
        // given
        final byte[] todoItemAsBytes = ProtobufTodoItem.CreateItemRequest
            .newBuilder()
            .setName(ITEM_NAME)
            .setDueDate(asTimestamp(DUE_DATE))
            .build()
            .toByteArray();
        ByteString bodyContent = ByteString.fromArray(todoItemAsBytes);

        Http.Request request = new Http.RequestBuilder()
            .bodyRaw(bodyContent)
            .header("Content-Type", PROTOBUF_MIME_TYPE)
            .build();

        Http.Context ctx = new Http.Context(request, javaContextComponents());

        // when
        final CompletionStage<Result> completionStage =
            new ProtobufAction(protobufParser(), delegateAction()).call(ctx);

        // then
        final Result result = completionStage.toCompletableFuture().get(1L, SECONDS);

        final TestProbe probe = new TestProbe(system);
        result.body().dataStream().map(ByteString::utf8String)
            .to(Sink.actorRef(probe.ref(), "fin")).run(mat);
        probe.expectMsg(ITEM_NAME);
    }

    private ProtobufParser protobufParser() {
        final ProtobufParser protobufParser = Mockito.mock(ProtobufParser.class);
        Mockito.when(protobufParser.value()).thenAnswer(
            (Answer<Object>) invocationOnMock -> ProtobufTodoItem.CreateItemRequest.class);
        return protobufParser;
    }

    private JavaContextComponents javaContextComponents() {
        return Mockito.mock(JavaContextComponents.class);
    }

    private Action<ProtobufParser> delegateAction() {
        return new Action<ProtobufParser>() {
            @Override
            public CompletionStage<Result> call(Http.Context ctx) {
                final ProtobufTodoItem.CreateItemRequest item =
                    ctx.request().body().as(ProtobufTodoItem.CreateItemRequest.class);
                return CompletableFuture.completedFuture(Results.ok(item.getName()));
            }
        };
    }

}
