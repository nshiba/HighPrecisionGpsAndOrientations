package jp.ac.dendai.im.cps.highprecisiongpsandorientations

class KalmanLatLng(var Q_metres_per_second: Float) {

    private val minAccuracy = 1f

    private var variance: Float = -1f

    var TimeStamp_milliseconds: Long = 0

    var lat: Double = 0.toDouble()

    var lng: Double = 0.toDouble()

    var consecutiveRejectCount: Int = 0

    val accuracy: Float
        get() = Math.sqrt(variance.toDouble()).toFloat()

    fun setState(lat: Double, lng: Double, accuracy: Float, TimeStamp_milliseconds: Long) {
        this.lat = lat
        this.lng = lng
        this.variance = accuracy * accuracy
        this.TimeStamp_milliseconds = TimeStamp_milliseconds
    }

    fun process(lat_measurement: Double, lng_measurement: Double,
                accuracy: Float, TimeStamp_milliseconds: Long, Q_metres_per_second: Float) {
        var innerAccuracy = accuracy
        this.Q_metres_per_second = Q_metres_per_second

        if (innerAccuracy < minAccuracy)
            innerAccuracy = minAccuracy
        if (variance < 0) {
            // if variance < 0, object is unitialised, so initialise with
            // current values
            setState(lat_measurement, lng_measurement, innerAccuracy, TimeStamp_milliseconds)
        } else {
            // else apply Kalman filter methodology
            val TimeInc_milliseconds = TimeStamp_milliseconds - this.TimeStamp_milliseconds
            if (TimeInc_milliseconds > 0) {
                // time has moved on, so the uncertainty in the current position
                // increases
                variance += TimeInc_milliseconds.toFloat() * Q_metres_per_second * Q_metres_per_second / 1000
                this.TimeStamp_milliseconds = TimeStamp_milliseconds
                // TO DO: USE VELOCITY INFORMATION HERE TO GET A BETTER ESTIMATE
                // OF CURRENT POSITION
            }

            // Kalman gain matrix K = Covarariance * Inverse(Covariance +
            // MeasurementVariance)
            // NB: because K is dimensionless, it doesn't matter that variance
            // has different units to lat and lng
            val K = variance / (variance + innerAccuracy * innerAccuracy)
            // apply K
            lat += K * (lat_measurement - lat)
            lng += K * (lng_measurement - lng)
            // new Covarariance matrix is (IdentityMatrix - K) * Covarariance
            variance *= (1 - K)
        }
    }
}