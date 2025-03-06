import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.safemap.model.LocationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
