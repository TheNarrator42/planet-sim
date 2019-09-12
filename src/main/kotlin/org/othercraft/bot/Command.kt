package org.othercraft.bot

import reactor.core.publisher.Mono

abstract class Command(
    val desc :String,
    val permission : PermissionLevel,
    val names :List<String>,
    val hidden: Boolean){
    abstract fun run(context: Context): Mono<Void>
}