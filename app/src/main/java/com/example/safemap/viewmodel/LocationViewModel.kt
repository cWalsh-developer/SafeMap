import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.safemap.model.LocationData

class LocationViewModel : ViewModel() {
    private val _location = mutableStateOf<LocationData?>(null)
    val location: State<LocationData?> = _location

    private var locationReceiver: BroadcastReceiver? = null

    fun updateLocation(newLocation: LocationData) {
        _location.value = newLocation
    }

    fun registerReceiver(context: Context) {
        if (locationReceiver != null) {
            return
        }

        locationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: return
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                updateLocation(LocationData(latitude, longitude))
            }
        }

        val filter = IntentFilter("com.example.safemap.LOCATION_UPDATE")
        LocalBroadcastManager.getInstance(context).registerReceiver(locationReceiver!!, filter)
    }

    override fun onCleared() {
        super.onCleared()
        locationReceiver?.let {
        }
    }
}
