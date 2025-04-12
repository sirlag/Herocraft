package app.herocraft

import app.herocraft.antlr.generated.HQLLexer
import app.herocraft.antlr.generated.HQLParser
import app.herocraft.features.search.SearchVisitor
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import kotlin.test.Test


class SearchTests {

    fun getContext(input: String): HQLParser.QueryContext {

        val inputStream = CharStreams.fromString(input)
        val lexer = HQLLexer(inputStream)
        val tokens = CommonTokenStream(lexer)
        val parser = HQLParser(tokens)

        return parser.query()
    }

    @Test
    fun testRawSearch() {
        val ctx = getContext("blood")
        println(ctx)

        val visitor = SearchVisitor()
        visitor.visit(ctx)
    }

    @Test
    fun testFieldSearch() {

        val ctx: HQLParser.QueryContext = getContext("t:flame")

        println(ctx)

        val vistor = SearchVisitor()
        val result = vistor.visit(ctx)

        println(result)

    }

    @Test
    fun testFieldSearchWithOr() {

        val ctx: HQLParser.QueryContext = getContext("t:flame OR t:stun")
        println(ctx)

        val vistor = SearchVisitor()
        val result = vistor.visit(ctx)

        println(result)

    }

    @Test
    fun testNot() {
        val ctx: HQLParser.QueryContext = getContext("NOT t:flame")
        println(ctx)

        val visitor = SearchVisitor()
        val result = visitor.visit(ctx)

        println(result)
    }


    @Test
    fun testNotOr() {
        val ctx: HQLParser.QueryContext = getContext("t:flame OR NOT t:stun")

        val visitor = SearchVisitor()
        val result = visitor.visit(ctx)

        println(result)
    }

    @Test
    fun testAnd() {
        val ctx: HQLParser.QueryContext = getContext("t:flame AND t:stun")
        val visitor = SearchVisitor()
        val result = visitor.visit(ctx)

        println(result)
    }

    @Test
    fun testImplicitAnd() {

        val ctx: HQLParser.QueryContext = getContext("blood r:stun")
        val visitor = SearchVisitor()
        val result = visitor.visit(ctx)

        println(result)
    }

    @Test
    fun testQuotedString() {
        val ctx: HQLParser.QueryContext = getContext("\"Destroy all cards\"")
        val visitor = SearchVisitor()
        val result = visitor.visit(ctx)

        println(result)
    }

    @Test
    fun testNestedQuery() {
        val ctx: HQLParser.QueryContext = getContext("blood OR (r:stun a:enchantress)")
        val visitor = SearchVisitor()
        val result = visitor.visit(ctx)

        println(result)
    }

}