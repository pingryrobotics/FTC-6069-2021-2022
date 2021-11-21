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
 * corresponding to a location on the FTC field in matrix form
 *
 * These coordinates are typically used in conjunction with a SpaceMap and are all
 * scaled in accordance wih the FieldMap's scale.
 *
 * These are always in the format {straight distance, side distance}.
 */
@Documented
@CoordinateRange
@Retention(CLASS)
@Target({METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})
public @interface MatrixCoordinates {
}
