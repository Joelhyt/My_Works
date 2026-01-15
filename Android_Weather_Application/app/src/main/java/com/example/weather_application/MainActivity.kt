package com.example.weather_application

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.weather_application.ui.theme.Weather_ApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val CUSTOM_CITIES_KEY = stringPreferencesKey("custom_cities_json")
private val DEFAULT_CITY_KEY = stringPreferencesKey("default_city_id")

// Default cities
data class City(val name: String, val latitude: Double, val longitude: Double, val isBuiltin: Boolean = false)

val BuiltinCities = listOf(
    City("Tampere", 61.4981, 23.7608, isBuiltin = true),
    City("Helsinki", 60.1695, 24.9354, isBuiltin = true),
    City("Oulu", 65.0121, 25.4651, isBuiltin = true),
    City("Turku", 60.4518, 22.2666, isBuiltin = true),
    City("Rovaniemi", 66.5039, 25.7294, isBuiltin = true)
)

// Main
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Weather_ApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WeatherAppRoot()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var customCities by remember { mutableStateOf<List<City>>(emptyList()) }

    var loaded by remember { mutableStateOf(false) }

    var selectedIndex by remember { mutableIntStateOf(0) }

    val allCities: List<City> = remember(customCities) { BuiltinCities + customCities }

    LaunchedEffect(Unit) {
        try {
            val prefs = context.dataStore.data.firstOrNull()

            if (prefs == null) {
                customCities = emptyList()
                selectedIndex = 0
                loaded = true
                return@LaunchedEffect
            }

            val customJson = prefs[CUSTOM_CITIES_KEY] ?: "[]"
            val loadedCustoms = try {
                parseCustomCitiesJson(customJson)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            customCities = loadedCustoms

            val combined = BuiltinCities + loadedCustoms
            val savedId = prefs[DEFAULT_CITY_KEY]
            var foundIndex = 0

            if (!savedId.isNullOrEmpty()) {
                try {
                    val parts = savedId.split("|")
                    if (parts.size == 3) {
                        val name = parts[0]
                        val lat = parts[1].toDoubleOrNull()
                        val lon = parts[2].toDoubleOrNull()
                        if (lat != null && lon != null) {
                            val idx = combined.indexOfFirst {
                                it.name == name && it.latitude == lat && it.longitude == lon
                            }
                            if (idx >= 0) foundIndex = idx
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    foundIndex = 0
                }
            }

            val safeIndex = if (foundIndex in combined.indices) foundIndex else 0
            selectedIndex = safeIndex

        } catch (e: Exception) {
            e.printStackTrace()
            selectedIndex = 0
            customCities = emptyList()
        } finally {
            loaded = true
        }
    }

    if (!loaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(customCities) {
        val maxIndex = (BuiltinCities + customCities).lastIndex
        if (maxIndex >= 0) {
            selectedIndex = selectedIndex.coerceIn(0, maxIndex)
        } else {
            selectedIndex = 0
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showHourly by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Weather — ${allCities.getOrNull(selectedIndex)?.name ?: "-"}") },
        )

        ScrollableTabRow(selectedTabIndex = selectedIndex) {
            allCities.forEachIndexed { index, city ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    text = { Text(city.name) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { showHourly = !showHourly }) {
                Text(if (showHourly) "Show Daily 15:00" else "Show Hourly")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { showAddDialog = true }) { Text("Add city") }

        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            val city = allCities.getOrNull(selectedIndex)
            if (city == null) {
                Text("No city selected")
            } else {
                WeatherCityPage(
                    city = city,
                    showHourly = showHourly,
                    onSetDefault = {
                        scope.launch {
                            val id = cityId(city.name, city.latitude, city.longitude)
                            context.dataStore.edit { prefs -> prefs[DEFAULT_CITY_KEY] = id }
                        }
                    },
                    onDelete = { toDelete ->
                        if (toDelete.isBuiltin) return@WeatherCityPage
                        val updated = customCities.filterNot {
                            it.name == toDelete.name && it.latitude == toDelete.latitude && it.longitude == toDelete.longitude
                        }

                        scope.launch {
                            context.dataStore.edit { prefs -> prefs[CUSTOM_CITIES_KEY] = citiesToJson(updated).toString() }
                        }

                        customCities = updated

                        val newAll = BuiltinCities + updated
                        val newIdx = newAll.indexOfFirst {
                            it.name == city.name && it.latitude == city.latitude && it.longitude == city.longitude
                        }
                        selectedIndex = if (newIdx in newAll.indices) newIdx else 0

                        scope.launch {
                            val prefs = context.dataStore.data.firstOrNull()
                            val defId = prefs?.get(DEFAULT_CITY_KEY)
                            if (!defId.isNullOrEmpty()) {

                                try {
                                    val parts = defId.split("|")
                                    if (parts.size == 3) {
                                        val name = parts[0]
                                        val lat = parts[1].toDoubleOrNull()
                                        val lon = parts[2].toDoubleOrNull()
                                        val stillExists = if (lat != null && lon != null) {
                                            (BuiltinCities + updated).any { it.name == name && it.latitude == lat && it.longitude == lon }
                                        } else false
                                        if (!stillExists) {
                                            context.dataStore.edit { p -> p.remove(DEFAULT_CITY_KEY) }
                                        }
                                    }
                                } catch (_: Exception) { /* ignore */ }
                            }
                        }
                    }
                )
            }
        }
    }

    // Add city dialog
    if (showAddDialog) {
        AddCityDialog(onDismiss = { showAddDialog = false }, onAdd = { name, latStr, lonStr ->
            val lat = latStr.toDoubleOrNull()
            val lon = lonStr.toDoubleOrNull()
            if (name.isBlank()) return@AddCityDialog
            if (lat == null || lon == null) return@AddCityDialog
            if (lat !in -90.0..90.0 || lon !in -180.0..180.0) return@AddCityDialog
            val newCity = City(name.trim(), lat, lon, isBuiltin = false)
            val updated = customCities + newCity
            scope.launch {
                context.dataStore.edit { prefs -> prefs[CUSTOM_CITIES_KEY] = citiesToJson(updated).toString() }
            }
            customCities = updated
            showAddDialog = false
        })
    }
}

@Composable
fun AddCityDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lon by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add city") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = lat, onValueChange = { lat = it }, label = { Text("Latitude") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = lon, onValueChange = { lon = it }, label = { Text("Longitude") }, modifier = Modifier.fillMaxWidth())
                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorText!!, color = MaterialTheme.colorScheme.error)
                }
            }

        },
        confirmButton = {
            Button(onClick = {
                val latD = lat.toDoubleOrNull()
                val lonD = lon.toDoubleOrNull()
                if (name.isBlank()) { errorText = "Name required"; return@Button }
                if (latD == null || lonD == null) { errorText = "Latitude and longitude must be numbers"; return@Button }
                if (latD !in -90.0..90.0 || lonD !in -180.0..180.0) { errorText = "Coordinates out of range"; return@Button }
                errorText = null
                onAdd(name, lat, lon)
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WeatherCityPage(city: City, showHourly: Boolean, onSetDefault: () -> Unit, onDelete: (City) -> Unit) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hourlyList by remember { mutableStateOf<List<HourlyEntry>>(emptyList()) }
    var dailyAt15 by remember { mutableStateOf<List<DailyEntry>>(emptyList()) }

    LaunchedEffect(city, showHourly) {
        loading = true
        errorMessage = null
        hourlyList = emptyList()
        dailyAt15 = emptyList()
        try {
            val (hours, daily) = fetchWeather(city.latitude, city.longitude)
            hourlyList = hours
            dailyAt15 = daily
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error"
        } finally {
            loading = false
        }
    }

    if (loading) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Loading weather...")
        }
        return
    }

    if (errorMessage != null) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Error: $errorMessage")
            if (!city.isBuiltin) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { onDelete(city) }) { Text("Delete city") }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onSetDefault) { Text("Set as default") }
            if (!city.isBuiltin) {
                Button(onClick = { onDelete(city) }) { Text("Delete city") }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (showHourly) {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val todays = hourlyList.filter { it.time.startsWith(today + "T") }
            if (todays.isEmpty()) {
                Text("No hourly data for today")
                return
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)) {
                todays.forEach { e ->
                    WeatherRow(
                        time = e.time.substringAfter("T").substring(0, 5), // "15:00"
                        temp = e.temperature,
                        precip = e.precipitation
                    )
                    Divider()
                }
            }
        } else {
            if (dailyAt15.isEmpty()) {
                Text("No daily 15:00 data")
                return
            }
            Column(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)) {
                dailyAt15.forEach { d ->
                    WeatherRow(time = d.date + " 15:00", temp = d.temperatureAt15, precip = d.precipitationAt15)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun WeatherRow(time: String, temp: Double?, precip: Double?) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(time)
            Text("Temp: ${temp?.let { String.format("%.1f °C", it) } ?: "—"}")
        }
        Text("Rain: ${precip?.let { String.format("%.1f mm", it) } ?: "—"}")
    }
}


