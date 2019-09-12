package org.othercraft.bot

import reactor.core.publisher.Mono

class NormalHelpPrinter : HelpCommand() {
    override fun run(context: Context): Mono<Void> {
        val commands = commands.filter { it.permission <= context.permissionLevel }
        return context.event.message.channel.flatMap { channel ->
            channel.createEmbed { spec ->
                commands.forEach { command ->
                    if (!command.hidden)
                    spec.addField(context.prefix + " " + command.names[0],command.desc,false)
                }
                spec.setTitle("Help menu")
            }
        }.then()
    }
}