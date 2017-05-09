package jp.ac.dendai.im.cps.highprecisiongpsandorientations.activity

import android.content.*
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.FragmentActivity
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import jp.ac.dendai.im.cps.highprecisiongpsandorientations.LocationService
import jp.ac.dendai.im.cps.highprecisiongpsandorientations.R

class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private val TAG = this::class.java.simpleName

    private lateinit var googleMap: GoogleMap

    private val TAG_LOCATION_SERVICE = "LocationService"

    private var locationService: LocationService? = null

    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            if (name?.className?.equals(TAG_LOCATION_SERVICE)?: false) {
                locationService = null
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (name?.className?.endsWith(TAG_LOCATION_SERVICE)?: false) {
                val binder = service as LocationService.LocationServiceBinder
                locationService = binder.service
            }
        }
    }

    private lateinit var locationUpdateReceiver: BroadcastReceiver

    private var runningPathPolyline: Polyline? = null

    private var polylineWidth: Float = 30F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        setupLocationService()
        setupLocationUpdateReceiver()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(jp.ac.dendai.im.cps.highprecisiongpsandorientations.R.id.map) as com.google.android.gms.maps.SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        unregisterReceiver(locationUpdateReceiver)
    }

    override fun onMapReady(googleMap: com.google.android.gms.maps.GoogleMap) {
        this.googleMap = googleMap
        setupMap()
    }

    private fun setupMap() {
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.isMyLocationEnabled = true
    }

    private fun setupLocationService() {
        val intent = android.content.Intent(this, LocationService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, android.content.Context.BIND_AUTO_CREATE)
    }

    private fun setupLocationUpdateReceiver() {
        locationUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                addPolyLine()
//                val location = intent?.getParcelableExtra<Location>(LocationService.EXTRA_LOCATION)
//                zoomMapTo(location)
            }
        }
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(locationUpdateReceiver, IntentFilter(LocationService.INTENT_LOCATION_UPDATED))
    }

    private fun zoomMapTo(location: Location?) {
        location ?: return

        val latlng = LatLng(location.latitude, location.longitude)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17.5F))
    }

    private fun addPolyLine() {
        Log.d(TAG, "addPolyLine")
        val locationList = locationService?.locationList ?: return

        if (locationList.size == 2) {
            Log.d(TAG, "addPolyLine: size = 2")
            val options = createRunningPolyLineOptions(locationList)
            runningPathPolyline = googleMap.addPolyline(options)
        } else if (locationList.size > 2) {
            Log.d(TAG, "addPolyLine: size > 2")
            val toLocation = locationList[locationList.size - 1]
            val to = LatLng(toLocation.latitude, toLocation.longitude)

            val points = runningPathPolyline?.points
            points?.add(to)

            runningPathPolyline?.points = points
        }
    }

    private fun createRunningPolyLineOptions(locationList: MutableList<Location>): PolylineOptions {
        val fromLocation = locationList[0]
        val toLocation = locationList[1]

        val from = LatLng(fromLocation.latitude, fromLocation.longitude)
        val to = LatLng(toLocation.latitude, toLocation.longitude)

        return PolylineOptions()
                .add(from, to)
                .width(polylineWidth)
                .color(Color.parseColor("#801b60fe"))
                .geodesic(true)
    }
}
