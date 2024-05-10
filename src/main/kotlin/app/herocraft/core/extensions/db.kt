package app.herocraft.core.extensions

import org.jetbrains.exposed.sql.*

class InsensitiveLikeOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "ILIKE")

infix fun<T:String?> ExpressionWithColumnType<T>.ilike(pattern: String): Op<Boolean> =
    InsensitiveLikeOp(this, QueryParameter<T?>(pattern as T, columnType))

