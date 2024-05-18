package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.newVer

import net.ccbluex.liquidbounce.CrossSine
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.newVer.element.CategoryElement
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.newVer.element.SearchElement
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule
import net.ccbluex.liquidbounce.ui.client.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.ui.client.gui.colortheme.GuiTheme
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.MouseUtils.mouseWithinBounds
import net.ccbluex.liquidbounce.utils.geom.Rectangle
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import org.apache.commons.lang3.tuple.MutablePair
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.IOException
import java.util.function.Consumer
import kotlin.math.abs

/**
 * @author inf (original java code)
 * @author pie (refactored)
 */
class NewUi : GuiScreen() {
    private val categoryElements: MutableList<CategoryElement> = ArrayList()
    private var startYAnim = height / 2f
    private var endYAnim = height / 2f
    private var searchElement: SearchElement? = null

    private val backgroundColor = Color(16, 16, 16, 255)
    private val backgroundColor2 = Color(40, 40, 40, 255)

    var windowXStart = 30f
    var windowYStart = 30f
    var windowXEnd = 500f
    var windowYEnd = 400f
    private val windowWidth
        get() = abs(windowXEnd - windowXStart)
    private val windowHeight
        get() = abs(windowYEnd - windowYStart)
    private val minWindowWidth = 475f
    private val minWindowHeight = 350f

    private val searchXOffset = 10f
    private val searchYOffset = 30f

    var sideWidth = 120f
    private val categoryXOffset
        get() = sideWidth
    private val searchWidth
        get() = sideWidth - 10f
    private val searchHeight = 20f

    private val elementHeight = 24f
    private val elementsStartY = 55f

    private val categoriesTopMargin = 20f
    private val categoriesBottommargin = 20f

    private val xButtonColor = Color(0.2f, 0f, 0f, 1f)

    private var moveDragging = false
    private var resizeDragging = false
    private var splitDragging = false

    private var quad = Pair(0, 0)
    private val resizeArea = 12f
    private var x2 = 0f
    private var y2 = 0f
    private var xHoldOffset = 0f
    private var yHoldOffset = 0f

    private val moveAera
        get() = Rectangle(windowXStart, windowYStart, windowWidth - 20f, 20f)
    private val splitArea
        get() = Rectangle(windowXStart + sideWidth - 5, windowYStart, 10f, windowHeight)

    private var closed = false
    var cant = false
    private var animProgress = 0F

    init {
        ModuleCategory.values().forEach { categoryElements.add(CategoryElement(it)) }
        searchElement = SearchElement(windowXStart + searchXOffset, windowYStart + searchYOffset, searchWidth, searchHeight)
        categoryElements[0].focused = true
    }

    private fun reload() {
        categoryElements.clear()
        ModuleCategory.values().forEach { categoryElements.add(CategoryElement(it)) }
        categoryElements[0].focused = true
    }


    private fun determineQuadrant(mouseX: Int, mouseY: Int): Pair<Int, Int> {
        val result = MutablePair(0, 0)
        val offset2 = 0f
        if (mouseX.toFloat() in windowXStart-resizeArea..windowXStart-offset2) {
            result.left = -1
            xHoldOffset = mouseX - windowXStart
        }
        if (mouseX.toFloat() in windowXEnd+offset2..windowXEnd+resizeArea) {
            result.left = 1
            xHoldOffset = mouseX - windowXEnd
        }
        if (mouseY.toFloat() in windowYStart-resizeArea..windowYStart-offset2) {
            result.right = 1
            yHoldOffset = mouseY - windowYStart
        }
        if (mouseY.toFloat() in windowYEnd+offset2..windowYEnd+resizeArea) {
            result.right = -1
            yHoldOffset = mouseY - windowYEnd
        }
        return result.toPair()
    }

    private fun handleMove(mouseX: Int, mouseY: Int) {
        if (moveDragging) {
            val w = windowWidth
            val h = windowHeight
            windowXStart = mouseX + x2
            windowYStart = mouseY + y2
            windowXEnd = windowXStart + w
            windowYEnd = windowYStart + h
        }
    }

