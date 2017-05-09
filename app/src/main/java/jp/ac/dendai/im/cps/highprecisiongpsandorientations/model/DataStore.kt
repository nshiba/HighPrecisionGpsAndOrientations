package jp.ac.dendai.im.cps.highprecisiongpsandorientations.model

import jp.ac.dendai.im.cps.citywalkersmeter.LocationData

interface DataStore {

    fun init()

    fun save(data: LocationData)

    fun save(data: Array<LocationData>)

    fun finish()
}