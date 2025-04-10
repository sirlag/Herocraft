package app.herocraft.core.extensions

fun String?.isEmailAddress(): Boolean = this != null && contains("@") && contains(".")