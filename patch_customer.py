import re

with open("app/src/main/java/com/pourify/distribution/ui/screens/CustomerDetailScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports = """import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import androidx.compose.material.icons.filled.PinDrop
"""
content = re.sub(r'import com.pourify.distribution.utils.WhatsAppDispatcher\n', imports + 'import com.pourify.distribution.utils.WhatsAppDispatcher\n', content)

# Add state and permission launcher
state_code = """
    val context = LocalContext.current
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationText by remember { mutableStateOf("Lat: ${customer?.geoLatitude ?: 0.0}, Lng: ${customer?.geoLongitude ?: 0.0}") }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.updateCustomerLocation(customerId, location.latitude, location.longitude)
                        locationText = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
"""
content = re.sub(r'val context = LocalContext.current\s+', state_code + '\n    ', content)

# Replace Box content
box_target = """Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "Map Placeholder",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )"""
box_replacement = """Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = locationText, style = Typography.bodySmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedButton(
                                        onClick = {
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                try {
                                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                                        if (location != null) {
                                                            viewModel.updateCustomerLocation(customerId, location.latitude, location.longitude)
                                                            locationText = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                                                        }
                                                    }
                                                } catch (e: SecurityException) {
                                                    e.printStackTrace()
                                                }
                                            } else {
                                                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                            }
                                        },
                                        modifier = Modifier.height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Filled.PinDrop, contentDescription = "Pin Location", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pin Location", style = Typography.labelSmall)
                                    }
                                }"""
content = content.replace(box_target, box_replacement)

with open("app/src/main/java/com/pourify/distribution/ui/screens/CustomerDetailScreen.kt", "w") as f:
    f.write(content)
