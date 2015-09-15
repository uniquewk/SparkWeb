package com.sparkweb.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记一个类被用于配置 @Path 路由
 * 
 * @author yswang
 * @version 1.0
 */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

}
