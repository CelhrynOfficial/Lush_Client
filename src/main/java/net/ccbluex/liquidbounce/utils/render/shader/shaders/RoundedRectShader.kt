package net.ccbluex.liquidbounce.utils.render.shader.shaders

import net.ccbluex.liquidbounce.utils.render.shader.Shader
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs

/**
 * @author inf, remoted through pie's pc (shader not by me though)
 */
object RoundedRectShader : Shader("roundedrect.frag") {
    override fun setupUniforms() {
        setupUniform("u_size")
        setupUniform("u_radius")
        setupUniform("u_color")
    }
    override fun updateUniforms() {
        // ignore
    }
        @Suppress("NOTHING_TO_INLINE")
        inline fun draw(x: Float, y: Float, x2: Float, y2: Float, radius: Float, color: Color): RoundedRectShader {
            val width = abs(x2 - x)
            val height = abs(y2 - y)


            RoundedRectShader.startShader()

            RoundedRectShader.setUniformf("u_size", width, height)
            RoundedRectShader.setUniformf("u_radius", radius)
            RoundedRectShader.setUniformf("u_color", color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

            GlStateManager.enableBlend()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.enableAlpha()
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f)
            drawQuad(x, y, width, height)
            GlStateManager.disableBlend()

            RoundedRectShader.stopShader()

            return RoundedRectShader
    }
}