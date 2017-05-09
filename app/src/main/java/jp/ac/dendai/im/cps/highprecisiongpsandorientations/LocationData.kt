package jp.ac.dendai.im.cps.citywalkersmeter

import android.location.Location

data class LocationData(
        var latitude: Double,
        var longitude: Double,
        var accuracy: Float,
        var time: Long,
        var bearing: Float) {

    companion object {
        fun entityToModel(location: Location?): LocationData? {
            if (location == null) {
                return null
            }

            return LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    time = location.time,
                    bearing = location.bearing)
        }
    }

    fun JSONFormat(): String {
        return "{" +
                "\"lat\": ${this.latitude}, " +
                "\"lng\": ${this.longitude}, " +
                "\"accuracy\": ${this.accuracy}, " +
                "\"timestamp\": ${this.time}," +
                "\"bearing\": ${this.bearing} " +
                "}"
    }
}
