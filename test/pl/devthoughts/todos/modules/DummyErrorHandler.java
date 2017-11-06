package pl.devthoughts.todos.modules;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import play.http.HttpEntity;
import play.http.HttpErrorHandler;
import play.mvc.Http;
import play.mvc.Result;

public class DummyErrorHandler implements HttpErrorHandler {

    @Override
    public CompletionStage<Result> onClientError(Http.RequestHeader request,
                                                 int statusCode,
                                                 String message) {
        return CompletableFuture.completedFuture(
            new Result(statusCode, message, new HashMap<>(), HttpEntity.NO_ENTITY));
    }

    @Override
    public CompletionStage<Result> onServerError(Http.RequestHeader request,
                                                 Throwable exception) {
        return CompletableFuture.completedFuture(
            new Result(500, exception.getMessage(), new HashMap<>(), HttpEntity.NO_ENTITY));
    }
}
