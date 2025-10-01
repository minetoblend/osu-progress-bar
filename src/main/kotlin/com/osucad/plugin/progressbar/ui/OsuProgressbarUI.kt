package com.osucad.plugin.progressbar.ui

import com.intellij.ui.util.height
import com.intellij.ui.util.width
import com.intellij.util.ui.GraphicsUtil
import com.intellij.util.ui.JBUI
import com.osucad.plugin.progressbar.settings.OsuProgressBarSettings
import com.osucad.plugin.progressbar.settings.Skin
import com.osucad.plugin.progressbar.settings.SkinSource
import com.osucad.plugin.progressbar.utils.withTint
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.plaf.ComponentUI
import javax.swing.plaf.basic.BasicProgressBarUI
import kotlin.math.min
import kotlin.math.roundToInt

class OsuProgressbarUI : BasicProgressBarUI() {
    companion object {
        @JvmStatic
        @Suppress("ACCIDENTAL_OVERRIDE")
        fun createUI(c: JComponent): ComponentUI {
            c.border = JBUI.Borders.empty().asUIResource()
            return OsuProgressbarUI()
        }
    }

    override fun getPreferredSize(c: JComponent): Dimension {
        return Dimension(super.getPreferredSize(c).width, JBUI.scale(30))
    }

    override fun installListeners() {
        super.installListeners()

        progressBar.addComponentListener(object : ComponentAdapter() {
            override fun componentShown(e: ComponentEvent?) {
                super.componentShown(e)
            }

            override fun componentHidden(e: ComponentEvent?) {
                super.componentHidden(e)
            }
        })
    }

    override fun paintIndeterminate(g: Graphics, c: JComponent) {
        if (g !is Graphics2D)
            return

        if (progressBar.orientation != SwingConstants.HORIZONTAL || !c.componentOrientation.isLeftToRight)
            return super.paintIndeterminate(g, c)

        val settings = OsuProgressBarSettings.getInstance()

        val config = GraphicsUtil.setupAAPainting(g)
        val insets = progressBar.insets
        val width = progressBar.width
        val height = progressBar.preferredSize.height - 2

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)

        g.drawSliderBody(
            SkinSource.get { this.config }.sliderTrackOverride ?: settings.comboColor,
            0,
            (c.height - height) / 2,
            width,
            height
        )


        val scale = height / 128f

        boxRect = getBox(boxRect)

        val middleFrame = frameCount / 2

        val isReverse = animationIndex >= middleFrame

        val sliderb = SkinSource.get { sliderb }

        var sliderBIndex = animationIndex % sliderb.size
        if (isReverse)
            sliderBIndex = sliderb.lastIndex - sliderBIndex

        var spanProgress = animationIndex.toFloat() / frameCount * 2f
        if (isReverse)
            spanProgress -= 1

        val centerY = c.height / 2

        g.drawHitCircle(
            reverseArrow = SkinSource.get { reverseArrow },
            x = height / 2,
            y = centerY,
            scale = scale,
            spanProgress = spanProgress.takeIf { !isReverse },
            observer = c,
            arrowDirection = ArrowDirection.Right,
        )
        g.drawHitCircle(
            getCircleSprite = { sliderEndCircle },
            getOverlaySprite = { sliderEndCircleOverlay },
            reverseArrow = SkinSource.get { reverseArrow },
            x = width - height / 2,
            y = centerY,
            scale = scale,
            spanProgress = spanProgress.takeIf { isReverse },
            observer = c,
            arrowDirection = ArrowDirection.Left,
        )

        SkinSource.getProvider { it.sliderb != null }!!.let { skin ->
            skin.sliderbND?.let { image ->
                g.drawImageWithScale(
                    image,
                    scale,
                    boxRect.centerX.toInt(),
                    boxRect.centerY.toInt(),
                    c
                )
            }
        }

        g.drawImageWithScale(
            if (SkinSource.get { this.config }.allowSliderBallTint)
                sliderb[sliderBIndex].withTint(settings.comboColor)
            else
                sliderb[sliderBIndex],
            scale,
            boxRect.centerX.toInt(),
            boxRect.centerY.toInt(),
            c
        )

        g.drawImageWithScale(
            SkinSource.get { sliderFollowCircle },
            scale,
            boxRect.centerX.toInt(),
            boxRect.centerY.toInt(),
            c
        )

