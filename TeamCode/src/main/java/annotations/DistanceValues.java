package annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Denotes that a parameter, field, or method return value is a set of values representing
 * the distance from a fixed location (typically a camera, or ftc bot) to an object or
 * location. These are always in the format {straight distance, side distance}, but can be
 * of any scale. These are typically accompanied by another annotation describing which
 * range the distances are for.
 */
@Documented
@CoordinateRange
@Retention(CLASS)
@Target({METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})
public @interface DistanceValues {
}

