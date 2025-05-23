 
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.item.ItemBucketMilk
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "FastUse", category = ModuleCategory.PLAYER)
class FastUse : Module() {

    private val modeValue = ListValue("Mode", arrayOf("NCP","Instant", "Timer", "CustomDelay", "DelayedInstant", "MinemoraTest", "AAC", "NewAAC","Medusa","Matrix","Fast", "BlocksMC"), "DelayedInstant")
    private val timerValue = FloatValue("Timer", 1.22F, 0.1F, 2.0F).displayable { modeValue.equals("Timer") }
    private val durationValue = IntegerValue("InstantDelay", 14, 0, 35).displayable { modeValue.equals("DelayedInstant") }
    private val delayValue = IntegerValue("CustomDelay", 0, 0, 300).displayable { modeValue.equals("CustomDelay") }
    private val viaFixValue = BoolValue("ViaVersion", false)

    private val msTimer = MSTimer()
    private var usedTimer = false
    private var sentPacket = false
    private var lastState = false

    private fun send(int: Int) {
        repeat(int) {
            mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
        }
    }

    private fun send() {
        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
    }
    
    private fun stopUsing() {
        if (viaFixValue.get()) {
            sentPacket = true
            if (mc.thePlayer.itemInUseCount < 10) {
                mc.thePlayer.itemInUseCount = 20
            }
        } else {
            sentPacket = false
            mc.playerController.onStoppedUsingItem(mc.thePlayer)
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }

        if (!mc.thePlayer.isUsingItem) {
            sentPacket = false
            lastState = mc.thePlayer.isUsingItem
            return
        } else if(!lastState) {
            sentPacket = false
        }
        
        lastState = mc.thePlayer.isUsingItem
        
        if (viaFixValue.get() && sentPacket) {
            if (mc.thePlayer.itemInUseCount < 10) {
                mc.thePlayer.itemInUseCount = 20
            }
            return
        }

        val usingItem = mc.thePlayer.itemInUse.item

        if (usingItem is ItemFood || usingItem is ItemBucketMilk || usingItem is ItemPotion) {
            when (modeValue.get().lowercase()) {
                "matrix" -> {
                    mc.timer.timerSpeed = 0.5f
                    usedTimer = true
                    send()
                }
                "fast" -> {
                    if (mc.thePlayer.itemInUseDuration < 25) {
                        mc.timer.timerSpeed = 0.3f
                        usedTimer = true
                        send(5)
                    }
                }
                "blocksmc" -> {
                    for (i in 0 until 2) {
                        mc.netHandler.addToSendQueue(
                            C03PacketPlayer.C06PacketPlayerPosLook(
                                mc.thePlayer.posX,
                                mc.thePlayer.posY,
                                mc.thePlayer.posZ,
                                mc.thePlayer.rotationYaw,
                                mc.thePlayer.rotationPitch,
                                mc.thePlayer.onGround
                            )
                        )
                    }
                }
                "medusa" -> {
                    if (mc.thePlayer.itemInUseDuration > 5 || !msTimer.hasTimePassed(360L))
                        return

                    send(20)

                    msTimer.reset()
                }
                "delayedinstant" -> if (mc.thePlayer.itemInUseDuration > durationValue.get()) {
                    send(35 - mc.thePlayer.itemInUseDuration)

                    stopUsing()
                }

                "ncp" -> if (mc.thePlayer.itemInUseDuration > 14) {
                    send(20)

                    stopUsing()
                }

                "instant" -> {
                    send(35)

                    stopUsing()
                }
                "aac" -> {
                    mc.timer.timerSpeed = 0.49F
                    usedTimer = true
                    if (mc.thePlayer.itemInUseDuration > 14) {
                        send(23)
                    }
                }
                "newaac" -> {
                    mc.timer.timerSpeed = 0.49F
                    usedTimer = true
                    send(2)
                }
                "timer" -> {
                    mc.timer.timerSpeed = timerValue.get()
                    usedTimer = true
                }

                "minemoratest" -> {
                    mc.timer.timerSpeed = 0.5F
                    usedTimer = true
                    if (mc.thePlayer.ticksExisted % 2 == 0) {
                        send(2)
                    }
                }

                "customdelay" -> {
                    if (!msTimer.hasTimePassed(delayValue.get().toLong())) {
                        return
                    }

                    send()
                    msTimer.reset()
                }
            }
            if (mc.thePlayer.itemInUseDuration >= 30 && viaFixValue.get()) {
                sentPacket = true
                if (mc.thePlayer.itemInUseCount < 10) {
                    mc.thePlayer.itemInUseCount = 20
                }
            }
        }
    }
    override fun onDisable() {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }
        sentPacket = false
        lastState = mc.thePlayer.isUsingItem
    }

    override val tag: String
        get() = modeValue.get()
}
