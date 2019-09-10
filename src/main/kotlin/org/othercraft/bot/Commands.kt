package org.othercraft.bot

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.util.function.Tuple2
import reactor.util.function.Tuple3
import reactor.util.function.Tuple4
import reactor.util.function.Tuples
import java.awt.Color
import java.io.File
import java.util.*


val commandsMap = mutableMapOf<DiscordClient,MutableList<Command>>()
val DiscordClient.commands: MutableList<Command>
    get(){
        return if (commandsMap.containsKey(this)){
            commandsMap[this]!!
        }else{
            commandsMap[this] = mutableListOf()
            commandsMap[this]!!
        }
    }


fun commands(client: DiscordClient, block: ManyCommandsBuilder.() -> Unit):Mono<Void>{
    return ManyCommandsBuilder().apply(block).finish(client)
}


fun start(token :String, block : DiscordClientBuilder.() -> Unit = {}): DiscordClient{
    return DiscordClientBuilder(token).apply(block).build()
}
fun start(token : File, block :DiscordClientBuilder.() -> Unit = {}): DiscordClient {
    return start(token.readText(Charsets.UTF_8),block)
}


const val HELP_OVERRIDE = "!!!help"



//TODO fix this
fun DiscordClient.applyCommands(commands :List<Command>):Mono<Void>{
    return this.eventDispatcher
        .on(MessageCreateEvent::class.java)
        .filter { it.message.content.isPresent }
        .map { Tuples.of(it,Config.prefix.getPrefixForGuild(it.guildId)) }
        .filter { tuple -> tuple.t1.message.content.get().startsWith(tuple.t2) || tuple.t1.message.content.get() == HELP_OVERRIDE }
        .map { (event,prefix) ->

            val command = commands.firstOrNull { command ->
                command.names.any { alias -> event.message.content.get().startsWith(prefix + alias) }
            } ?: event.message.content.map<String?> { it }.orElse(null)?.run {
                if(this == HELP_OVERRIDE || this == "${prefix}help") Config.defaultHelp.getFor(event) else null
            }
            Tuples.of(event,prefix,Optional.ofNullable(command))

        }
        .filter { it.t3.isPresent }
        .map { Tuples.of(it.t1,it.t2,it.t3.get()) }
        .map { (event,prefix,command) ->
            Tuples.of(event,prefix,command, getPermissionLevelForUser(event.member))
        }
        .flatMap { tuple ->
            tuple.t4.map { four -> Tuples.of(tuple.t1,tuple.t2,tuple.t3,four) } }
        .map { (event,prefix,command,permission) ->
            Tuples.of(Context(
                prefix = prefix,
                permissionLevel = permission,
                event = event,
                nameUsed = command.names.first { alias -> event.message.content.get().startsWith(prefix + alias) }
            ),command)
        }.flatMap {(context,command) ->
            (if (command.permission > context.permissionLevel) {
                Config.badPermissionHandler.run(context, command)
            } else {
                command.run(context)
            }).onErrorResume { e ->
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
        .then()

}




fun getPermissionLevelForUser(memberOp: Optional<Member>):Mono<PermissionLevel> {
    return memberOp.map { member ->
        member.isPlayer().toMono()// FIXME this can be a lot better
            .flatMap { isPlayer -> Config.isMasterAdmin.isMasterAdmin(member).map { Tuples.of(isPlayer,it) } }
            .map { (isPlayer, isMasterAdmin) ->
                when {
                    isMasterAdmin -> PermissionLevel.MASTER_ADMIN
                    isPlayer -> PermissionLevel.PLAYER
                    else -> PermissionLevel.ANY
                }
            }
    }.orElse(Mono.just(PermissionLevel.ANY))
}

fun Member.isPlayer(): Boolean = this.roleIds.contains(Snowflake.of(620577477391155201))


operator fun <T1, T2> Tuple2<T1, T2>.component1():T1 = t1

operator fun <T1, T2> Tuple2<T1, T2>.component2():T2 = t2

operator fun <T1, T2, T3> Tuple3<T1, T2, T3>.component3():T3 = t3


operator fun <T1, T2, T3,T4> Tuple4<T1, T2, T3, T4>.component4():T4 = t4

fun config(block :ConfigBuilder.() -> Unit){
    ConfigBuilder().apply(block).setupConfig()
}
