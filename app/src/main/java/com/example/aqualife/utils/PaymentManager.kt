package com.example.aqualife.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object PaymentManager {

    // Function to call PHP API to get payment link
    fun requestMomoPayment(context: Context, amount: Int) {
        val createOrderUrl = "http://10.0.2.2:8000/momo_create.php?amount=$amount"
        // Note: 10.0.2.2 is the computer's localhost when running on Android Emulator
        // If running on a real phone, you must use LAN IP (e.g. 192.168.1.x)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(createOrderUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val text = connection.inputStream.bufferedReader().readText()
                    val json = JSONObject(text)

                    if (json.getString("status") == "success") {
                        val payUrl = json.getString("payUrl") 

                        // Switch to Main Thread to open MoMo App 
                        withContext(Dispatchers.Main) { 
                            openMomoApp(context, payUrl) 
                        } 
                    } else { 
                        withContext(Dispatchers.Main) { 
                            Toast.makeText(context, "Error creating MoMo order", Toast.LENGTH_SHORT).show() 
                        } 
                    } 
                } 
            } catch (e: Exception) { 
                e.printStackTrace() 
                withContext(Dispatchers.Main) { 
                    Toast.makeText(context, "Server connection error: ${e.message}", Toast.LENGTH_SHORT).show() 
                } 
            } 
        } 
    } 

    // Function to open DeepLink MoMo 
    private fun openMomoApp(context: Context, url: String) { 
        try { 
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            // If MoMo is not installed on the device, it will open the web browser
            Toast.makeText(context, "Opening payment browser...", Toast.LENGTH_SHORT).show()
        }
    }
}

