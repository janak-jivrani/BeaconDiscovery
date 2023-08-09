package com.example.beacondiscovery.ui.home

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.beacondiscovery.BeaconDiscoveryApplication
import com.example.beacondiscovery.R
import com.example.beacondiscovery.adapter.BeaconsListAdapter
import com.example.beacondiscovery.models.BeaconsCsvDataModel
import com.example.beacondiscovery.permissions.BeaconScanPermissionsActivity
import com.example.beacondiscovery.ui.setting.SettingActivity
import com.example.beacondiscovery.utils.CSVFile
import com.example.beacondiscovery.utils.Constant
import com.example.beacondiscovery.utils.PreferenceUtil
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.MonitorNotifier
import java.util.Timer
import kotlin.concurrent.timerTask

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    lateinit var mViewModel: HomeViewModel

    lateinit var beaconListView: RecyclerView
    lateinit var beaconCountTextView: TextView
    lateinit var monitoringButton: Button
    lateinit var rangingButton: Button
    lateinit var beaconDiscoveryApplication: BeaconDiscoveryApplication

    private var isScanFromAll = true

    val qList = arrayListOf<BeaconsCsvDataModel>()
    val listOfBeacons = arrayListOf<BeaconsCsvDataModel>()
    private val csvBeaconList = arrayListOf<BeaconsCsvDataModel>()

    private val handler = Handler(Looper.getMainLooper())
    private var intervalMillis: Long = 1000

    private lateinit var locationManager: LocationManager
    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        beaconDiscoveryApplication = application as BeaconDiscoveryApplication
        mViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        readCsvAndList()

        setObserver()

        val regionViewModel = BeaconManager.getInstanceForApplication(this)
            .getRegionViewModel(beaconDiscoveryApplication.region)
        regionViewModel.regionState.observe(this, monitoringObserver)
        regionViewModel.rangedBeacons.observe(this, rangingObserver)
        rangingButton = findViewById(R.id.rangingButton)
        monitoringButton = findViewById(R.id.monitoringButton)
        beaconListView = findViewById(R.id.beaconList)
        beaconCountTextView = findViewById(R.id.beaconCount)
        beaconCountTextView.text = getString(R.string.no_beacons_detected)
        beaconListView.adapter = BeaconsListAdapter(this, listOfBeacons)

        startLocationUpdates()

        checkBaseUrlAndStartApiCalling()

    }

    override fun onResume() {
        super.onResume()

        if (!BeaconScanPermissionsActivity.allPermissionsGranted(
                this,
                true
            )
        ) {
            val intent = Intent(this, BeaconScanPermissionsActivity::class.java)
            intent.putExtra("backgroundAccessRequested", true)
            startActivity(intent)
        }
    }

    private val monitoringObserver = Observer<Int> { state ->
        if (state == MonitorNotifier.OUTSIDE) {
            beaconCountTextView.text = getString(R.string.outside_of_region_no_beacons_detected)
            beaconListView.adapter = BeaconsListAdapter(this, arrayListOf())
        } else {
            beaconCountTextView.text = getString(R.string.inside_beacon_region)
        }
    }

    private val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        if (BeaconManager.getInstanceForApplication(this).rangedRegions.isNotEmpty()) {
            beaconCountTextView.text = getString(
                R.string.randing_enabled_count_beacon_deletected,
                beacons.count().toString()
            )

            listOfBeacons.clear()
            beacons.sortedBy { it.distance }.map {
                Log.e(TAG, ": " + it.id1)
                listOfBeacons.add(
                    BeaconsCsvDataModel(
                        uuid = it.id1.toString(),
                        major = it.id2.toString(),
                        minor = it.id3.toString()
                    )
                )
            }

            if (isScanFromAll) {
                beaconListView.adapter = BeaconsListAdapter(this, listOfBeacons)
            } else {
                val list =
                    listOfBeacons.filter { beacons -> csvBeaconList.any { it.uuid == beacons.uuid && it.major == beacons.major && it.minor == beacons.minor } }
                listOfBeacons.clear()
                listOfBeacons.addAll(list)
                beaconListView.adapter = BeaconsListAdapter(this, listOfBeacons)
            }

            addBeaconIntoQ()

        }
    }

    private fun addBeaconIntoQ() {
        if (listOfBeacons.isNotEmpty()) {
            listOfBeacons.map { beacon ->
                if (!qList.any { it.uuid == beacon.uuid }) {
                    qList.add(beacon)
                }
            }
        }
    }

    fun rangingButtonTapped(view: View) {
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        if (beaconManager.rangedRegions.isEmpty()) {
            beaconManager.startRangingBeacons(beaconDiscoveryApplication.region)
            rangingButton.text = getString(R.string.stop_ranging)
            beaconCountTextView.text = getString(R.string.ranging_enabled)
        } else {
            beaconManager.stopRangingBeacons(beaconDiscoveryApplication.region)
            rangingButton.text = getString(R.string.start_ranging)
            beaconCountTextView.text = getString(R.string.ranging_disabled)
            beaconListView.adapter = BeaconsListAdapter(this, arrayListOf())
        }
    }

    fun monitoringButtonTapped(view: View) {
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        if (beaconManager.monitoredRegions.isEmpty()) {
            beaconManager.startMonitoring(beaconDiscoveryApplication.region)

            monitoringButton.text = getString(R.string.stop_monitoring)
        } else {
            beaconManager.stopMonitoring(beaconDiscoveryApplication.region)
            monitoringButton.text = getString(R.string.start_monitoring)
        }
    }

    fun scanForAll(view: View) {
        isScanFromAll = true
    }

    fun scanFromCsv(view: View) {
        isScanFromAll = false
    }

    fun openSetting(view: View) {
        startForResult.launch(Intent(this, SettingActivity::class.java))
    }

    private fun readCsvAndList() {

        val inputStream = resources.openRawResource(R.raw.sample)
        val csvFile = CSVFile(inputStream)
        val scoreList = csvFile.readCsvFile()

        csvBeaconList.clear()
        csvBeaconList.addAll(scoreList)

    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                checkBaseUrlAndStartApiCalling()
            }
        }

    private fun setObserver() {

        mViewModel.postsLiveData.observe(this) {
            Log.e(TAG, "setObserver: Submited")
        }

        mViewModel.errorLiveData.observe(this) {
            Log.e(TAG, "setObserver: Submit Failed" + it)
        }

    }

    private fun checkBaseUrlAndStartApiCalling() {
        if (PreferenceUtil.getStringSharedPref(this, Constant.URL, "").toString().isNotEmpty()) {
            intervalMillis =
                (PreferenceUtil.getStringSharedPref(this, Constant.MINUTES, "10")?.toInt()
                    ?: 0) * 1000L
            scheduleApiCall()
        } else {
            handler.removeCallbacksAndMessages(null)
        }
    }

    private fun scheduleApiCall() {

        handler.removeCallbacksAndMessages(null)

        handler.postDelayed(
            object : Runnable {
                override fun run() {
                    callApiForSubmitBeacons()
                    handler.postDelayed(
                        this,
                        intervalMillis
                    )
                }
            }, intervalMillis
        )

    }

    private fun callApiForSubmitBeacons() {
        Log.e(TAG, "callApiForSubmitBeacons: Api Call")

        if (qList.isNotEmpty()) {
            mViewModel.sendBeacons(
                PreferenceUtil.getStringSharedPref(this, Constant.URL, "").toString(),
                qList.first().apply {
                    lat = latitude.toString()
                    long = longitude.toString()
                }
            )
            qList.removeFirst()
        }
    }

    private fun startLocationUpdates() {

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            showEnableLocationDialog()
        }

        val locationListener = LocationListener { location ->
            latitude = location.latitude
            longitude = location.longitude
        }

        // Request location updates from both GPS and network providers
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0L,
            0f,
            locationListener
        )
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0L,
            0f,
            locationListener
        )
    }

    private fun showEnableLocationDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enable GPS")
        builder.setMessage("GPS is not enabled. Please enable GPS to use location services.")
        builder.setPositiveButton("Enable GPS") { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

    }

}
