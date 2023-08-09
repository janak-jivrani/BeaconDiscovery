package com.example.beacondiscovery.utils

import android.util.Log
import com.example.beacondiscovery.models.BeaconsCsvDataModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class CSVFile(var inputStream: InputStream) {

    fun readCsvFile(): List<BeaconsCsvDataModel> {
        val resultList = ArrayList<BeaconsCsvDataModel>()
        val reader = BufferedReader(
            InputStreamReader(
                inputStream
            )
        )

        try {
            var csvLine: String? = null
            while (reader.readLine().also { csvLine = it } != null) {
                val row = csvLine?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
                row?.let {
                    val beaconsCsvDataModel = BeaconsCsvDataModel(uuid = row[0], major = row[1], minor = row[2])
                    resultList.add(beaconsCsvDataModel)
                }
            }
        } catch (ex: IOException) {
            throw RuntimeException("Error in reading CSV file: $ex")
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                throw RuntimeException("Error while closing input stream: $e")
            }
        }
        return resultList
    }
}