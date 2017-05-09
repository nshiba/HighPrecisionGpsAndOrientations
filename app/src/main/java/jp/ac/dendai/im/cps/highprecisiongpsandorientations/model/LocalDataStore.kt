package jp.ac.dendai.im.cps.highprecisiongpsandorientations.model

import jp.ac.dendai.im.cps.citywalkersmeter.LocationData
import jp.ac.dendai.im.cps.highprecisiongpsandorientations.util.DateUtil
import java.io.*

class LocalDataStore : DataStore {

    private val PATH_DIR = "/sdcard/Android/data/gpsandorientations/"

    private var filename: String? = null

    override fun init() {
    }

    override fun save(data: LocationData) {
        write(createCsvFormat(data))
    }

    override fun save(data: Array<LocationData>) {
        data.forEach { save(it) }
    }

    override fun finish() {
        // do nothing
    }

    fun write(writeText: String) {
        val file = File(PATH_DIR)
        if (!file.isDirectory) {
            file.absoluteFile.mkdir()
        }

        val dataFile = File(PATH_DIR + filename!!)
        val outputStream: OutputStream

        try {
            outputStream = FileOutputStream(dataFile, true)
            val writer = PrintWriter(OutputStreamWriter(outputStream, "UTF-8"))
            writer.append(writeText)
            writer.append("\n")
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * latitude longitude accuracy time speed altitude bearing
     * @return csv format
     */
    private fun createCsvFormat(data: LocationData): String {
        return System.currentTimeMillis().toString() + "," +
                DateUtil.parseDate(System.currentTimeMillis()) + "," +
                data.latitude + "," +
                data.longitude + "," +
                data.accuracy + "," +
                data.time + "," +
                data.bearing
    }
}