package com.example.beacondiscovery

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.beacondiscovery.permissions.BeaconScanPermissionsActivity
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.MonitorNotifier

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    lateinit var beaconListView: ListView
    lateinit var beaconCountTextView: TextView
    lateinit var monitoringButton: Button
    lateinit var rangingButton: Button
    lateinit var beaconDiscoveryApplication: BeaconDiscoveryApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        beaconDiscoveryApplication = application as BeaconDiscoveryApplication

        val regionViewModel = BeaconManager.getInstanceForApplication(this)
            .getRegionViewModel(beaconDiscoveryApplication.region)
        regionViewModel.regionState.observe(this, monitoringObserver)
        regionViewModel.rangedBeacons.observe(this, rangingObserver)
        rangingButton = findViewById(R.id.rangingButton)
        monitoringButton = findViewById(R.id.monitoringButton)
        beaconListView = findViewById(R.id.beaconList)
        beaconCountTextView = findViewById(R.id.beaconCount)
        beaconCountTextView.text = getString(R.string.no_beacons_detected)
        beaconListView.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
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
            beaconListView.adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
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
            beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
                beacons
                    .sortedBy { it.distance }
                    .map {
                        "UUID: ${it.id1}\nMajor: ${it.id2}\nMinor: ${it.id3}"
                    }.toTypedArray()
            )
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
            beaconListView.adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
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

}
