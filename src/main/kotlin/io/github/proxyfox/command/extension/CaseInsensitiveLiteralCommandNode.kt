package io.github.proxyfox.command.extension

import com.mojang.brigadier.Command
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.CommandContextBuilder
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import io.github.proxyfox.logger
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

/**
 * A case-insensitive literal node
 * @author Oliver
 * */
class CaseInsensitiveLiteralCommandNode<S>(
    private val literal: String,
    command: Command<S?>?,
    requirement: Predicate<S?>?,
    redirect: CommandNode<S?>?,
    modifier: RedirectModifier<S?>?,
    forks: Boolean
) :
    CommandNode<S?>(command, requirement, redirect, modifier, forks) {
    override fun getName(): String {
        return literal
    }

    @Throws(CommandSyntaxException::class)
    override fun parse(reader: StringReader, contextBuilder: CommandContextBuilder<S?>) {
        val start = reader.cursor
        val end = parse(reader)
        if (end > -1) {
            contextBuilder.withNode(this, StringRange.between(start, end))
            return
        }
        logger.info("Test!")
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().createWithContext(reader, literal)
    }

    private fun parse(reader: StringReader): Int {
        val start = reader.cursor
        if (reader.canRead(literal.length)) {
            val end = start + literal.length
            if (reader.string.substring(start, end).lowercase() == literal.lowercase()) {
                reader.cursor = end
                if (!reader.canRead() || reader.peek() == ' ') {
                    return end
                } else {
                    reader.cursor = start
                }
            }
        }
        return -1
    }

    override fun listSuggestions(
        context: CommandContext<S?>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return if (literal.lowercase().startsWith(builder.remainingLowerCase)) {
            builder.suggest(literal).buildFuture()
        } else {
            Suggestions.empty()
        }
    }

    public override fun isValidInput(input: String): Boolean {
        return parse(StringReader(input)) > -1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LiteralCommandNode<*>) return false
        val that = other as CaseInsensitiveLiteralCommandNode<*>
        return if (literal != that.literal) false else super.equals(other)
    }

    override fun getUsageText(): String {
        return literal
    }

    override fun hashCode(): Int {
        var result = literal.hashCode()
        result = 31 * result + super.hashCode()
        return result
    }

    override fun createBuilder(): LiteralArgumentBuilder<S?> {
        val builder = LiteralArgumentBuilder.literal<S?>(literal)
        builder.requires(requirement)
        builder.forward(redirect, redirectModifier, isFork)
        if (command != null) {
            builder.executes(command)
        }
        return builder
    }

    override fun getSortedKey(): String {
        return literal
    }

    override fun getExamples(): Collection<String> {
        return setOf(literal)
    }

    override fun toString(): String {
        return "<literal $literal>"
    }

}