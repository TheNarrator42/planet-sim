package org.othercraft.bot

import discord4j.core.DiscordClient
import reactor.core.publisher.Mono

class ManyCommandsBuilder {

    private val commands = mutableListOf<Command>()

    fun command(block: CommandBuilder.() -> Unit){
        commands.add(CommandBuilder().apply(block).build())
    }
    fun finish(client: DiscordClient): Mono<Void> {
        client.commands.addAll(commands)
        return client.applyCommands(commands)
    }

    inner class CommandBuilder {
        private val aliases = mutableListOf<String>()
        var desc: String = "No description"

        var permission: PermissionLevel = PermissionLevel.ANY


        private var execute: ((Context) -> Mono<Void>)? = null

        fun execute(block: (Context) -> Mono<out Any>) {
            execute = { block(it).then() }
        }

        fun <A> executeRaw(block: (Context) -> A) {
            execute = { Mono.just(block(it)).then() }
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
                permission = permission
            ) {
                override fun run(context: Context): Mono<Void> {
                    return execute!!.invoke(context)
                }
            }
        }
    }
}