package jp.ac.dendai.im.cps.highprecisiongpsandorientations

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.*
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import jp.ac.dendai.im.cps.citywalkersmeter.LocationData
import android.support.v4.content.LocalBroadcastManager
import android.os.SystemClock

class LocationService : Service(), LocationListener {

    private val binder = LocationServiceBinder()

    private val locationManager: LocationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    val locationList = mutableListOf<Location>()

    val runStartTimeInMillis: Long = SystemClock.elapsedRealtimeNanos() / 1000000

    var kalmanFilter: KalmanLatLng = KalmanLatLng(3f)

    var currentSpeed: Float = 0.0f

    override fun onCreate() {
        super.onCreate()
        startUpdatingLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
    }

    fun startUpdatingLocation() {
        val criteria = createCriteria()
        val gpsFreqInMillis: Long = 1000
        val gpsFreqInDistance: Float = 1F
        locationManager.requestLocationUpdates(gpsFreqInMillis, gpsFreqInDistance, criteria, this, null)
    }

    fun createCriteria(): Criteria {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_HIGH
        criteria.isAltitudeRequired = false
        criteria.isSpeedRequired = false
        criteria.isCostAllowed = true
        criteria.isBearingRequired = true
        criteria.horizontalAccuracy = Criteria.ACCURACY_FINE
        criteria.verticalAccuracy = Criteria.ACCURACY_FINE
        return criteria
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onLocationChanged(location: Location?) {
        val locationData = LocationData.entityToModel(location)
        Log.d(TAG, locationData.toString())

        if (location != null) {
            locationList.add(location)
//            filterAndAddLocation(location)
        }

        val intent = Intent(INTENT_LOCATION_UPDATED)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun filterAndAddLocation(location: Location): Boolean {
        val age = getLocationAge(location)

        if (age > 10 * 1000) { //more than 10 seconds
            Log.d(TAG, "Location is old")
//            oldLocationList.add(location)
            return false
        }

        if (location.accuracy <= 0) {
            Log.d(TAG, "Latitidue and longitude values are invalid.")
//            noAccuracyLocationList.add(location)
            return false
        }

        //setAccuracy(newLocation.getAccuracy());
        val horizontalAccuracy = location.accuracy
        if (horizontalAccuracy > 100) {
            Log.d(TAG, "Accuracy is too low.")
//            inaccurateLocationList.add(location)
            return false
        }


        /* Kalman Filter */
        val predictedLocation = filterKalman(location) ?: return false

        /* Notifiy predicted location to UI */
        val intent = Intent(INTENT_PREDICT_LOCATION)
        intent.putExtra(EXTRA_LOCATION, predictedLocation)
        LocalBroadcastManager.getInstance(this.application).sendBroadcast(intent)

        Log.d(TAG, "Location quality is good enough.")
        currentSpeed = location.speed
//        locationList.add(location)


        return true
    }

    private fun filterKalman(location: Location): Location? {
        val Qvalue: Float

        val elapsedTimeInMillis = (location.elapsedRealtimeNanos / 1000000) - runStartTimeInMillis

        if (currentSpeed == 0.0f) {
            Qvalue = 3.0f //3 meters per second
        } else {
            Qvalue = currentSpeed // meters per second
        }

        kalmanFilter.process(location.latitude, location.longitude, location.accuracy, elapsedTimeInMillis, Qvalue)
        val predictedLat = kalmanFilter.lat
        val predictedLng = kalmanFilter.lng

        val predictedLocation = Location("")//provider name is unecessary
        predictedLocation.latitude = predictedLat//your coords of course
        predictedLocation.longitude = predictedLng
        val predictedDeltaInMeters = predictedLocation.distanceTo(location)

        if (predictedDeltaInMeters > 60) {
            Log.d(TAG, "Kalman Filter detects mal GPS, we should probably remove this from track")
            kalmanFilter.consecutiveRejectCount = 1 + kalmanFilter.consecutiveRejectCount

            if (kalmanFilter.consecutiveRejectCount > 3) {
                kalmanFilter = KalmanLatLng(3f)
            }

//            kalmanNGLocationList.add(location)
            return null
        } else {
            kalmanFilter.consecutiveRejectCount = 0
        }

        return predictedLocation
    }

    private fun getLocationAge(newLocation: Location): Long {
        val currentTimeInMilli = (SystemClock.elapsedRealtimeNanos() / 1000000)
        val locationAge = currentTimeInMilli - newLocation.elapsedRealtimeNanos / 1000000
        return locationAge
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //        return super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY
    }

    override fun onRebind(intent: Intent) {
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // do nothing
    }

    override fun onProviderEnabled(provider: String?) {
        // do nothing
    }

    override fun onProviderDisabled(provider: String?) {
        // do nothing
    }

    inner class LocationServiceBinder : Binder() {

        internal val service: LocationService
            get() = this@LocationService
    }

    companion object {

        private val TAG = LocationService::class.java.simpleName

        val INTENT_PREDICT_LOCATION = "PredictLocation"

        val INTENT_LOCATION_UPDATED = "LocationUpdated"

        val EXTRA_LOCATION = "location"
    }
}
