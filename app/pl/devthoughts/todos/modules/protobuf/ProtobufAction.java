package pl.devthoughts.todos.modules.protobuf;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;

import io.vavr.control.Try;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

public class ProtobufAction extends Action<ProtobufParser> {

    private static Logger.ALogger logger = Logger.of(ProtobufAction.class);

    ProtobufAction(ProtobufParser configuration, Action<?> delegate) {
        this.configuration = configuration;
        this.delegate = delegate;
    }

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {

        final Class<? extends MessageOrBuilder> inputType = configuration.value();

        System.out.println("delegate " + (delegate == null));
        System.out.println("inputType " + (inputType == null));

        return Try.of(() -> parseMessage(inputType, ctx))
            .map(protoMessage -> requestWithParsedData(ctx, protoMessage))
            .map(request -> delegate.call(ctx.withRequest(request)))
            .getOrElseGet(ex -> {
                logger.error("Cannot parse protobuf message", ex);
                return CompletableFuture
                        .completedFuture(Results.internalServerError(ex.toString()));
                }
            );
    }

    private AbstractMessage parseMessage(Class<? extends MessageOrBuilder> clazz, Http.Context ctx)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Method method = clazz.getMethod("parseFrom", new Class[]{byte[].class});
            return (AbstractMessage) method.invoke(clazz, bodyBytes(ctx));
    }

    private Http.Request requestWithParsedData(Http.Context ctx, Message message) {
        return ctx.request().withBody(new Http.RequestBody(message));
    }

    private byte[] bodyBytes(Http.Context ctx) {
        return ctx.request().body().asBytes().toArray();
    }
}
