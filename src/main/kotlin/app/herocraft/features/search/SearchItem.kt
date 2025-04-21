package app.herocraft.features.search

import java.util.*

sealed interface SearchItem

data class FieldSearchItem(val field: SearchField, val operation: FieldOperation, val value: String) : SearchItem
data object EmptySearchItem : SearchItem {
    override fun toString(): String {
        return "EmptySearchItem"
    }
}

data object TerminalSearchItem : SearchItem {
    override fun toString(): String {
        return "TerminalSearchItem"
    }
}
data class BooleanSearchItem(val type: BooleanSearchType, val children: List<SearchItem>) : SearchItem
data class ValuesSearchItem(val children: List<SearchItem>) : SearchItem
data class NotSearchItem(val child: SearchItem) : SearchItem

enum class BooleanSearchType {
    AND,
    OR
}

enum class FieldOperation {
    GTE,
    LTE,
    NEQ,
    GT,
    LT,
    EQ,
    UNKNOWN
    ;

    companion object {
        fun parse(string: String): FieldOperation =
            when (string.lowercase(Locale.getDefault())) {
                ">=" -> GTE
                "<=" -> LTE
                "!=" -> NEQ
                ">" -> GT
                "<" -> LT
                ":", "=" -> EQ
                else -> UNKNOWN
            }
    }
}

enum class SearchField {
    ARCHETYPE,
    ARTIST,
    FORMAT,
    FLAVOR,
    NAME,
    RULES,
    TYPE,
    UNKNOWN
    ;

    companion object {
        fun parse(string: String): SearchField =
            when (string.lowercase(Locale.getDefault())) {
                "a", "archetype", "c", "class" -> ARCHETYPE
                "artist"-> ARTIST
                "ft", "flavor" -> FLAVOR
                "f", "format" -> FORMAT
                "r", "rules", "o" -> RULES
                "t", "type" -> TYPE
                else -> UNKNOWN
            }
    }
}