data class HourlyEntry(val time: String, val temperature: Double?, val precipitation: Double?)
data class DailyEntry(val date: String, val temperatureAt15: Double?, val precipitationAt15: Double?)


suspend fun fetchWeather(lat: Double, lon: Double): Pair<List<HourlyEntry>, List<DailyEntry>> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()


        val today = LocalDate.now()
        val endDate = today.plusDays(6)
        val formatter = DateTimeFormatter.ISO_DATE
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&hourly=temperature_2m,precipitation&timezone=auto&start_date=${today.format(formatter)}&end_date=${endDate.format(formatter)}"

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) throw Exception("Network error: ${response.code}")

        val body = response.body?.string() ?: throw Exception("Empty response body")

        val json = JSONObject(body)
        val hourly = json.getJSONObject("hourly")
        val times = jsonArrayToStringList(hourly.getJSONArray("time"))
        val temps = jsonArrayToDoubleList(hourly.optJSONArray("temperature_2m"))
        val precips = jsonArrayToDoubleList(hourly.optJSONArray("precipitation"))

        val hourlyList = mutableListOf<HourlyEntry>()
        for (i in times.indices) {
            val t = times.getOrNull(i)
            val temp = temps.getOrNull(i)
            val pr = precips.getOrNull(i)
            if (t != null) hourlyList.add(HourlyEntry(t, temp, pr))
        }


        val dailyAt15 = mutableListOf<DailyEntry>()
        var cursorDate = today
        for (i in 0..6) {
            val dateStr = cursorDate.format(formatter)
            val targetTime = "${dateStr}T15:00"

            val exact = hourlyList.find { it.time == targetTime || it.time.startsWith(dateStr + "T15:00") }
            if (exact != null) {
                dailyAt15.add(DailyEntry(dateStr, exact.temperature, exact.precipitation))
            } else {

                val candidates = hourlyList.filter { it.time.startsWith(dateStr + "T") }
                if (candidates.isNotEmpty()) {
                    val chosen = candidates.minByOrNull { kotlin.math.abs(extractHour(it.time) - 15) }
                    dailyAt15.add(DailyEntry(dateStr, chosen?.temperature, chosen?.precipitation))
                } else {
                    dailyAt15.add(DailyEntry(dateStr, null, null))
                }
            }
            cursorDate = cursorDate.plusDays(1)
        }

        Pair(hourlyList, dailyAt15)
    }
}


