package jp.ac.dendai.im.cps.highprecisiongpsandorientations.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import jp.ac.dendai.im.cps.highprecisiongpsandorientations.LocationService
import jp.ac.dendai.im.cps.highprecisiongpsandorientations.R

class LoggingActivity : android.support.v7.app.AppCompatActivity() {


    private val TAG_LOCATION_SERVICE = "LocationService"

    private var locationService: LocationService? = null

    private val serviceConnection = object : ServiceConnection {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logging)

        setupLocationService()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    private fun setupLocationService() {
        val intent = Intent(this, LocationService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
}
