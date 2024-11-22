package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.astolfo.buttons.value

import net.ccbluex.liquidbounce.features.value.FontValue
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.astolfo.AstolfoConstants.BACKGROUND_VALUE
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.astolfo.AstolfoConstants.SELECTED_FORMAT
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.astolfo.drawHeightCenteredString
import net.ccbluex.liquidbounce.utils.CPSCounter.MouseButton
import net.ccbluex.liquidbounce.utils.FontUtils
import net.ccbluex.liquidbounce.utils.geom.Rectangle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import java.awt.Color

class FontValueButton(x: Float, y: Float, width: Float, height: Float, var setting: FontValue, var color: Color) : BaseValueButton(x, y, width, height, setting) {
  private val listEntryBoxPairs = mutableListOf<Pair<Rectangle, String>>()

  override fun drawPanel(mouseX: Int, mouseY: Int): Rectangle {
    val background = Rectangle(x, y, width, height)
    drawRect(background, BACKGROUND_VALUE)
    FONT.drawHeightCenteredString(setting.name, x + hOffset, y + height / 2, -0x1)

    //		val format = setting.get()
    //		val formatWidth = FONT.getStringWidth(format)
    //		FONT.drawHeightCenteredString(format, x + width - formatWidth - hOffset, y + height / 2, -0x1)
    //
    var count = 0
    listEntryBoxPairs.clear()
    if (setting.openList) {
      for (fontPair in FontUtils.getAllFontDetails()) {
        val rect = Rectangle(x, y + (count + 1) * height, width, height)
        listEntryBoxPairs.add(rect to fontPair.first)
        drawRect(rect, BACKGROUND_VALUE)

        val listEntryText = (if (setting.get() === fontPair.second) SELECTED_FORMAT else "") + fontPair.first
        FONT.drawHeightCenteredString(listEntryText, rect.x + width - FONT.getStringWidth(listEntryText) - hOffset, rect.y + height / 2, if (setting.get() == fontPair.second) color.rgb else Color(128, 128, 128).rgb)
        count++
      }
    }

    background.height += count * height

    return background
  }

  override fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int) {
    if (click) {
      when (button) {
        MouseButton.LEFT.ordinal -> { //					if (baseRect.contains(mouseX, mouseY)) // clicked on the button with value name
          //						setting.nextValue()
          for (pair in listEntryBoxPairs) {
            if (pair.first.contains(mouseX, mouseY)) setting.set(FontUtils.getAllFontDetails().filter { it.first == pair.second }[0].second)
          }
        }

        MouseButton.RIGHT.ordinal -> {
          if (baseRect.contains(mouseX, mouseY)) setting.openList = !setting.openList
        }
      }
    }
  }
}
