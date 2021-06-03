package com.example.yelpapirestaurantsearch.ui

import android.app.AlertDialog
import android.content.*
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Bundle
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.subjects.PublishSubject


/**
 *This is the Base Fragment from which all other fragments are extended
 * Enables us to have a common functionality here that can be used everywhere
 */
open class RestaurantBaseFragment : Fragment() {

    private var connectivitySubject: PublishSubject<Boolean>? = null

    var connectivityManager: ConnectivityManager? = null

    companion object{
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectivitySubject = PublishSubject.create()
        connectivityManager =
            activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        checkInternetConnectivity()
    }

    fun displayAlertDialog(
        title: String,
        message: String,
        positiveButtonTitle: String,
        positiveClickCallback: (Any?) -> Unit,
        negativeButtonTitle: String,
        negativeButtonCallback: (Any?) -> Unit,
        extraData: Any?
    ){

        val alertDialog: AlertDialog? = this.let {
            val builder = AlertDialog.Builder(it.requireContext())

            builder.apply {
                setPositiveButton(positiveButtonTitle,
                    DialogInterface.OnClickListener { dialog, which ->
                        positiveClickCallback(extraData)
                        dialog.dismiss()
                    })

                setNegativeButton(negativeButtonTitle,
                    DialogInterface.OnClickListener { dialog, which ->
                        negativeButtonCallback(extraData)
                        dialog.dismiss()
                    })

                setTitle(title)
                setMessage(message)
            }
            builder.create()
        }
        alertDialog?.show()
    }

    fun displayAlertDialog(
        title: String,
        message: String,
        positiveButtonTitle: String,
        positiveClickCallback: () -> Unit
    ){

        val alertDialog: AlertDialog? = this.let {
            val builder = AlertDialog.Builder(it.requireContext())

            builder.apply {
                setPositiveButton(positiveButtonTitle,
                    DialogInterface.OnClickListener { dialog, which ->
                        positiveClickCallback()
                        dialog.dismiss()
                    })

                setTitle(title)
                setMessage(message)
            }
            builder.create()
        }
        alertDialog?.show()
    }

    override fun onResume() {
        super.onResume()

        connectivitySubject?.subscribe{ isOnline ->

            if (!isOnline){
                displayAlertDialog(
                    title = "No Internet Connectivity",
                    message = "Internet Connectivity is required to retrieve Burrito restaurants near by.",
                    positiveButtonTitle = "Ok",
                    positiveClickCallback = ::InternetConnectivityDialogCallback
                )
            }
        }
    }

    private fun InternetConnectivityDialogCallback(){

    }


    private fun checkInternetConnectivity(){
        if (SDK_INT >= LOLLIPOP) {
            val builder = NetworkRequest.Builder()
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            builder.addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            val callback: NetworkCallback = object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    connectivitySubject?.onNext(true)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    connectivitySubject?.onNext(false)
                }
            }
            connectivityManager?.registerNetworkCallback(builder.build(), callback)
        } else {
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            val receiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val noConnectivity = intent?.getBooleanExtra(
                        ConnectivityManager.EXTRA_NO_CONNECTIVITY, false
                    )
                    connectivitySubject?.onNext(noConnectivity)
                }
            }
            activity?.registerReceiver(receiver, filter)
        }
    }

}