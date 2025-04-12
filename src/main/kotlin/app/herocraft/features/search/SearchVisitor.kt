package app.herocraft.features.search

import app.herocraft.antlr.generated.HQLBaseVisitor
import app.herocraft.antlr.generated.HQLParser
import org.antlr.v4.kotlinruntime.tree.TerminalNode
import org.slf4j.LoggerFactory


class SearchVisitor : HQLBaseVisitor<SearchItem>() {

    private val logger = LoggerFactory.getLogger(SearchVisitor::class.java)

    override fun visitQuery(ctx: HQLParser.QueryContext): SearchItem {
        return this.visitChildren(ctx)
    }

    override fun visitExpression(ctx: HQLParser.ExpressionContext): SearchItem {
        return this.visitChildren(ctx)
    }

    override fun visitOrExpression(ctx: HQLParser.OrExpressionContext): SearchItem {

        println(ctx.childCount)
        return when (val childItem = this.visitChildren(ctx)) {
            is ValuesSearchItem -> BooleanSearchItem(
                BooleanSearchType.OR,
                childItem.children
            )

            else -> childItem
        }

    }

    override fun visitAndExpression(ctx: HQLParser.AndExpressionContext): SearchItem {
        return when (val childItem = this.visitChildren(ctx)) {
            is ValuesSearchItem -> BooleanSearchItem(
                BooleanSearchType.AND,
                childItem.children
            )

            else -> childItem
        }
    }

    override fun visitPrimaryExpression(ctx: HQLParser.PrimaryExpressionContext): SearchItem {
        println("Visiting Primary Expression: ${ctx.text}")
        return if (ctx.NOT() != null) {
            NotSearchItem(this.visitChildren(ctx))
        } else {
            this.visitChildren(ctx)
        }
    }

    override fun visitTerm(ctx: HQLParser.TermContext): SearchItem {

        return when (ctx.childCount) {
            1 -> FieldSearchItem(SearchField.NAME, FieldOperation.EQ, ctx.text)
            else -> {
                val type = ctx.fieldSpecifier()?.text
                    ?.let { SearchField.parse(it) }
                    ?: SearchField.UNKNOWN

                val operation = (ctx.COLON()?.text ?: ctx.comparisonOperator()?.text)
                    ?.let { FieldOperation.parse(it) }
                    ?: FieldOperation.UNKNOWN

                val value = ctx.atomicValue()?.text?.replace("\"", "") ?: "Unknown Value"
                FieldSearchItem(type, operation, value)

            }
        }
    }

    override fun defaultResult(): SearchItem {
        return EmptySearchItem
    }

    override fun aggregateResult(
        aggregate: SearchItem,
        nextResult: SearchItem
    ): SearchItem {

        logger.debug("Original: {} - Next: {}", aggregate, nextResult)

        if (nextResult is TerminalSearchItem) {
            return aggregate
        }

        return when (aggregate) {
            is EmptySearchItem, is TerminalSearchItem -> nextResult
            is ValuesSearchItem -> {
                ValuesSearchItem(aggregate.children + nextResult)
            }

            is FieldSearchItem -> {
                when (nextResult) {
                    is BooleanSearchItem, is FieldSearchItem, is NotSearchItem -> {
                        ValuesSearchItem(listOf(aggregate, nextResult))
                    }
                    else -> {
                        logger.error("Unknown field term aggregation found")
                        EmptySearchItem
                    }
                }
            }

            is BooleanSearchItem, is NotSearchItem-> {
                logger.warn("Calling fall through aggregation case.")
                super.aggregateResult(aggregate, nextResult)
            }
        }
    }

    override fun visitTerminal(node: TerminalNode): SearchItem {
        return TerminalSearchItem
    }

}