fun jsonArrayToStringList(arr: JSONArray): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until arr.length()) list.add(arr.optString(i))
    return list
}

fun jsonArrayToDoubleList(arr: org.json.JSONArray?): List<Double?> {
    if (arr == null) return emptyList()
    val list = mutableListOf<Double?>()
    for (i in 0 until arr.length()) {
        if (arr.isNull(i)) list.add(null) else list.add(arr.optDouble(i))
    }
    return list
}

fun extractHour(timeStr: String): Int {
    return try {
        val parts = timeStr.split('T')
        if (parts.size < 2) return 0
        val hh = parts[1].split(':')[0]
        hh.toIntOrNull() ?: 0
    } catch (e: Exception) { 0 }
}


fun citiesToJson(cities: List<City>): JSONArray {
    val arr = JSONArray()
    cities.forEach { c ->
        val o = JSONObject()
        o.put("name", c.name)
        o.put("lat", c.latitude)
        o.put("lon", c.longitude)
        arr.put(o)
    }
    return arr
}

fun parseCustomCitiesJson(jsonStr: String): List<City> {
    return try {
        val arr = JSONArray(jsonStr)
        val list = mutableListOf<City>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val name = o.optString("name")
            val lat = o.optDouble("lat")
            val lon = o.optDouble("lon")
            list.add(City(name, lat, lon, isBuiltin = false))
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}

fun cityToJson(city: City): JSONObject {
    val o = JSONObject()
    o.put("name", city.name)
    o.put("lat", city.latitude)
    o.put("lon", city.longitude)
    return o
}

fun cityId(name: String, lat: Double, lon: Double): String =
    "${name.trim()}|${lat}|${lon}"

fun cityFromJson(o: JSONObject): City {
    return City(o.optString("name"), o.optDouble("lat"), o.optDouble("lon"), isBuiltin = false)
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    Weather_ApplicationTheme {
        WeatherAppRoot()
    }
}
