package dev.proxyfox.common.annotations

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn("You shouldn't expose this publicly!")
annotation class DontExpose(val reason: String)
