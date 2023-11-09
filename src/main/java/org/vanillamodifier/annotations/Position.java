package org.vanillamodifier.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface Position {
    Priority value();
    int ordinal() default -1;
    String target() default "";
    enum Priority {
        /**
         * Method begin
         */
        FRIST,
        /**
         * Method end
         */
        LAST,
        /**
         * Method return
         */
        RETURN,
        /**
         * Method invoke
         */
        INVOKE,
        /**
         * Overwrite method
         */
        OVERWRITE
    }
}
