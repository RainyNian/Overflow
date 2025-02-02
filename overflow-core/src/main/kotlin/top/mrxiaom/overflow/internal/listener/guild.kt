package top.mrxiaom.overflow.internal.listener

import cn.evole.onebot.sdk.event.message.GuildMessageEvent
import cn.evole.onebot.sdk.event.message.GuildMessageEvent.GuildSender
import cn.evolvefield.onebot.client.handler.EventBus
import cn.evolvefield.onebot.client.listener.EventListener
import net.mamoe.mirai.contact.MemberPermission
import top.mrxiaom.overflow.event.LegacyGuildMessageEvent
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.message.OnebotMessages

internal fun EventBus.addGuildListeners(bot: BotWrapper) {
    listOf(
        GuildMessageListener(bot),

        ).forEach(::addListener)
}

internal class GuildMessageListener(
    val bot: BotWrapper
) : EventListener<GuildMessageEvent> {
    override suspend fun onMessage(e: GuildMessageEvent) {
        when (e.subType) {
            "channel" -> {
                val miraiMessage = OnebotMessages.deserializeFromOneBot(bot, e.message)
                val messageString = miraiMessage.toString()

                if (e.sender.userId == bot.id) {
                    // TODO: 过滤自己发送的消息
                } else {
                    bot.logger.verbose("[频道][${e.guildId}(${e.channelId})] ${e.sender.nameCardOrNick}(${e.sender.userId}) -> $messageString")
                    bot.eventDispatcher.broadcastAsync(LegacyGuildMessageEvent(
                        bot = bot,
                        guildId = e.guildId,
                        channelId = e.channelId,
                        messageId = e.messageId,
                        message = miraiMessage,
                        senderId = e.sender.userId,
                        senderTinyId = e.sender.tinyId,
                        senderNick = e.sender.nickname,
                        senderNameCard = e.sender.card,
                        senderTitle = e.sender.title,
                        senderLevel = e.sender.level,
                        senderRole = when(e.sender.role.lowercase()) {
                            "owner" -> MemberPermission.OWNER
                            "admin" -> MemberPermission.ADMINISTRATOR
                            else -> MemberPermission.MEMBER
                        },
                        time = (e.time / 1000).toInt()
                    ))
                }
            }
        }
    }

    private val GuildSender.nameCardOrNick: String
        get() = card.takeIf { it.isNotBlank() } ?: nickname
}
