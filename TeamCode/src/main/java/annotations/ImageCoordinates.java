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
 * corresponding to a location on an image
 * Example, the pixel height and pixel width on an image where a ml recognition is located
 *
 * These are usually in {vertical px, horizontal px} form even though it's technically incorrect
 * because other coordinates are in {straight/vertical, side/horizontal} form, so this matches
 * for consistency
 */
@Documented
@CoordinateRange
@Retention(CLASS)
@Target({METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})
public @interface ImageCoordinates {
}



