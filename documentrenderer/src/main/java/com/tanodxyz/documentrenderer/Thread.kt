package com.tanodxyz.documentrenderer

/**
 * Indicates on which thread the method will be called.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Thread(val description:String)
