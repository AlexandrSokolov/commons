package com.savdev.commons.config;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated "static final String" constant, not a property
 *  that should be ignored during validation
 */
@Retention(RUNTIME)
public @interface NotProperty {
}
