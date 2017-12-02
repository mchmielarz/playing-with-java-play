package pl.devthoughts.todos.modules.protobuf;

import com.google.protobuf.MessageOrBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import play.mvc.With;

@With(ProtobufAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtobufParser {
    Class<? extends MessageOrBuilder> value();
}