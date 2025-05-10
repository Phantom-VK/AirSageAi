package com.vikram.airsageai.utils

import com.vikram.airsageai.data.dataclass.GasReading
import com.vikram.airsageai.data.dataclass.GasReadingLists
import com.vikram.airsageai.data.dataclass.MinMax
import android.content.Context
import android.os.Environment
import android.widget.Toast
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun Context.exportToExcel(data: List<GasReading>) {
    try {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Gas Readings")

        val headerRow = sheet.createRow(0)
        val headers = arrayOf(
            "Time", "CO (ppm)", "CO AQI", "Benzene (ppm)", "Benzene AQI",
            "NH3 (ppm)", "NH3 AQI", "Smoke (ppm)", "Smoke AQI", "LPG (ppm)", "LPG AQI",
            "CH4 (ppm)", "CH4 AQI", "H2 (ppm)", "H2 AQI", "Overall AQI", "AQI Category"
        )

        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        data.forEachIndexed { rowIndex, reading ->
            val row = sheet.createRow(rowIndex + 1)
            val aqiMap = reading.toAQI()

            row.createCell(0).setCellValue(reading.Time ?: "")
            row.createCell(1).setCellValue(reading.CO ?: 0.0)
            row.createCell(2).setCellValue(aqiMap["CO"]?.toDouble() ?: 0.0)
            row.createCell(3).setCellValue(reading.Benzene ?: 0.0)
            row.createCell(4).setCellValue(aqiMap["Benzene"]?.toDouble() ?: 0.0)
            row.createCell(5).setCellValue(reading.NH3 ?: 0.0)
            row.createCell(6).setCellValue(aqiMap["NH3"]?.toDouble() ?: 0.0)
            row.createCell(7).setCellValue(reading.Smoke ?: 0.0)
            row.createCell(8).setCellValue(aqiMap["Smoke"]?.toDouble() ?: 0.0)
            row.createCell(9).setCellValue(reading.LPG ?: 0.0)
            row.createCell(10).setCellValue(aqiMap["LPG"]?.toDouble() ?: 0.0)
            row.createCell(11).setCellValue(reading.CH4 ?: 0.0)
            row.createCell(12).setCellValue(aqiMap["Methane"]?.toDouble() ?: 0.0)
            row.createCell(13).setCellValue(reading.H2 ?: 0.0)
            row.createCell(14).setCellValue(aqiMap["Hydrogen"]?.toDouble() ?: 0.0)
            row.createCell(15).setCellValue(reading.overallAQI().toDouble())
            row.createCell(16).setCellValue(reading.getAQICategory(reading.overallAQI()))
        }



        // Optional: Manually set column width (in units of 1/256th of a character width)
        for (i in headers.indices) {
            sheet.setColumnWidth(i, 5000) // or adjust to your needs
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "GasReadings_$timeStamp.xlsx"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        FileOutputStream(file).use { fileOut ->
            workbook.write(fileOut)
        }

        // ✅ Optional: Add a success Toast
        Toast.makeText(this, "Excel exported to Downloads/$fileName", Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}


fun Context.exportToCSV(data: List<GasReading>) {
    try {
        val headers = listOf(
            "Time", "CO (ppm)", "CO AQI", "Benzene (ppm)", "Benzene AQI",
            "NH3 (ppm)", "NH3 AQI", "Smoke (ppm)", "Smoke AQI", "LPG (ppm)", "LPG AQI",
            "CH4 (ppm)", "CH4 AQI", "H2 (ppm)", "H2 AQI", "Overall AQI", "AQI Category"
        )

        val csvContent = buildString {
            appendLine(headers.joinToString(","))

            data.forEach { reading ->
                val aqi = reading.toAQI()
                val row = listOf(
                    reading.Time ?: "",
                    reading.CO ?: 0.0,
                    aqi["CO"] ?: 0,
                    reading.Benzene ?: 0.0,
                    aqi["Benzene"] ?: 0,
                    reading.NH3 ?: 0.0,
                    aqi["NH3"] ?: 0,
                    reading.Smoke ?: 0.0,
                    aqi["Smoke"] ?: 0,
                    reading.LPG ?: 0.0,
                    aqi["LPG"] ?: 0,
                    reading.CH4 ?: 0.0,
                    aqi["Methane"] ?: 0,
                    reading.H2 ?: 0.0,
                    aqi["Hydrogen"] ?: 0,
                    reading.overallAQI(),
                    reading.getAQICategory(reading.overallAQI())
                )
                appendLine(row.joinToString(","))
            }
        }

        val fileName = "GasReadings_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        FileOutputStream(file).use { it.write(csvContent.toByteArray()) }
        Toast.makeText(this, "Excel exported to Downloads/$fileName", Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        e.printStackTrace()

    }
}

class AnalyticsUtils {

    // Optimized function to extract gas readings
    fun extractGasReadings(readings: List<GasReading>): GasReadingLists {
        return readings.fold(GasReadingLists()) { acc, reading ->
            acc.apply {
                aqiReadings.add(reading.overallAQI().toDouble())
                coReadings.add(reading.CO?.toDouble() ?: 0.0)
                benzeneReadings.add(reading.Benzene?.toDouble() ?: 0.0)
                nh3Readings.add(reading.NH3?.toDouble() ?: 0.0)
                smokeReadings.add(reading.Smoke?.toDouble() ?: 0.0)
                lpgReadings.add(reading.LPG?.toDouble() ?: 0.0)
                ch4Readings.add(reading.CH4?.toDouble() ?: 0.0)
                h2Readings.add(reading.H2?.toDouble() ?: 0.0)
            }
        }
    }


    fun calculateMinMax(readings: List<GasReading>): MinMax {
        var min = Double.MAX_VALUE
        var max = Double.MIN_VALUE

        for (reading in readings) {
            val values = listOfNotNull(
                reading.CO,
                reading.Benzene,
                reading.NH3,
                reading.Smoke,
                reading.LPG,
                reading.CH4,
                reading.H2
            ).map { it.toDouble() }

            min = minOf(min, values.minOrNull() ?: Double.MAX_VALUE)
            max = maxOf(max, values.maxOrNull() ?: Double.MIN_VALUE)
        }

        return MinMax(min, max)
    }

}

