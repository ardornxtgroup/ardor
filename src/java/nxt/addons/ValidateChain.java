package nxt.addons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ValidationAnnotation
public @interface ValidateChain {
    int[] accept() default {};
    int[] reject() default {};
}
