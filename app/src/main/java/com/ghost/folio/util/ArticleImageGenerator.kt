package com.ghost.folio.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.ghost.folio.data.model.Article
import com.ghost.folio.data.model.BodyBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ArticleImageGenerator {

    private const val CANVAS_WIDTH = 1080
    private const val CANVAS_HEIGHT = 1920
    private const val HORIZONTAL_PADDING = 72
    private const val TOP_PADDING = 120
    private const val CONTENT_WIDTH = CANVAS_WIDTH - (HORIZONTAL_PADDING * 2)

    private const val COLOR_CHAMPION_BLUE = 0xFF151130.toInt()
    private const val COLOR_LAVENDER_MIST = 0xFFC8C5F0.toInt()
    private const val COLOR_WHITE_CONVOLVULUS = 0xFFF5F2F3.toInt()
    private const val COLOR_MUTED_DARK = 0xFF6B6880.toInt()

    suspend fun generateAndSaveImage(context: Context, article: Article): File = withContext(Dispatchers.IO) {
        val bitmap = Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background full bleed
        canvas.drawColor(COLOR_CHAMPION_BLUE)

        var currentY = TOP_PADDING.toFloat()

        // 2. Top Section: Category Label
        val categoryPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_LAVENDER_MIST
            textSize = 39f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.15f
        }

        val categoryText = article.category.replace("-", " ").uppercase()
        canvas.drawText(categoryText, HORIZONTAL_PADDING.toFloat(), currentY + 35f, categoryPaint)
        currentY += 35f + 72f // Text baseline + 24dp gap

        // 3. Top Section: Article Title
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_WHITE_CONVOLVULUS
            textSize = 90f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        val titleLayout = createStaticLayout(
            text = article.title,
            paint = titlePaint,
            width = CONTENT_WIDTH,
            maxLines = 3,
            lineSpacingMultiplier = 1.15f
        )

        canvas.save()
        canvas.translate(HORIZONTAL_PADDING.toFloat(), currentY)
        titleLayout.draw(canvas)
        canvas.restore()
        currentY += titleLayout.height + 96f // Title height + 32dp margin

        // 4. Divider Line
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(76, 200, 197, 240) // LavenderMist at 0.3 alpha
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(
            HORIZONTAL_PADDING.toFloat(),
            currentY,
            (CANVAS_WIDTH - HORIZONTAL_PADDING).toFloat(),
            currentY,
            dividerPaint
        )
        currentY += 96f // 32dp margin bottom

        // 5. Body Section: First Definition Block
        val definitionBlock = article.body.filterIsInstance<BodyBlock.Definition>().firstOrNull()
        if (definitionBlock != null) {
            val termPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_LAVENDER_MIST
                textSize = 46f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }

            canvas.drawText(definitionBlock.term, HORIZONTAL_PADDING.toFloat(), currentY + 40f, termPaint)
            currentY += 40f + 20f

            val defTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(217, 245, 242, 243) // WhiteConvolvulus at 0.85 alpha
                textSize = 46f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }

            val defLayout = createStaticLayout(
                text = definitionBlock.definition,
                paint = defTextPaint,
                width = CONTENT_WIDTH,
                maxLines = 5,
                lineSpacingMultiplier = 1.55f
            )

            canvas.save()
            canvas.translate(HORIZONTAL_PADDING.toFloat(), currentY)
            defLayout.draw(canvas)
            canvas.restore()
            currentY += defLayout.height + 72f // 24dp gap
        }

        // 6. Body Section: First Paragraph Block
        val paragraphBlock = article.body.filterIsInstance<BodyBlock.Paragraph>().firstOrNull()
        if (paragraphBlock != null) {
            val paragraphPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(178, 245, 242, 243) // WhiteConvolvulus at 0.7 alpha
                textSize = 43f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }

            val paragraphLayout = createStaticLayout(
                text = paragraphBlock.text,
                paint = paragraphPaint,
                width = CONTENT_WIDTH,
                maxLines = 6,
                lineSpacingMultiplier = 1.6f
            )

            canvas.save()
            canvas.translate(HORIZONTAL_PADDING.toFloat(), currentY)
            paragraphLayout.draw(canvas)
            canvas.restore()
        }

        // 7. Bottom Section (Pinned to Bottom)
        val bottomLineY = (CANVAS_HEIGHT - 130).toFloat()
        val bottomDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(38, 200, 197, 240) // LavenderMist at 0.15 alpha
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(
            HORIZONTAL_PADDING.toFloat(),
            bottomLineY,
            (CANVAS_WIDTH - HORIZONTAL_PADDING).toFloat(),
            bottomLineY,
            bottomDividerPaint
        )

        val bottomTextY = bottomLineY + 65f
        val bottomPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_MUTED_DARK
            textSize = 36f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        // Left text: "folio"
        canvas.drawText("folio", HORIZONTAL_PADDING.toFloat(), bottomTextY, bottomPaint)

        // Right text: article URL slug
        val slugText = article.id
        val slugWidth = bottomPaint.measureText(slugText)
        val slugX = (CANVAS_WIDTH - HORIZONTAL_PADDING).toFloat() - slugWidth
        canvas.drawText(slugText, slugX, bottomTextY, bottomPaint)

        // 8. Save PNG to cacheDir
        val cacheFolder = File(context.cacheDir, "shared_articles").apply { if (!exists()) mkdirs() }
        val outputFile = File(cacheFolder, "folio_${article.id}.png")
        FileOutputStream(outputFile).use { outStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.flush()
        }

        outputFile
    }

    private fun createStaticLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        maxLines: Int,
        lineSpacingMultiplier: Float
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, lineSpacingMultiplier)
                .setIncludePad(false)
                .setMaxLines(maxLines)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                lineSpacingMultiplier,
                0f,
                false
            )
        }
    }
}
