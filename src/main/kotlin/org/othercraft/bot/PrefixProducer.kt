package org.othercraft.bot

import discord4j.core.`object`.util.Snowflake
import java.util.*

interface PrefixProducer{
    fun getPrefixForGuild(guild : Snowflake):String
    fun getDefaultPrefix():String
    fun getPrefixForGuild(guild : Optional<Snowflake>):String{
        return guild
            .map { getPrefixForGuild(it) }
            .orElseGet { getDefaultPrefix() }
    }
}