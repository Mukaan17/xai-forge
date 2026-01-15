package com.xaiforge.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to automatically log activities via AOP.
 * When applied to a controller method, it will automatically log the activity
 * after successful execution.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogActivity {
    
    /**
     * The event type to log (as string, e.g., "LOGIN_SUCCESS", "DATASET_UPLOADED").
     * Must match ActivityLog.EventType enum values.
     */
    String eventType();
    
    /**
     * Description of the activity (can use SpEL expressions).
     * Example: "Uploaded dataset: #{#datasetName}"
     */
    String description() default "";
    
    /**
     * Resource type (e.g., "DATASET", "MODEL", "PREDICTION").
     */
    String resourceType() default "";
    
    /**
     * SpEL expression to extract resource ID from method parameters or return value.
     * Example: "#result.id" or "#datasetId"
     */
    String resourceId() default "";
    
    /**
     * SpEL expression to extract resource name.
     * Example: "#result.fileName" or "#datasetName"
     */
    String resourceName() default "";
}
