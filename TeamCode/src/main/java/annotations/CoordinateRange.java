package annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Denotes that an annotation represents a set of valid and within bounds coordinates or
 * values for their specific use case.
 */
@Documented
@Retention(CLASS)
@Target({ANNOTATION_TYPE})
@interface CoordinateRange {
}
