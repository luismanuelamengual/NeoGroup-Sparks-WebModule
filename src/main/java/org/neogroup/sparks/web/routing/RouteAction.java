package org.neogroup.sparks.web.routing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Action to access a method in a web processor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RouteAction {

    /**
     * Name of the action in web processor
     * @return string
     */
    public String name() default "";
}
