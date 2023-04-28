package com.example.beebration_v2

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    val CITY: String = "bacnotan, la union"
    val API: String = "06c921750b9a82d8f5d1294e1586276f" // Use API key

    private lateinit var database: DatabaseReference
    private lateinit var textViewVoltage: TextView
    private lateinit var textViewBatteryPercentage: TextView
    private lateinit var textViewLightBulbStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherTask().execute()


        FirebaseApp.initializeApp(this)

        // Get a reference to the Firebase Realtime Database
        database = FirebaseDatabase.getInstance("https://beebration-v2-52386-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        // Initialize UI elements
        textViewVoltage = findViewById(R.id.textView_voltage)
        textViewBatteryPercentage = findViewById(R.id.textView_batteryPercentage)
        textViewLightBulbStatus = findViewById(R.id.textView_lightBulbStatus)


        // Fetch data and listen for changes
        fetchData()
    }



    private fun fetchData() {
        // Get a reference to the "data" child node in the Realtime Database
        val dataRef = database.child("data")

        // Attach a ValueEventListener to the "data" child node to listen for changes
        dataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    // Handle the Voltage data fetched from the Realtime Database
                    val voltageValue = dataSnapshot.child("Voltage").getValue()
                    Log.d(TAG, "Voltage value is: $voltageValue")
                    // Update the TextView with the fetched Voltage data
                    when (voltageValue) {
                        is Long -> textViewVoltage.text = "Voltage: ${voltageValue}"
                        is Double -> textViewVoltage.text = "Voltage: ${voltageValue}"
                        else -> textViewVoltage.text = "Voltage: No data available"
                    }
                    

                    // Handle the Battery Percentage data fetched from the Realtime Database
                    val batteryPercentageValue = dataSnapshot.child("Battery Percentage").getValue()
                    Log.d(TAG, "Battery Percentage value is: $batteryPercentageValue")

                    // Update the TextView with the fetched Battery Percentage data
                    when (batteryPercentageValue) {
                        is Long -> textViewBatteryPercentage.text = "Battery Percentage: ${batteryPercentageValue}"
                        is Double -> textViewBatteryPercentage.text = "Battery Percentage: ${batteryPercentageValue}"
                        else -> textViewBatteryPercentage.text = "Battery Percentage: No data available"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to read value.", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors that occur while fetching the data
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })


        // Get a reference to the "status" child node in the Realtime Database
        val statusRef = database.child("status")

        // Attach a ValueEventListener to the "status" child node to listen for changes
        statusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    // Handle the Light Bulb Status data fetched from the Realtime Database
                    val lightBulbStatusValue = dataSnapshot.child("Bulb Status").getValue(Boolean::class.java)
                    Log.d(TAG, "Light Bulb Status value is: $lightBulbStatusValue")

                    // Update the TextView with the fetched Light Bulb Status data
                    textViewLightBulbStatus.text = when (lightBulbStatusValue) {
                        true -> "Light Bulb is On"
                        false -> "Light Bulb is Off"
                        else -> "Light Bulb Status: No data available"
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to read value.", e)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle any errors that occur while fetching the data
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }


    companion object {
        private const val TAG = "MainActivity"
    }

    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            /* Showing the ProgressBar, Making the main design GONE */
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(
                    Charsets.UTF_8
                )
            }catch (e: Exception){
                response = null
            }
            return response
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: "+ SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))
                val temp = main.getString("temp")+"°C"
                val tempMin = "Min Temp: " + main.getString("temp_min")+"°C"
                val tempMax = "Max Temp: " + main.getString("temp_max")+"°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name")+", "+sys.getString("country")

                /* Populating extracted data into our views */

                findViewById<TextView>(R.id.temp).text = temp



                /* Views populated, Hiding the loader, Showing the main design */
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE

            } catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }}}





}

