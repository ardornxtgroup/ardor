package nxt.addons;

import nxt.blockchain.TransactionTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ValidationAnnotation
public @interface ValidateTransactionType {

    TransactionTypeEnum[] accept() default {};
    TransactionTypeEnum[] reject() default {};

}