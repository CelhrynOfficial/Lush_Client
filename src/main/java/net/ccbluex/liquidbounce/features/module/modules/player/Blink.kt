package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.CrossSine
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.visual.Breadcrumbs
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.BlinkUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import org.lwjgl.opengl.GL11
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo(name = "Blink", category = ModuleCategory.PLAYER)
object Blink : Module() {
    private val modeValue = ListValue("Blink-Mode", arrayOf("All", "Movement"), "All")
    private val renderPlayer = BoolValue("Render", false)
    private val pulseValue = BoolValue("Pulse", false)
    private val serverPacket = BoolValue("Server-Packet", false)
    private val pulseDelayValue = IntegerValue("PulseDelay", 1000, 100, 5000).displayable { pulseValue.get() }

    private val pulseTimer = MSTimer()
    private var fakePlayer: EntityOtherPlayerMP? = null
    private val positions = LinkedList<DoubleArray>()

    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayClient>>()

    override fun onEnable() {
        if (mc.thePlayer == null) return
        if (modeValue.equals("All")) {
            BlinkUtils.setBlinkState(all = true)
        } else {
            BlinkUtils.setBlinkState(packetMoving = true)
        }
        if (renderPlayer.get()) {
            if (!pulseValue.get()) {
                fakePlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
                fakePlayer!!.clonePlayer(mc.thePlayer, true)
                fakePlayer!!.copyLocationAndAnglesFrom(mc.thePlayer)
                fakePlayer!!.rotationYawHead = mc.thePlayer.rotationYawHead
                mc.theWorld.addEntityToWorld(-1337, fakePlayer)
            }
        }
        packets.clear()
        synchronized(positions) {
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight() / 2, mc.thePlayer.posZ))
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY, mc.thePlayer.posZ))
        }


        pulseTimer.reset()
    }

    override fun onDisable() {
        synchronized(positions) { positions.clear() }
        if (mc.thePlayer == null) return
        BlinkUtils.setBlinkState(off = true, release = true)
        clearPackets()
        if (fakePlayer != null) {
            mc.theWorld.removeEntityFromWorld(fakePlayer!!.entityId)
            fakePlayer = null
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        synchronized(positions) {
            positions.add(
                doubleArrayOf(
                    mc.thePlayer.posX,
                    mc.thePlayer.entityBoundingBox.minY,
                    mc.thePlayer.posZ
                )
            )
        }
        if (pulseValue.get() && pulseTimer.hasTimePassed(pulseDelayValue.get().toLong())) {
            synchronized(positions) { positions.clear() }
            BlinkUtils.releasePacket()
            clearPackets()
            pulseTimer.reset()
        }
    }

    private fun clearPackets() {
        while (!packets.isEmpty()) {
            PacketUtils.handlePacket(packets.take() as Packet<INetHandlerPlayClient?>)
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (serverPacket.get()) {
            if (packet.javaClass.simpleName.startsWith("S", ignoreCase = true)) {
                if (mc.thePlayer.ticksExisted < 20) return
                event.cancelEvent()
                packets.add(packet as Packet<INetHandlerPlayClient>)
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!renderPlayer.get()) return
        val breadcrumbs = CrossSine.moduleManager[Breadcrumbs::class.java]!!
        synchronized(positions) {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            GL11.glLineWidth(2F)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            RenderUtils.glColor(breadcrumbs.color)
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ
            for (pos in positions) GL11.glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ)
            GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
            GL11.glEnd()
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
        }
    }

    override val tag: String
        get() = "" + BlinkUtils.bufferSize().toString()
}