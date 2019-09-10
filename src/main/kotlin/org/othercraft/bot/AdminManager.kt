package org.othercraft.bot

import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

@FunctionalInterface
interface AdminManager{
    fun isMasterAdmin(user : User): Mono<Boolean>
}

interface BadPermissionHandler{
    fun run(context: Context,command: Command):Mono<Void>
}

interface HelpCommand{
    fun getFor(event : MessageCreateEvent):Command
}