    private fun handleResize(mouseX: Int, mouseY: Int) {
        val mouseX = mouseX - xHoldOffset
        val mouseY = mouseY - yHoldOffset
        if (resizeDragging) {
            val triangleColor = Color(255, 255, 255)
            when (quad.first to quad.second) {
                1 to 1 -> {
                    windowXEnd = mouseX.coerceAtLeast(windowXStart + minWindowWidth)
                    windowYStart = mouseY.coerceAtMost(windowYEnd - minWindowHeight)
                    RenderUtils.drawSquareTriangle(windowXEnd + resizeArea, windowYStart - resizeArea, -resizeArea, resizeArea, triangleColor, true)
                }
                -1 to -1 -> {
                    windowXStart = mouseX.coerceAtMost(windowXEnd - minWindowWidth)
                    windowYEnd = mouseY.coerceAtLeast(windowYStart + minWindowHeight)
                    RenderUtils.drawSquareTriangle(windowXStart - resizeArea, windowYEnd + resizeArea, resizeArea, -resizeArea, triangleColor, true)
                }

                -1 to 1 -> {
                    windowXStart = mouseX.coerceAtMost(windowXEnd - minWindowWidth)
                    windowYStart = mouseY.coerceAtMost(windowYEnd - minWindowHeight)
                    RenderUtils.drawSquareTriangle(windowXStart - resizeArea, windowYStart - resizeArea, resizeArea, resizeArea, triangleColor, true)
                }
                1 to -1 -> {
                    windowXEnd = mouseX.coerceAtLeast(windowXStart + minWindowWidth)
                    windowYEnd = mouseY.coerceAtLeast(windowYStart + minWindowHeight)
                    RenderUtils.drawSquareTriangle(windowXEnd + resizeArea, windowYEnd + resizeArea, -resizeArea, -resizeArea, triangleColor, true)
                }
            }
        }
    }

    private fun resetPositions() {
        windowXStart = 30f
        windowYStart = 30f
        windowXEnd = 500f
        windowYEnd = 400f
        resizeDragging = false
        moveDragging = false
    }

    private fun handleSplit(mouseX: Int) {
        if (splitDragging) {
            sideWidth = (mouseX - windowXStart).coerceIn(80f, windowWidth/2)
        }
    }

