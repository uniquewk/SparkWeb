package com.sparkweb.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cache an action's result.
 *
 * <p>Example: <code>@CacheFor("1h")</code>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheFor 
{
	// 数据放入缓存的哪个缓存区中
    String region() default "";
    
    // 数据的唯一标识key
    String key() default "";
}
