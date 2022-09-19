/*
 * Copyright 2018-2019 NotAnotherOne Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.atmotube

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.atmotube.ble.AtmotubeUtils
import com.atmotube.ble.UpdateDataHolder
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION_REQ_CODE = 34
    private val ENABLE_BT_REQ = 0
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mScanning = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MyAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    inner class MyCallback : ScanCallback() {

        override fun onScanFailed(errorCode: Int) {

        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val resultHolder = AtmotubeUtils.getDataFromScanResult(result)
            if (resultHolder != null) {
                // atmotube found
                println("@@@ " + result.device.address + " -> " + resultHolder)
                viewAdapter.addItem(resultHolder)
            }
        }

    }

    private val mCallback: MyCallback = MyCallback()

    class MyAdapter : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        private val data = ArrayList<UpdateDataHolder>()
        private val mSb = StringBuilder()

        @Synchronized
        fun addItem(item: UpdateDataHolder) {
            val key = item.mac
            for ((pos, i) in data.withIndex()) {
                if (i.mac == key) {
                    data[pos] = item
                    notifyItemChanged(pos)
                    return
                }
            }
            data.add(item)
            notifyItemInserted(data.size - 1)
        }

        class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var line1: TextView = view.findViewById(R.id.line1)
            var line2: TextView = view.findViewById(R.id.line2)
            var line3: TextView = view.findViewById(R.id.line3)
            var line4: TextView = view.findViewById(R.id.line4)
            var text: TextView = view.findViewById(R.id.text)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_view, parent, false)
            return MyViewHolder(view)
        }

        @Synchronized
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val d = data[position]
            mSb.setLength(0)
            holder.line1.text = getLine1(mSb, d)
            mSb.setLength(0)
            holder.line2.text = getLine2(mSb, d)
            mSb.setLength(0)
            holder.line3.text = getLine3(mSb, d)
            if (d.isHw4) {
                mSb.setLength(0)
                holder.line4.visibility = View.VISIBLE
                holder.line4.text = getLine4(mSb, d)
            } else {
                holder.line4.visibility = View.GONE
            }
            if (d.isActivated || d.isHw3 || d.isHw4) {
                holder.text.text = String.format(Locale.US, "%.2f", d.voc)
            } else {
                holder.text.text = d.adc.toString()
            }
            if (d.isHw3 || d.isHw4) {
                holder.line1.setCompoundDrawablesWithIntrinsicBounds(if (d.isActivated)
                    0
                else
                    R.drawable.factory, 0,
                        if (d.isBonded) R.drawable.paired else 0, 0)
            } else {
                holder.line1.setCompoundDrawablesWithIntrinsicBounds(if (d.isActivated)
                    0
                else
                    R.drawable.factory, 0, 0, 0)
            }
            holder.line2.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    if (d.isCharging && d.isCalibrating)
                        R.drawable.chr_heat
                    else if (d.isCharging)
                        R.drawable.chr
                    else if (d.isCalibrating)
                        R.drawable.heat
                    else if (d.isChargingTimeout) R.drawable.timer else 0, 0)
        }

        @Synchronized
        override fun getItemCount() = data.size

        private fun getLine1(mSb: StringBuilder, data: UpdateDataHolder): StringBuilder {
            mSb.append(data.mac).append(" (")
            mSb.append(AtmotubeUtils.getVersionName(data.hwVer))
            mSb.append(")")
            if (data.errorCode > 0) {
                mSb.append(", ERR").append(data.errorCode)
            }
            if ((data.isHw3 || data.isHw4) && !data.isActivated) {
                mSb.append(", ").append(data.batteryVoltageFloat).append("V")
            }
            return mSb
        }

        private fun getLine2(mSb: StringBuilder, data: UpdateDataHolder): StringBuilder {
            mSb.append("FW ")
            if (data.fwVer != null && data.fwVer!!.length == 6) {
                mSb.append(data.fwVer!!.substring(0, 2)).append('.').append(data.fwVer!!.substring(2, 4)).append('.').append(data.fwVer!!.substring(4))
            } else {
                mSb.append(data.fwVer)
            }
            if (data.isHw3 || data.isHw4) {
                mSb.append(", bat ").append(data.battery).append("%")
            } else {
                mSb.append(", bat").append(data.battery)
            }
            if (data.isActivated && data.batteryVoltageFloat > 0) {
                mSb.append(", ").append(data.batteryVoltageFloat).append("V")
            }
            if (data.deviceCRC > 0) {
                mSb.append(", ").append(Integer.toHexString(data.deviceCRC))
            }
            return mSb
        }

        private fun getLine3(mSb: StringBuilder, data: UpdateDataHolder): StringBuilder {
            if (data.temperature > 0) {
                mSb.append('+')
            }
            mSb.append(data.temperature.toInt()).append("°C, ").append(data.humidity.toInt()).append("%")
            if ((data.isHw3 || data.isHw4) && data.pressure > 0) {
                mSb.append(", ").append(data.pressure).append(" mbar")
            }
            mSb.append(", ").append(System.currentTimeMillis() / 1000 - data.time).append("s ago")
            return mSb
        }

        private fun getLine4(mSb: StringBuilder, data: UpdateDataHolder): StringBuilder {
            if (data.isHw4) {
                if (!data.isPmIsOn) {
                    mSb.append("PM: off")
                } else if (!data.isActivated) {
                    mSb.append(String.format(Locale.US, "PM2.5: %d", data.pm25.toInt()))
                } else {
                    mSb.append(String.format(Locale.US, "PM1: %d, PM2.5: %d, PM10: %d", data.pm1.toInt(), data.pm25.toInt(), data.pm10.toInt()))
                }
            }
            return mSb
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager;
        mBluetoothAdapter = manager.adapter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                return
            }
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_REQ_CODE)
            return
        }
        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter()

        recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            itemAnimator = DefaultItemAnimator()
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }


    private fun startScan() {
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, ENABLE_BT_REQ)
            return
        }
        if (mScanning) {
            return
        }
        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner.startScan(null, settings, mCallback)
        mScanning = true
    }

    override fun onPause() {
        super.onPause()
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanner.stopScan(mCallback)
        mScanning = false
    }

    override fun onResume() {
        super.onResume()
        startScan()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Bluetooth permission granted
                }
            }
        }
    }
}
