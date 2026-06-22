package com.fdtracker.feature.ocr.util

import com.google.mlkit.vision.text.Text

object TextBlockParser {
    fun extractFullText(visionText: Text): String {
        val sb = StringBuilder()
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                sb.appendLine(line.text)
            }
        }
        return sb.toString()
    }
}
