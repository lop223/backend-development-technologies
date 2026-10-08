package net.lop223.helpdesksystem.utils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the username of the currently authenticated user into a {@code String} controller parameter.
 * Resolved by {@link net.lop223.helpdesksystem.utils.resolver.CurrentUsernameArgumentResolver}.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUsername {
}
