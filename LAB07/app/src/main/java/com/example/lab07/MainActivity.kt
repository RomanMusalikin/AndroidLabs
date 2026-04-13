package com.example.lab07

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

// Для геолокации
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.*
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError

import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.DrivingRouterType

import com.yandex.mapkit.search.Session as SearchSession

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var etFrom: TextInputEditText
    private lateinit var etTo: TextInputEditText
    private lateinit var mapObjects: MapObjectCollection

    private lateinit var searchManagerFrom: SearchManager
    private lateinit var searchManagerTo: SearchManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var pointA: Point? = null
    private var pointB: Point? = null

    private var searchSessionFrom: SearchSession? = null
    private var searchSessionTo: SearchSession? = null
    private var drivingSession: DrivingSession? = null

    companion object {
        private const val LOCATION_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        etFrom = findViewById(R.id.etFrom)
        etTo = findViewById(R.id.etTo)
        val btnRoute = findViewById<MaterialButton>(R.id.btnRoute)
        val fabMyLocation = findViewById<FloatingActionButton>(R.id.fabMyLocation)

        mapObjects = mapView.map.mapObjects.addCollection()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        searchManagerFrom = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        searchManagerTo = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)

        mapView.map.move(CameraPosition(Point(55.751244, 37.618423), 11.0f, 0.0f, 0.0f))

        // Обработка кнопки GPS
        fabMyLocation.setOnClickListener {
            requestLocationAndMove()
        }

        // Обработка кнопки маршрута
        btnRoute.setOnClickListener {
            val fromQuery = etFrom.text.toString().trim()
            val toQuery = etTo.text.toString().trim()

            if (fromQuery.isNotEmpty() && toQuery.isNotEmpty()) {
                hideKeyboard()

                searchSessionFrom?.cancel()
                searchSessionTo?.cancel()
                drivingSession?.cancel()
                mapObjects.clear()

                Toast.makeText(this, "Ищем точки...", Toast.LENGTH_SHORT).show()

                // УМНАЯ ЛОГИКА: Проверяем, не использовал ли юзер GPS для первой точки
                val useMyLoc = (fromQuery == "Моё местоположение" && pointA != null)

                if (useMyLoc) {
                    // Возвращаем маркер GPS на карту
                    mapObjects.addPlacemark(pointA!!).apply {
                        setIcon(ImageProvider.fromResource(this@MainActivity, android.R.drawable.ic_menu_mylocation))
                    }
                    // Ищем только пункт Б
                    searchPlace(toQuery, isFrom = false)
                } else {
                    pointA = null
                    // Запускаем поиск обеих точек
                    searchPlace(fromQuery, isFrom = true)
                    searchPlace(toQuery, isFrom = false)
                }
                pointB = null

            } else {
                Toast.makeText(this, "Заполните оба поля", Toast.LENGTH_SHORT).show()
            }
        }

        // При старте запрашиваем права
        requestLocationPermission()
    }

    // ===================== ГЕОЛОКАЦИЯ =====================
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
        }
    }

    // ===================== ГЕОЛОКАЦИЯ (ХАРДКОД ДЛЯ ЛАБЫ) =====================
    private fun requestLocationAndMove() {
        // Просто игнорируем датчик GPS эмулятора и сразу задаем координаты МАДИ
        val madiPoint = Point(55.800318, 37.531608)

        pointA = madiPoint
        etFrom.setText("Моё местоположение")

        // Перемещаем камеру
        mapView.map.move(CameraPosition(madiPoint, 15.0f, 0.0f, 0.0f))

        // Ставим синий маркер
        mapObjects.clear()
        mapObjects.addPlacemark(madiPoint).apply {
            setIcon(ImageProvider.fromResource(this@MainActivity, android.R.drawable.ic_menu_mylocation))
        }

        Toast.makeText(this, "Телепортировались в МАДИ!", Toast.LENGTH_SHORT).show()
    }

    // ===================== АСИНХРОННЫЙ ПОИСК =====================
    private fun searchPlace(query: String, isFrom: Boolean) {
        val currentManager = if (isFrom) searchManagerFrom else searchManagerTo

        val session = currentManager.submit(query, VisibleRegionUtils.toPolygon(mapView.map.visibleRegion), SearchOptions(),
            object : SearchSession.SearchListener {
                override fun onSearchResponse(response: Response) {
                    val point = response.collection.children.firstOrNull()?.obj?.geometry?.firstOrNull()?.point
                    if (point != null) {
                        if (isFrom) pointA = point else pointB = point

                        mapObjects.addPlacemark(point).apply {
                            setIcon(ImageProvider.fromResource(this@MainActivity, android.R.drawable.ic_menu_compass))
                        }

                        if (pointA != null && pointB != null) {
                            buildRoute(pointA!!, pointB!!)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Не найдено: $query", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onSearchError(error: Error) {
                    Toast.makeText(this@MainActivity, "Ошибка поиска: $query", Toast.LENGTH_SHORT).show()
                }
            }
        )

        if (isFrom) searchSessionFrom = session else searchSessionTo = session
    }

    // ===================== АВТОМОБИЛЬНЫЙ МАРШРУТ =====================
    private fun buildRoute(from: Point, to: Point) {
        try {
            val drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)

            val points = listOf(
                RequestPoint(from, RequestPointType.WAYPOINT, null, null),
                RequestPoint(to, RequestPointType.WAYPOINT, null, null)
            )

            drivingSession = drivingRouter.requestRoutes(points, DrivingOptions(), VehicleOptions(),
                object : DrivingSession.DrivingRouteListener {
                    override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
                        if (routes.isNotEmpty()) {
                            val polyline = routes.first().geometry
                            mapObjects.addPolyline(polyline).apply {
                                setStrokeColor(Color.parseColor("#4488FF"))
                                setStrokeWidth(5.0f)
                            }

                            mapView.map.move(CameraPosition(from, 13.0f, 0.0f, 0.0f))
                            Toast.makeText(this@MainActivity, "Маршрут построен!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onDrivingRoutesError(error: Error) {
                        Toast.makeText(this@MainActivity, "Не удалось построить маршрут", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } catch (e: Throwable) {
            Toast.makeText(this, "Системная ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // ===================== УТИЛИТЫ =====================
    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

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