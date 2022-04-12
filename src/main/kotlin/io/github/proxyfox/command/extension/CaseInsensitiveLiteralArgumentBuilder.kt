package io.github.proxyfox.command.extension

import com.mojang.brigadier.builder.ArgumentBuilder

open class CaseInsensitiveLiteralArgumentBuilder<S> protected constructor(val literal: String) :
    ArgumentBuilder<S?, CaseInsensitiveLiteralArgumentBuilder<S>>() {

    override fun getThis(): CaseInsensitiveLiteralArgumentBuilder<S> {
        return this
    }

    override fun build(): CaseInsensitiveLiteralCommandNode<S> {
        val result = CaseInsensitiveLiteralCommandNode(
            literal,
            command, requirement, redirect, redirectModifier, isFork
        )
        for (argument in arguments) {
            result.addChild(argument)
        }
        return result
    }

    companion object {
        fun <S> literal(name: String): CaseInsensitiveLiteralArgumentBuilder<S> {
            return CaseInsensitiveLiteralArgumentBuilder(name)
        }
    }
}

inline fun <S> ArgumentBuilder<S, *>.caseInsensitiveLiteral(
    literal: String,
    action: CaseInsensitiveLiteralArgumentBuilder<S>.() -> Unit
): CaseInsensitiveLiteralCommandNode<S> {
    val result = CaseInsensitiveLiteralArgumentBuilder.literal<S>(literal).apply(action).build()
    this.then(result)
    return result
}