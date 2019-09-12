package org.othercraft.bot

import discord4j.core.DiscordClient
import reactor.core.publisher.Mono


class CommandBuilder {
    private val aliases = mutableListOf<String>()
    var desc: String = "No description"

    var permission: PermissionLevel = PermissionLevel.ANY

    var hidden = false

    private var execute: (Context.() -> Mono<Void>)? = null

    fun execute(block: Context.() -> Mono<out Any>) {
        execute = { this.run(block).then() }
    }

    fun <A> executeRaw(block: Context.() -> A) {
        execute = { Mono.just(run(block)).then() }
    }

    var alias: String
        get() = ""
        set(value) {
            aliases.add(value)
        }
    var name: String
        get() = ""
        set(value) {
            aliases.add(value)
        }


    internal fun build(): Command {
        if (execute == null)
            error("No executor defined")

        return object : Command(
            desc = desc,
            names = aliases,
            permission = permission,
            hidden = hidden
        ) {
            override fun run(context: Context): Mono<Void> {
                return execute!!.invoke(context)
            }
        }
    }
}