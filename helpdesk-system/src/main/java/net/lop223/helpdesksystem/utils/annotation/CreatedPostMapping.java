package net.lop223.helpdesksystem.utils.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = RequestMethod.POST)
@ResponseStatus(HttpStatus.CREATED)
public @interface CreatedPostMapping {

    @AliasFor(annotation = PostMapping.class, attribute = "value")
    String[] value() default {};

    @AliasFor(annotation = PostMapping.class, attribute = "consumes")
    String[] consumes() default {};

    @AliasFor(annotation = PostMapping.class, attribute = "produces")
    String[] produces() default {};
}
