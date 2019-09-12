package org.othercraft.bot

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuple3
import reactor.util.function.Tuple4
import java.awt.Color
import java.io.File
import java.util.*




val commands = mutableListOf<Command>()

fun command(block: CommandBuilder.() -> Unit){
    commands.add(CommandBuilder().apply(block).build())
}

private fun commandBuild(block: CommandBuilder.() -> Unit):Command{
    return CommandBuilder().apply(block).build()
}

fun start(token :String, block : DiscordClientBuilder.() -> Unit = {}): DiscordClient{
    return DiscordClientBuilder(token).apply(block).build()
}
fun start(token : File, block :DiscordClientBuilder.() -> Unit = {}): DiscordClient {
    return start(token.readText(Charsets.UTF_8),block)
}

fun DiscordClient.runCommands(): Mono<Void> = this.eventDispatcher
    .on(MessageCreateEvent::class.java)
    .filter { it.message.content.isPresent }
    .flatMap { Mono.justOrEmpty(it.message.content).filter { str -> str.startsWith("ps") }.map { w -> it to w } }
    .filter { (_,content) -> content.startsWith("ps") }
    .flatMap { (event,content) ->
        val arguments = content.substring(Config.prefix.length).trim()
        val commandName = arguments.substring(0,arguments.indexOf(' ').takeUnless { it == -1 } ?: arguments.length)
        val command = when {
            arguments.isBlank() -> // the person is clearly wrong and needs to be punished
                commandBuild { execute { createMessage("Use `${Config.prefix} help` for help!") } }
            arguments == "help" -> Config.defaultHelp //flagged first just to be sure.
            else -> commands.firstOrNull { it.names.contains(arguments) } ?:
            commandBuild { execute { createMessage("I can't find command `$commandName`. Use `ps help` for help") } }
        }

        val permissionLevel = getPermissionLevelForUser(event.member)
        val context = Context(
            prefix = "ps",
            permissionLevel = getPermissionLevelForUser(event.member),
            event = event,
            nameUsed = commandName
        )
        if (permissionLevel < command.permission){
            Config.badPermissionHandler.run(context,command)
        } else {
            command.run(context).onErrorResume { e ->
                e.printStackTrace()
                context.event.message.channel.flatMap { channel ->
                    channel.createEmbed { spec ->
                        spec.setColor(Color.RED)
                        spec.setTitle("Internal error : ${e::class.java.simpleName}")
                        spec.setDescription(e.message ?: "")
                    }
                }.then()
            }
        }
    }.then()


fun getPermissionLevelForUser(memberOp: Optional<Member>):PermissionLevel {
    return memberOp.map {
        when {
            Config.isMasterAdmin.isMasterAdmin(it) -> PermissionLevel.ADMIN
            it.isPlayer() -> PermissionLevel.PLAYER
            else -> PermissionLevel.ANY
        }
    }.orElse(PermissionLevel.ANY)
}

fun Member.isPlayer(): Boolean = this.roleIds.contains(Snowflake.of(620577477391155201))


operator fun <T1, T2> Tuple2<T1, T2>.component1():T1 = t1

operator fun <T1, T2> Tuple2<T1, T2>.component2():T2 = t2

operator fun <T1, T2, T3> Tuple3<T1, T2, T3>.component3():T3 = t3


operator fun <T1, T2, T3,T4> Tuple4<T1, T2, T3, T4>.component4():T4 = t4

fun config(block :ConfigBuilder.() -> Unit){
    ConfigBuilder().apply(block).setupConfig()
}
