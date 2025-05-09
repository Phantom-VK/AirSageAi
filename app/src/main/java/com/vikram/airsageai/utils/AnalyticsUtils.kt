package com.vikram.airsageai.utils

import com.vikram.airsageai.data.dataclass.GasReading
import com.vikram.airsageai.data.dataclass.GasReadingLists
import com.vikram.airsageai.data.dataclass.MinMax
import android.content.Context
import android.os.Environment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun Context.exportToExcel(data: List<GasReading>) {
    try {
        // Create a new workbook
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Gas Readings")

        // Create header row
        val headerRow = sheet.createRow(0)
        val headers = arrayOf(
            "Time", "CO (ppm)", "CO AQI", "Benzene (ppm)", "Benzene AQI",
            "NH3 (ppm)", "NH3 AQI", "Smoke (ppm)", "Smoke AQI", "LPG (ppm)", "LPG AQI",
            "CH4 (ppm)", "CH4 AQI", "H2 (ppm)", "H2 AQI", "Overall AQI", "AQI Category"
        )

        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        // Fill data rows
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

        // Auto-size columns
        for (i in 0 until headers.size) {
            sheet.autoSizeColumn(i)
        }

        // Create file name with timestamp
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "GasReadings_$timeStamp.xlsx"

        // Get downloads directory
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        // Write to file
        val fileOut = FileOutputStream(file)
        workbook.write(fileOut)
        fileOut.close()
//        workbook.close()

        // Show success message or notification
        // You might want to add a Toast or notification here
    } catch (e: Exception) {
        e.printStackTrace()
        // Handle error (show Toast, log, etc.)
    }
}

fun Context.exportToCSV(data: List<GasReading>) {
    try {
        // Create CSV header
        val csvHeader = "Time,CO (ppm),CO AQI,Benzene (ppm),Benzene AQI," +
                "NH3 (ppm),NH3 AQI,Smoke (ppm),Smoke AQI,LPG (ppm),LPG AQI," +
                "CH4 (ppm),CH4 AQI,H2 (ppm),H2 AQI,Overall AQI,AQI Category\n"

        // Build CSV content
        val csvContent = StringBuilder()
        csvContent.append(csvHeader)

        data.forEach { reading ->
            val aqiMap = reading.toAQI()
            csvContent.append("${reading.Time ?: ""},")
            csvContent.append("${reading.CO ?: 0.0},")
            csvContent.append("${aqiMap["CO"] ?: 0},")
            csvContent.append("${reading.Benzene ?: 0.0},")
            csvContent.append("${aqiMap["Benzene"] ?: 0},")
            csvContent.append("${reading.NH3 ?: 0.0},")
            csvContent.append("${aqiMap["NH3"] ?: 0},")
            csvContent.append("${reading.Smoke ?: 0.0},")
            csvContent.append("${aqiMap["Smoke"] ?: 0},")
            csvContent.append("${reading.LPG ?: 0.0},")
            csvContent.append("${aqiMap["LPG"] ?: 0},")
            csvContent.append("${reading.CH4 ?: 0.0},")
            csvContent.append("${aqiMap["Methane"] ?: 0},")
            csvContent.append("${reading.H2 ?: 0.0},")
            csvContent.append("${aqiMap["Hydrogen"] ?: 0},")
            csvContent.append("${reading.overallAQI()},")
            csvContent.append("${reading.getAQICategory(reading.overallAQI())}\n")
        }

        // Create file name with timestamp
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "GasReadings_$timeStamp.csv"

        // Get downloads directory
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        // Write to file
        FileOutputStream(file).use { fos ->
            fos.write(csvContent.toString().toByteArray())
        }

        // Show success message or notification
        // You might want to add a Toast or notification here
    } catch (e: Exception) {
        e.printStackTrace()
        // Handle error (show Toast, log, etc.)
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

