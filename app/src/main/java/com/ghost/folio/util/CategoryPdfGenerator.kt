package com.ghost.folio.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.ghost.folio.data.model.Article
import com.ghost.folio.data.model.BodyBlock
import com.ghost.folio.data.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object CategoryPdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN_LEFT = 48f
    private const val MARGIN_RIGHT = 547f
    private const val MARGIN_TOP = 48f
    private const val MARGIN_BOTTOM = 794f
    private const val CONTENT_WIDTH = (MARGIN_RIGHT - MARGIN_LEFT).toInt()

    private const val COLOR_PRIMARY = 0xFF151130.toInt()
    private const val COLOR_TEXT_DARK = 0xFF1A1A1A.toInt()
    private const val COLOR_TEXT_MUTED = 0xFF6B6880.toInt()
    private const val COLOR_BORDER = 0xFFD8D5E0.toInt()
    private const val COLOR_BOX_BG = 0xFFF5F3F8.toInt()

    suspend fun generateCategoryPdf(
        context: Context,
        category: Category,
        articles: List<Article>
    ): File = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        var pageNumber = 1

        val pagesInfoList = mutableListOf<PdfDocument.Page>()

        for (article in articles) {
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            var currentY = MARGIN_TOP

            // Draw Article Header
            val categoryPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_TEXT_MUTED
                textSize = 9f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                letterSpacing = 0.10f
            }
            canvas.drawText(category.label.uppercase(), MARGIN_LEFT, currentY + 9f, categoryPaint)
            currentY += 18f

            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_PRIMARY
                textSize = 22f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            }
            val titleLayout = createStaticLayout(article.title, titlePaint, CONTENT_WIDTH, 1.15f)
            canvas.save()
            canvas.translate(MARGIN_LEFT, currentY)
            titleLayout.draw(canvas)
            canvas.restore()
            currentY += titleLayout.height + 12f

            // Header Divider Line
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_BORDER
                strokeWidth = 0.75f
                style = Paint.Style.STROKE
            }
            canvas.drawLine(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY, dividerPaint)
            currentY += 16f

            // Render Body Blocks
            for (block in article.body) {
                // Check if block exceeds page height
                if (currentY > MARGIN_BOTTOM - 60f) {
                    drawFooter(canvas, pageNumber)
                    pdfDocument.finishPage(page)
                    pageNumber++

                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = MARGIN_TOP

                    // Continuation header
                    val contPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = COLOR_TEXT_MUTED
                        textSize = 9f
                        typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                    }
                    canvas.drawText("${article.title} (Continued)", MARGIN_LEFT, currentY + 9f, contPaint)
                    currentY += 16f
                    canvas.drawLine(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY, dividerPaint)
                    currentY += 16f
                }

                when (block) {
                    is BodyBlock.Paragraph -> {
                        val pPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_TEXT_DARK
                            textSize = 10.5f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                        }
                        val layout = createStaticLayout(block.text, pPaint, CONTENT_WIDTH, 1.45f)

                        if (currentY + layout.height > MARGIN_BOTTOM) {
                            drawFooter(canvas, pageNumber)
                            pdfDocument.finishPage(page)
                            pageNumber++

                            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            currentY = MARGIN_TOP
                        }

                        canvas.save()
                        canvas.translate(MARGIN_LEFT, currentY)
                        layout.draw(canvas)
                        canvas.restore()
                        currentY += layout.height + 12f
                    }

                    is BodyBlock.Heading -> {
                        currentY += 6f
                        val hPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_PRIMARY
                            textSize = 13.5f
                            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                        }
                        val layout = createStaticLayout(block.text, hPaint, CONTENT_WIDTH, 1.2f)
                        canvas.save()
                        canvas.translate(MARGIN_LEFT, currentY)
                        layout.draw(canvas)
                        canvas.restore()
                        currentY += layout.height + 8f
                    }

                    is BodyBlock.Definition -> {
                        val termPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_PRIMARY
                            textSize = 10.5f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        }
                        val defPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_TEXT_DARK
                            textSize = 10f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                        }

                        val innerWidth = CONTENT_WIDTH - 20
                        val termLayout = createStaticLayout(block.term, termPaint, innerWidth, 1.2f)
                        val defLayout = createStaticLayout(block.definition, defPaint, innerWidth, 1.4f)
                        val boxHeight = termLayout.height + defLayout.height + 20f

                        if (currentY + boxHeight > MARGIN_BOTTOM) {
                            drawFooter(canvas, pageNumber)
                            pdfDocument.finishPage(page)
                            pageNumber++

                            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            currentY = MARGIN_TOP
                        }

                        // Draw definition background box
                        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_BOX_BG
                            style = Paint.Style.FILL
                        }
                        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_BORDER
                            style = Paint.Style.STROKE
                            strokeWidth = 0.75f
                        }
                        val rect = RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + boxHeight)
                        canvas.drawRoundRect(rect, 4f, 4f, bgPaint)
                        canvas.drawRoundRect(rect, 4f, 4f, strokePaint)

                        canvas.save()
                        canvas.translate(MARGIN_LEFT + 10f, currentY + 10f)
                        termLayout.draw(canvas)
                        canvas.translate(0f, termLayout.height.toFloat() + 4f)
                        defLayout.draw(canvas)
                        canvas.restore()

                        currentY += boxHeight + 12f
                    }

                    is BodyBlock.BulletList -> {
                        val bulletPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_TEXT_DARK
                            textSize = 10f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                        }

                        for (item in block.items) {
                            val itemLayout = createStaticLayout("•  $item", bulletPaint, CONTENT_WIDTH - 12, 1.35f)
                            if (currentY + itemLayout.height > MARGIN_BOTTOM) {
                                drawFooter(canvas, pageNumber)
                                pdfDocument.finishPage(page)
                                pageNumber++

                                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                                page = pdfDocument.startPage(pageInfo)
                                canvas = page.canvas
                                currentY = MARGIN_TOP
                            }

                            canvas.save()
                            canvas.translate(MARGIN_LEFT + 12f, currentY)
                            itemLayout.draw(canvas)
                            canvas.restore()
                            currentY += itemLayout.height + 4f
                        }
                        currentY += 8f
                    }

                    is BodyBlock.Note -> {
                        val notePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_TEXT_MUTED
                            textSize = 9.5f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                        }
                        val layout = createStaticLayout(block.text, notePaint, CONTENT_WIDTH - 18, 1.35f)
                        val noteHeight = layout.height + 10f

                        if (currentY + noteHeight > MARGIN_BOTTOM) {
                            drawFooter(canvas, pageNumber)
                            pdfDocument.finishPage(page)
                            pageNumber++

                            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            currentY = MARGIN_TOP
                        }

                        // Left vertical accent bar
                        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_PRIMARY
                            strokeWidth = 2.5f
                            style = Paint.Style.STROKE
                        }
                        canvas.drawLine(MARGIN_LEFT, currentY, MARGIN_LEFT, currentY + noteHeight, barPaint)

                        canvas.save()
                        canvas.translate(MARGIN_LEFT + 10f, currentY + 5f)
                        layout.draw(canvas)
                        canvas.restore()

                        currentY += noteHeight + 12f
                    }

                    is BodyBlock.Comparison -> {
                        val cellPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_TEXT_DARK
                            textSize = 8.5f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                        }
                        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_PRIMARY
                            textSize = 8.5f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        }

                        val colCount = maxOf(1, block.headers.size)
                        val colWidth = CONTENT_WIDTH / colCount.toFloat()
                        val rowHeight = 22f
                        val totalTableHeight = (block.rows.size + 1) * rowHeight

                        if (currentY + totalTableHeight > MARGIN_BOTTOM) {
                            drawFooter(canvas, pageNumber)
                            pdfDocument.finishPage(page)
                            pageNumber++

                            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            currentY = MARGIN_TOP
                        }

                        // Header row background
                        val headerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_BOX_BG
                            style = Paint.Style.FILL
                        }
                        canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + rowHeight, headerBg)

                        // Draw headers
                        for (i in block.headers.indices) {
                            val hText = block.headers[i]
                            val x = MARGIN_LEFT + (i * colWidth) + 6f
                            canvas.drawText(hText, x, currentY + 14f, headerPaint)
                        }
                        currentY += rowHeight

                        // Draw rows
                        for (row in block.rows) {
                            val labelX = MARGIN_LEFT + 6f
                            canvas.drawText(row.label, labelX, currentY + 14f, headerPaint)

                            for (j in row.values.indices) {
                                if (j + 1 < colCount) {
                                    val valX = MARGIN_LEFT + ((j + 1) * colWidth) + 6f
                                    val valText = row.values[j]
                                    canvas.drawText(valText.take(28), valX, currentY + 14f, cellPaint)
                                }
                            }
                            currentY += rowHeight
                        }

                        // Outer border
                        val tableBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_BORDER
                            strokeWidth = 0.5f
                            style = Paint.Style.STROKE
                        }
                        canvas.drawRect(MARGIN_LEFT, currentY - totalTableHeight, MARGIN_RIGHT, currentY, tableBorder)
                        currentY += 12f
                    }

                    is BodyBlock.Diagram -> {
                        val diagPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = COLOR_TEXT_MUTED
                            textSize = 9f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                        }
                        canvas.drawText("(diagram available in app)", MARGIN_LEFT, currentY + 10f, diagPaint)
                        currentY += 20f
                    }
                }
            }

            drawFooter(canvas, pageNumber)
            pdfDocument.finishPage(page)
            pageNumber++
        }

        // Save PDF to cacheDir/exports/[category]-folio.pdf
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val sanitizedSlug = category.slug.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val destinationFile = File(exportDir, "$sanitizedSlug-folio.pdf")

        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        FileOutputStream(destinationFile).use { output ->
            pdfDocument.writeTo(output)
            output.flush()
        }

        pdfDocument.close()
        destinationFile
    }

    private fun drawFooter(canvas: android.graphics.Canvas, pageNum: Int) {
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 8f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        canvas.drawText("Folio Reference", MARGIN_LEFT, MARGIN_BOTTOM + 20f, footerPaint)

        val pageText = "Page $pageNum"
        val pageTextWidth = footerPaint.measureText(pageText)
        canvas.drawText(pageText, MARGIN_RIGHT - pageTextWidth, MARGIN_BOTTOM + 20f, footerPaint)
    }

    private fun createStaticLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        lineSpacingMultiplier: Float
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, lineSpacingMultiplier)
                .setIncludePad(false)
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
