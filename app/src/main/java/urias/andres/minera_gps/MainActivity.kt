package urias.andres.minera_gps

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_matricula.*
import kotlinx.android.synthetic.main.dialog_matricula.view.*
import java.util.*


class MainActivity : AppCompatActivity(){
    lateinit var myHandler : Handler
    var matriculaClave: String? = null
    var pedirMatricula: Boolean = false

    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private var lat = 0.0
    private var long = 0.0
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 30
        fastestInterval = 10
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        maxWaitTime = 60
    }

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                Toast.makeText(
                    this@MainActivity,
                    "Got Location: " + location.toString(),
                    Toast.LENGTH_LONG
                )
                    .show()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            val intento = Intent(this, ReportarMaterialActivity::class.java)
            intento.putExtra("matricula", matriculaClave)
            startActivity(intento)
        }
        if(!pedirMatricula){
            onCreateDialog()
        }
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()
        var cont=-1
        val handler: Handler = Handler()
        val run = object : Runnable {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun run() {
                if(pedirMatricula){
                    if(cont==-1){
                        cont=0
                        lastLocation()
                    }
                    if(cont==15){
                        lastLocation()
                        cont=0
                    }
                    else{
                        cont++
                    }
                    contador.setText(""+cont+" segundos")
                }
                handler.postDelayed(this,1000)
            }
        }
        handler.post(run)


    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun lastLocation(){
        checkLocationPermission()
        fusedLocationProvider!!.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                    val currentDate = sdf.format(Date())
                    lat = location.latitude
                    long = location.longitude
                    tiempo.setText(""+currentDate)

                }

            }
    }


    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationProvider?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationProvider?.removeLocationUpdates(locationCallback)
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Permisos de ubicación necesarios")
                    .setMessage("Esta aplicación oupa los permisos de ubicación, por favor acepta el uso de la funcionalidad de ubicación")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            } else {
                // No explanation needed, we can request the permission.
                requestLocationPermission()
            }
        } else {
            checkBackgroundLocation()
        }
    }

    private fun checkBackgroundLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBackgroundLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocationProvider?.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )

                        // Now check background location
                        checkBackgroundLocation()
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permiso denegado", Toast.LENGTH_LONG).show()

                    // Check if we are in a state where the user has denied the permission and
                    // selected Don't ask again
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.packageName, null),
                            ),
                        )
                    }
                }
                return
            }
            MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocationProvider?.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )

                        Toast.makeText(
                            this,
                            "Permisos de ubicación en segundo plano otorgados",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permiso denegado", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        private const val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66
    }

    fun onCreateDialog() {
            // Build the dialog and set up the button click handlers
        val context = this
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.dialog_matricula, null)
        builder.setView(view)
        builder.setPositiveButton("Iniciar sesión") { dialog, id ->
            // Send the positive button event back to the host activity
            var isValid = true
            if (view.matricula.text.isBlank()) {
                isValid = false
            }
            if (isValid) {
                val valor = view.matricula.text.toString()
                matriculaClave = valor
                pedirMatricula = true
            }
            if (!isValid) {
                dialog.dismiss()
            }
        }
        builder.show()
    }
}
