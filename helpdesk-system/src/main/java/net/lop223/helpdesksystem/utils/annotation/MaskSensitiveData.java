package net.lop223.helpdesksystem.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaskSensitiveData {
    String mask() default "********";
    String maskDescription() default "Masked";
}
