package com.surendrasingh.connectivitytest.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Save all available networks with an internet connection to a set (@validNetworks).
 * As long as the size of the set > 0, this LiveData emits true.
 */

val TAG = "ConnectivityManager"

class ConnectionLiveData(context: Context) : LiveData<Boolean>() {

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val connectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()

    private fun checkValidNetworks() {
        postValue(validNetworks.size > 0)
    }

    override fun onActive() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onInactive() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        /**
         * Called when a network is detected.
         * If that network has internet, save it in the Set(validNetwork)
         **/

        override fun onAvailable(network: Network) {
            Log.d(TAG, "onAvailable: $network")
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
            Log.d(TAG, "onAvailable: ${network}, $hasInternetCapability")
            if (hasInternetCapability == true) {
                // check if this network actually has internet
                CoroutineScope(Dispatchers.IO).launch {
                    val hasInternet = DoesNetworkHaveInternet.execute(network.socketFactory)
                    if (hasInternet) {
                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "onAvailable: adding network. $network")
                            validNetworks.add(network)
                            checkValidNetworks()
                        }
                    }
                }
            }
        }

        /*
      Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onLost(android.net.Network)
     */
        /**
         *If the callback was registered with registerNetworkCallback()
         * it will be called for each network
         * which no longer satisfies the criteria of the callback.
         * */

        override fun onLost(network: Network) {
            Log.d(TAG, "onLost: $network")
            validNetworks.remove(network)
            checkValidNetworks()
        }
    }
}