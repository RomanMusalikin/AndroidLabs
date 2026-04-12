package com.example.lab07

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

// Основные импорты MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.*
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.*
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError

// Алиасы для разрешения конфликта имён Session
import com.yandex.mapkit.search.Session as SearchSession
import com.yandex.mapkit.transport.masstransit.Session as RouteSession

class MainActivity : AppCompatActivity(), SearchSession.SearchListener {

    private lateinit var mapView: MapView
    private lateinit var etSearch: TextInputEditText
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchManager: SearchManager
    private lateinit var mapObjects: MapObjectCollection

    // Точки для маршрута
    private var pointA: Point? = null  // текущее местоположение
    private var pointB: Point? = null  // найденная точка

    companion object {
        private const val LOCATION_PERMISSION_CODE = 1001
        // Москва по умолчанию
        private val DEFAULT_POINT = Point(55.751244, 37.618423)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Инициализация элементов
        mapView = findViewById(R.id.mapView)
        etSearch = findViewById(R.id.etSearch)
        val btnSearch = findViewById<MaterialButton>(R.id.btnSearch)
        val btnMyLocation = findViewById<MaterialButton>(R.id.btnMyLocation)
        val btnRoute = findViewById<MaterialButton>(R.id.btnRoute)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        searchManager = SearchFactory.getInstance().createSearchManager(
            SearchManagerType.COMBINED
        )
        mapObjects = mapView.map.mapObjects.addCollection()

        // 2. Начальная позиция камеры — Москва
        mapView.map.move(
            CameraPosition(DEFAULT_POINT, 12.0f, 0.0f, 0.0f)
        )

        // 3. Кнопка "Моё место"
        btnMyLocation.setOnClickListener {
            requestLocationAndMove()
        }

        // 4. Кнопка "Найти"
        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                searchPlace(query)
            }
        }

        // 5. Кнопка "Маршрут"
        btnRoute.setOnClickListener {
            if (pointA != null && pointB != null) {
                buildRoute(pointA!!, pointB!!)
            } else {
                Toast.makeText(
                    this,
                    "Сначала определите своё место и найдите точку назначения",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // 6. Запросить разрешение на геолокацию при старте
        requestLocationPermission()
    }

    // ===================== ГЕОЛОКАЦИЯ =====================

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    private fun requestLocationAndMove() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userPoint = Point(location.latitude, location.longitude)
                pointA = userPoint

                // Перемещаем камеру
                mapView.map.move(
                    CameraPosition(userPoint, 15.0f, 0.0f, 0.0f)
                )

                // Ставим маркер
                mapObjects.clear()
                mapObjects.addPlacemark(userPoint).apply {
                    setIcon(ImageProvider.fromResource(
                        this@MainActivity,
                        android.R.drawable.ic_menu_mylocation
                    ))
                }

                Toast.makeText(this, "Вы здесь!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Не удалось определить местоположение", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Разрешение получено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ===================== ПОИСК =====================

    private fun searchPlace(query: String) {
        searchManager.submit(
            query,
            VisibleRegionUtils.toPolygon(mapView.map.visibleRegion),
            SearchOptions(),
            this
        )
    }

    override fun onSearchResponse(response: Response) {
        val results = response.collection.children
        if (results.isNotEmpty()) {
            val point = results.first().obj?.geometry?.firstOrNull()?.point
            if (point != null) {
                pointB = point

                mapView.map.move(
                    CameraPosition(point, 14.0f, 0.0f, 0.0f)
                )

                mapObjects.addPlacemark(point).apply {
                    setIcon(ImageProvider.fromResource(
                        this@MainActivity,
                        android.R.drawable.ic_menu_compass
                    ))
                }

                val name = results.first().obj?.name ?: "Найдено"
                Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Ничего не найдено", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSearchError(error: Error) {
        val message = when (error) {
            is NetworkError -> "Ошибка сети"
            is RemoteError -> "Ошибка сервера"
            else -> "Неизвестная ошибка"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // ===================== МАРШРУТ =====================

    private fun buildRoute(from: Point, to: Point) {
        val router = TransportFactory.getInstance().createPedestrianRouter()

        val points = listOf(
            RequestPoint(from, RequestPointType.WAYPOINT, null, null),
            RequestPoint(to, RequestPointType.WAYPOINT, null, null)
        )

        router.requestRoutes(
            points,
            TimeOptions(),
            RouteOptions(), // <-- ДОБАВЛЕН ТОТ САМЫЙ ПРОПУЩЕННЫЙ ПАРАМЕТР
            object : RouteSession.RouteListener {

                override fun onMasstransitRoutes(routes: List<Route>) {
                    if (routes.isNotEmpty()) {
                        val polyline = routes.first().geometry
                        mapObjects.addPolyline(polyline).apply {
                            setStrokeColor(Color.parseColor("#4488FF"))
                            setStrokeWidth(5.0f)
                        }
                        Toast.makeText(this@MainActivity, "Маршрут построен", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onMasstransitRoutesError(error: Error) {
                    Toast.makeText(this@MainActivity, "Ошибка построения маршрута", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // ===================== ЖИЗНЕННЫЙ ЦИКЛ =====================

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}