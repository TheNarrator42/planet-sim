package org.othercraft.bot

import com.google.common.base.Splitter
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import java.awt.Color

class Context(
    val prefix: String,
    val permissionLevel: PermissionLevel,
    val event : MessageCreateEvent,
    val nameUsed : String){

    val c//this is for backwards compatibility with people who don't want Context as this
        get() = this

    //helper functions
    fun createMessage(message :String): Mono<Message> {
        return event.message.channel.flatMap { it.createMessage(message) }
    }

    fun createMessage(message: Mono<String>): Mono<Message> {
        return event.message.channel.zipWith(message).flatMap { it.t1.createMessage(it.t2) }
    }

    fun createEmbed(spec :(EmbedCreateSpec) -> Unit):Mono<Message> {
        return this.event.message.channel.flatMap { it.createEmbed(spec) }
    }

    fun error(errorMessage: String,title: String):Mono<Message> {
        return createEmbed {
            it.setColor(Color.RED)
            it.setTitle(title)
            it.setDescription(errorMessage)
        }
    }

    fun internalError(message: String) = error(message,"Internal Error")
    fun clientError(message: String) = error(message,"Client Error")


    private var cache = ""
    fun cache(message :String){
        cache += "\n$message"
    }
    fun cacheWithNoNewline(message: String){
        cache += message
    }
    fun flush(): Flux<Message> {
        val cacheCache = cache
        cache = ""
        return event
            .message
            .channel
            .flatMapMany { channel ->
                val split = Splitter.fixedLength(Message.MAX_CONTENT_LENGTH)
                    .split(cacheCache)
                split.toFlux()
                    .filter { it.isNotBlank() }
                    .flatMap { channel.createMessage(it) }
            }
    }



    fun getArguments():String{
        return event.message.content.get()
            .replaceFirst(prefix,"")
            .replaceFirst(nameUsed,"")
            .trim()
    }


    fun getUserAsFirstArgument(): Snowflake? {
        return getArguments()
            .split(" ")
            .firstOrNull()
            ?.let { getUser(it) }
    }

    fun getUser(identifier :String): Snowflake? {
        val id = identifier
            .filter { !"<@!#>".contains("" + it) }
        return try {
            Snowflake.of(id)
        }catch(e :NumberFormatException) { null }

    }

}