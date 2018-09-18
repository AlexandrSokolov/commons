package com.savdev.commons.config;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated property name, defines a property
 * that either might be empty or not exist at all
 */
@Retention(RUNTIME)
public @interface MapProperty {
}