        config.restore()
    }

    private fun Graphics2D.drawHitCircle(
        getCircleSprite: Skin.() -> BufferedImage? = { hitCircle },
        getOverlaySprite: Skin.() -> BufferedImage? = { hitCircleOverlay },
        reverseArrow: BufferedImage?,
        x: Int,
        y: Int,
        scale: Float,
        spanProgress: Float?,
        observer: ImageObserver,
        arrowDirection: ArrowDirection = ArrowDirection.Left
    ) {
        val provider = SkinSource.getProvider { getCircleSprite(it) != null }
            ?: SkinSource.getProvider { it.hitCircle != null }!!

        var hasCircleSprite = true

        val circleSprite = (provider.getCircleSprite() ?: SkinSource.get { hitCircle }.also { hasCircleSprite = false })
            .withTint(OsuProgressBarSettings.getInstance().comboColor)

        val overlaySprite = if (hasCircleSprite) provider.getOverlaySprite() else SkinSource.get { hitCircleOverlay }

        drawImageWithScale(circleSprite, scale, x, y, observer)
        if (overlaySprite != null)
            drawImageWithScale(overlaySprite, scale, x, y, observer)


        if (reverseArrow != null) {
            when (arrowDirection) {
                ArrowDirection.Left -> drawImageWithScale(reverseArrow, scale, x, y, observer, flipX = true)
                ArrowDirection.Right -> drawImageWithScale(reverseArrow, scale, x, y, observer)
            }
        }

        if (spanProgress == null)
            return

        val progress = (spanProgress * 4).coerceIn(0f, 1f)

        if (progress >= 1)
            return

        val alpha = 1 - progress
        val circleScale = scale * 1f.lerp(1.4f, easeOut(progress))

        withComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)) {
            drawImageWithScale(circleSprite, circleScale, x, y, observer)
            if (overlaySprite != null)
                drawImageWithScale(overlaySprite, circleScale, x, y, observer)

            if (reverseArrow != null) {
                when (arrowDirection) {
                    ArrowDirection.Left -> drawImageWithScale(reverseArrow, circleScale, x, y, observer, flipX = true)
                    ArrowDirection.Right -> drawImageWithScale(reverseArrow, circleScale, x, y, observer)
                }
            }
        }
    }

    private inline fun Graphics2D.withComposite(composite: Composite, block: () -> Unit) {
        val oldComposite = this.composite

        try {
            this.composite = composite
            block()
        } finally {
            this.composite = oldComposite
        }
    }

    private fun Graphics2D.drawImageWithScale(
        image: BufferedImage,
        scale: Float,
        centerX: Int,
        centerY: Int,
        observer: ImageObserver,
        flipX: Boolean = false
    ) {
        val width = (image.width * scale).roundToInt() * if (flipX) -1 else 1
        val height = (image.width * scale).roundToInt()

        drawImage(
            image,
            centerX - width / 2,
            centerY - height / 2,
            width,
            height,
            observer,
        )
    }

    override fun paintDeterminate(g: Graphics, c: JComponent) {
        if (g !is Graphics2D)
            return

        if (progressBar.orientation != SwingConstants.HORIZONTAL || !c.componentOrientation.isLeftToRight)
            return super.paintDeterminate(g, c)

        val settings = OsuProgressBarSettings.getInstance()

        val config = GraphicsUtil.setupAAPainting(g)
        val insets = progressBar.insets
        val width = progressBar.width
        val height = progressBar.preferredSize.height - 2

        val centerY = c.height / 2

        val amountFull = ((width - height) * progressBar.percentComplete).toInt()

        g.drawSliderBody(
            SkinSource.get { this.config }.sliderTrackOverride ?: settings.comboColor,
            0,
            (c.height - height) / 2,
            width,
            height
        )

        val scale = height / 128f

        g.drawHitCircle(
            reverseArrow = null,
            x = height / 2 + amountFull,
            y = centerY,
            scale = scale,
            spanProgress = null,
            observer = c,
            arrowDirection = ArrowDirection.Right,
        )
        g.drawHitCircle(
            getCircleSprite = { sliderEndCircle },
            getOverlaySprite = { sliderEndCircleOverlay },
            reverseArrow = null,
            x = width - height / 2,
            y = centerY,
            scale = scale,
            spanProgress = null,
            observer = c,
            arrowDirection = ArrowDirection.Left,
        )

        val sliderb = SkinSource.get { sliderb }

        val sliderBIndex = (amountFull / 4) % sliderb.size


        SkinSource.getProvider { it.sliderb != null }!!.let { skin ->
            skin.sliderbND?.let { image ->
                g.drawImageWithScale(
                    image,
                    scale,
                    amountFull + height / 2,
                    centerY,
                    c
                )
            }
        }

        g.drawImageWithScale(sliderb[sliderBIndex], scale, amountFull + height / 2, centerY, c)

        g.drawImageWithScale(
            SkinSource.get { sliderFollowCircle },
            scale,
            amountFull + height / 2,
            centerY,
            c
        )

        config.restore()
    }

    private fun Graphics2D.drawSliderBody(
        accentColor: Color,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        val (positions, colors) = sliderBodyGradientStops(accentColor)

        paint = LinearGradientPaint(
            0f,
            y.toFloat(),
            0f,
            y + height.toFloat(),
            positions,
            colors,
        )

        fillRect(x + height / 2, y, width - height, height)

        paint = RadialGradientPaint(
            Point2D.Float(
                x + height / 2f,
                y + height / 2f
            ),
            height / 2f,
            positions.take(6).map { 1 - it * 2 }.reversed().toFloatArray(),
            colors.take(6).reversed().toTypedArray(),
        )

        fillRect(x, y, height / 2, height)

        paint = RadialGradientPaint(
            Point2D.Float(
                x + width - height / 2f,
                y + height / 2f
            ),
            height / 2f,
            positions.take(6).map { 1 - it * 2 }.reversed().toFloatArray(),
            colors.take(6).reversed().toTypedArray(),
        )

        fillRect(x + width - height / 2, y, height / 2, height)
    }

    private fun sliderBodyGradientStops(accentColor: Color): Pair<FloatArray, Array<Color>> {
        val shadowPortion = 1f - (59f / 64f)
        val borderPortion = 0.1875f

        val outerShadowColor = Color.BLACK.opacity(0f)
        val innerShadowColor = Color.BLACK.opacity(0.25f)
        val borderColor = SkinSource.get { config }.sliderBorder

        val outerColor = accentColor.darken(0.1f).opacity(0.7f)
        val innerColor = accentColor.lighten(0.5f).opacity(0.7f)
        val aaWidth = 0.01f

        val stops = floatArrayOf(
            0f,
            shadowPortion / 2,
            shadowPortion / 2 + aaWidth,
            borderPortion / 2,
            borderPortion / 2 + aaWidth,
            0.5f,
            1f - borderPortion / 2 - aaWidth,
            1f - borderPortion / 2,
            1f - shadowPortion / 2 - aaWidth,
            1f - shadowPortion / 2,
            1f,
        )
        val colors = arrayOf(
            outerShadowColor,
            innerShadowColor,
            borderColor,
            borderColor,
            outerColor,
            innerColor,
            outerColor,
            borderColor,
            borderColor,
            innerShadowColor,
            outerShadowColor,
        )

        return Pair(stops, colors)
    }

    private fun Color.opacity(alpha: Float) = Color(red, green, blue, (alpha * 255).toInt())

    private operator fun Color.times(amount: Float) = Color(
        red / 255f * amount,
        green / 255f * amount,
        blue / 255f * amount,
        alpha / 255f
    )

    private fun Color.darken(amount: Float) = this * (1 / (1 + amount))

    private fun Color.lighten(amount: Float): Color {
        val x = amount * 0.5f

        return Color(
            min(1f, red / 255f * (1 + 0.5f * x) + x),
            min(1f, green / 255f * (1 + 0.5f * x) + x),
            min(1f, blue / 255f * (1 + 0.5f * x) + x),
            alpha / 255f
        )
    }

    override fun getBoxLength(availableLength: Int, otherDimension: Int): Int {
        return otherDimension
    }

    override fun getBox(r: Rectangle): Rectangle = super.getBox(r).scale(2.4f)

    private fun Float.lerp(other: Float, factor: Float) = this + (other - this) * factor

    private fun easeOut(x: Float) = 1 - ((1 - x) * (1 - x))

    private fun Rectangle.scale(scale: Float): Rectangle {
        val newWidth = width * scale
        val newHeight = height * scale

        x += ((width - newWidth) / 2).toInt()
        y += ((height - newHeight) / 2).toInt()
        width = newWidth.toInt()
        height = newHeight.toInt()

        return this
    }

    override fun incrementAnimationIndex() {
        super.incrementAnimationIndex()
        progressBar.repaint()
    }

    private enum class ArrowDirection {
        Left,
        Right,
    }
}