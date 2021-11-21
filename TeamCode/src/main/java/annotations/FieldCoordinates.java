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
 * Denotes that a parameter, field, or method return value is a set of coordinates
 * corresponding to a cartesian location on the FTC field.
 * These are always in the format {straight distance, side distance}, and are always
 * scaled in millimeters for production code.
 *
 * These coordinates are typically used by OpenGLMatrices or vuforia.
 */
@Documented
@CoordinateRange
@Retention(CLASS)
@Target({METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})
public @interface FieldCoordinates {
}