    private fun handleMisc() {
        if (Keyboard.isKeyDown(Keyboard.KEY_F12)) {
            resetPositions()
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_F5)) {
            reload()
        }
    }

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        categoryElements.forEach { cat ->
            cat.moduleElements.filter { it.listeningKeybind() }.forEach { mod ->
                mod.resetState()
            }
        }

        super.initGui()
    }

    override fun onGuiClosed() {
        categoryElements.filter { it.focused }.map { it.handleMouseRelease(-1, -1, 0, 0f, 0f, 0f, 0f) }
        moveDragging = false
        resizeDragging = false
        splitDragging = false
        closed = false
        animProgress = 0F
        Keyboard.enableRepeatEvents(false)
        CrossSine.fileManager.saveConfigs()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        animProgress += (0.0075F * 0.25F * RenderUtils.deltaTime * if (closed) -1F else 1F)
        animProgress = animProgress.coerceIn(0F, 1F)
        if (closed && animProgress == 0F) {
            mc.displayGuiScreen(null)
        }
        val percent = EaseUtils.easeOutBack(animProgress.toDouble()).toFloat()
        GL11.glPushMatrix()
        GL11.glScalef(percent, percent, percent)
        GL11.glTranslatef(((windowXEnd * 0.5f * (1 - percent)) / percent), ((windowYEnd * 0.5f * (1 - percent)) / percent), 0.0F)
        handleMisc()
        handleMove(mouseX, mouseY)
        handleResize(mouseX, mouseY)
        handleSplit(mouseX)
        drawFullSized(mouseX, mouseY, partialTicks, ClientTheme.getColor(1))
        GL11.glPopMatrix()
    }

    private fun drawFullSized(mouseX: Int, mouseY: Int, partialTicks: Float, accentColor: Color, xOffset: Float = 0f, yOffset: Float = 0f) {
        val windowRadius = 4f
        RenderUtils.drawRoundedRect((windowXStart + xOffset), (windowYStart + yOffset), (windowXEnd + xOffset), (windowYEnd + yOffset), windowRadius, backgroundColor.rgb)
        RenderUtils.customRounded((windowXStart + xOffset), (windowYStart + yOffset), (windowXEnd + xOffset), (windowYStart + yOffset) + 20f, windowRadius, windowRadius, 0f, 0f, backgroundColor2.rgb)

        if (mouseX.toFloat() in (windowXEnd - 20F)..(windowXEnd) && mouseY.toFloat() in (windowYStart)..(windowYStart + 20F))
            RenderUtils.customRounded((windowXEnd + xOffset) - 20f, (windowYStart + yOffset), (windowXEnd + xOffset), (windowYStart + yOffset) + 20f, 0f, windowRadius, 0f, 0f, xButtonColor.rgb)
        GlStateManager.disableAlpha()
        RenderUtils.drawImage(IconManager.removeIcon, (windowXEnd + xOffset).toInt() - 15, (windowYStart + yOffset).toInt() + 5, 10, 10)
        RenderUtils.drawImage(IconManager.brush, (windowXStart + xOffset).toInt() + 6, (windowYEnd + yOffset).toInt() - 30, 24,24)
        RenderUtils.drawImage(IconManager.paint, (windowXStart + xOffset).toInt() + 6, (windowYEnd + yOffset).toInt() - 60, 24,24)
        GlStateManager.enableAlpha()

        searchElement!!.xPos = (windowXStart + xOffset) + searchXOffset
        searchElement!!.yPos = (windowYStart + yOffset) + searchYOffset
        searchElement!!.width = searchWidth

        searchElement!!.searchBox.width = searchWidth.toInt() - 4
        searchElement!!.searchBox.xPosition = ((windowXStart + xOffset) + searchXOffset + 2).toInt()
        searchElement!!.searchBox.yPosition = ((windowYStart + yOffset) + searchYOffset + 2).toInt()

        if (searchElement!!.drawBox(mouseX, mouseY, accentColor)) {
            searchElement!!.drawPanel(mouseX, mouseY, (windowXStart + xOffset) + categoryXOffset, (windowYStart + yOffset) + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin, Mouse.getDWheel(), categoryElements, accentColor)
            return
        }

        var startY = (windowYStart + yOffset) + elementsStartY
        var lastFastYStart = 0f
        var lastFastYEnd = 0f

        for (ce in categoryElements) {
            ce.drawLabel(mouseX, mouseY, (windowXStart + xOffset), startY, categoryXOffset, elementHeight)
            if (ce.focused) {
                lastFastYStart = startY + 6f
                lastFastYEnd = startY + elementHeight - 6f
                startYAnim = if (ClickGUIModule.fastRenderValue.get())
                    startY + 6f
                             else
                                 AnimationUtils.animate(startY + 6f,
                                    startYAnim,
                                    (if (startYAnim - (startY + 5f) > 0) 0.65f else 0.55f) * RenderUtils.deltaTime * 0.025f
                                )
                endYAnim =  if (ClickGUIModule.fastRenderValue.get())
                                startY + elementHeight - 6f
                            else
                                AnimationUtils.animate(
                                    startY + elementHeight - 6f,
                                    endYAnim,
                                    (if (endYAnim - (startY + elementHeight - 5f) < 0) 0.65f else 0.55f) * RenderUtils.deltaTime * 0.025f
                                )
                ce.drawPanel(mouseX, mouseY, (windowXStart + xOffset) + categoryXOffset, (windowYStart + yOffset) + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin, Mouse.getDWheel(), accentColor)
                Fonts.SFApple40.drawStringWithShadow(ce.name, (windowXStart + xOffset) + 7, (windowYStart + yOffset) + 7, -1)
            }
            startY += elementHeight
        }
        val offset = 8f
        val drawYStart = if (resizeDragging || moveDragging) lastFastYStart else startYAnim
        val drawYEnd = if (resizeDragging || moveDragging) lastFastYEnd else endYAnim
        RenderUtils.drawRoundedRect((windowXStart + xOffset) + 2f + offset, drawYStart, (windowXStart + xOffset) + 4f + offset, drawYEnd, 1f, accentColor.rgb)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (searchElement!!.isTyping() && Rectangle(windowXStart, windowYStart, 60f, 24f).contains(mouseX, mouseY)) {
            searchElement!!.searchBox.text = ""
            return
        }

        if (moveAera.contains(mouseX, mouseY) && !moveDragging) {
            moveDragging = true
            x2 = windowXStart - mouseX
            y2 = windowYStart - mouseY
            return
        }

        if (Rectangle(windowXEnd - 20, windowYStart, 20f, 20f).contains(mouseX, mouseY)) {
            mc.displayGuiScreen(null)
            return
        }
        if (Rectangle(windowXStart, windowYEnd - 40, 40f, 40f).contains(mouseX, mouseY)) {
            mc.displayGuiScreen(GuiHudDesigner())
            return
        }
        if (Rectangle(windowXStart, windowYEnd - 80, 40f, 40f).contains(mouseX, mouseY)) {
            mc.displayGuiScreen(GuiTheme())
            return
        }

        if (splitArea.contains(mouseX, mouseY)) {
            splitDragging = true
            return
        }

        val quad2 = determineQuadrant(mouseX, mouseY)
        if (quad2.first != 0 && quad2.second != 0) {
            quad = quad2
            resizeDragging = true
            return
        }


        var startY = windowYStart + elementsStartY

        searchElement!!.handleMouseClick(mouseX, mouseY, mouseButton, windowXStart + categoryXOffset, windowYStart + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin, categoryElements)
        if (!searchElement!!.isTyping()) {
            categoryElements.forEach { cat ->
                if (cat.focused)
                    cat.handleMouseClick(mouseX, mouseY, mouseButton, windowXStart + categoryXOffset, windowYStart + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin)
                if (mouseWithinBounds(mouseX, mouseY, windowXStart, startY, windowXStart + categoryXOffset, startY + elementHeight) && !searchElement!!.isTyping()) {
                    categoryElements.forEach(Consumer { e: CategoryElement -> e.focused = false })
                    cat.focused = true
                    return
                }
                startY += elementHeight
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (moveDragging && moveAera.contains(mouseX, mouseY)) {
            moveDragging = false
            return
        }

        if (resizeDragging)
            resizeDragging = false

        if (splitDragging)
            splitDragging = false

        searchElement!!.handleMouseRelease(mouseX, mouseY, state, windowXStart + categoryXOffset, windowYStart + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin, categoryElements)
        if (!searchElement!!.isTyping()) {
            categoryElements.filter { it.focused }.forEach { cat ->
                cat.handleMouseRelease(mouseX, mouseY, state, windowXStart + categoryXOffset, windowYStart + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin)
            }
        }
        super.mouseReleased(mouseX, mouseY, state)
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == 1 && !cant) {
            closed = true
            return
        }
        categoryElements.filter { it.focused }.forEach { cat ->
            if (cat.handleKeyTyped(typedChar, keyCode)) return
        }
        if (searchElement!!.handleTyping(typedChar, keyCode, windowXStart + categoryXOffset, windowYStart + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin, categoryElements))
            return
        super.keyTyped(typedChar, keyCode)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    companion object {
        private var instance: NewUi? = null
        fun getInstance(): NewUi {
            return if (instance == null) NewUi().also { instance = it } else instance!!
        }

        fun resetInstance() {
            instance = NewUi()
        }
    }
}
