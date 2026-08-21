package com.example.medreminder.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug
import java.io.File
import java.io.FileOutputStream

/**
 * 用药方案 PDF 导出（Android 原生 PdfDocument，零第三方依赖）。
 * 输出到应用缓存目录，通过 FileProvider 分享给家属。
 */
object PdfExporter {

    fun export(context: Context, drugs: List<Drug>, schedules: List<DoseSchedule>): Uri? {
        val pageWidth = 595  // A4 宽（px @ 72dpi）
        val pageHeight = 842 // A4 高
        val margin = 48
        val contentWidth = pageWidth - margin * 2
        val lineHeight = 20

        val doc = PdfDocument()
        var page: PdfDocument.Page? = null
        var canvas: android.graphics.Canvas? = null
        var y = 0

        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 24f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            isAntiAlias = true
            textSize = 16f
            isFakeBoldText = true
            color = Color.DKGRAY
        }
        val bodyPaint = Paint().apply {
            isAntiAlias = true
            textSize = 14f
            color = Color.BLACK
        }
        val subPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
            color = Color.GRAY
        }

        fun newPage() {
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
            canvas = page?.canvas
            y = margin
            canvas?.drawColor(Color.WHITE)
        }

        fun drawLine(text: String, paint: Paint = bodyPaint, indent: Int = 0) {
            if (y > pageHeight - margin - lineHeight) {
                doc.finishPage(page)
                newPage()
            }
            canvas?.drawText(text, (margin + indent).toFloat(), y.toFloat(), paint)
            y += lineHeight
        }

        newPage()
        drawLine("用药方案清单", titlePaint)
        drawLine("生成时间：${DateUtils.today()}", subPaint)
        drawLine("", subPaint)

        // 按用药人分组
        val grouped = drugs.groupBy { it.owner }
        grouped.forEach { (owner, ownerDrugs) ->
            drawLine("用药人：$owner", headerPaint)
            ownerDrugs.forEach { drug ->
                val times = schedules.filter { it.drugId == drug.id }
                drawLine("● ${drug.name}（${drug.dosage}）", bodyPaint, 12)
                if (drug.remark.isNotBlank()) {
                    drawLine("  备注：${drug.remark}", subPaint, 24)
                }
                times.forEach { s ->
                    val days = if (s.repeatDays == "daily") "每天"
                    else s.repeatDays.split(",").joinToString("、") { "周${weekName(it.toInt())}" }
                    val relation = if (s.relation.isNotBlank() && s.relation != "无") "（${s.relation}）" else ""
                    drawLine("  ${s.time} $days $relation", subPaint, 24)
                }
            }
            drawLine("", subPaint)
        }

        doc.finishPage(page)

        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "medication_regimen.pdf")
        return try {
            FileOutputStream(file).use { doc.writeTo(it) }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            null
        } finally {
            doc.close()
        }
    }

    private fun weekName(d: Int): String = when (d) {
        1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"; 5 -> "五"; 6 -> "六"; else -> "日"
    }
}
