package io.github.proxyfox.command.extension

import com.mojang.brigadier.builder.ArgumentBuilder

/**
 * A case-insensitive literal argument builder
 * @author Oliver
 * */
open class CaseInsensitiveLiteralArgumentBuilder<S> constructor(val literal: String) :
    ArgumentBuilder<S, CaseInsensitiveLiteralArgumentBuilder<S>>() {

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

    fun caseInsensitiveLiteral(
        literal: String,
        action: CaseInsensitiveLiteralArgumentBuilder<S>.() -> Unit
    ) {
        val result = literal<S>(literal).apply(action).build()
        this.then(result)
    }
}

