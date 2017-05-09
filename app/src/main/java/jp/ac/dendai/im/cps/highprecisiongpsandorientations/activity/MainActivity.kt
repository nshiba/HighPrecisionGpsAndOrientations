package jp.ac.dendai.im.cps.highprecisiongpsandorientations.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import jp.ac.dendai.im.cps.highprecisiongpsandorientations.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (findViewById(R.id.button_logging) as Button).setOnClickListener {
            startActivity(Intent(this, LoggingActivity::class.java))
        }
        (findViewById(R.id.button_map) as Button).setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }
    }
}
