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

import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.DrivingRouterType

// НОВЫЕ ИМПОРТЫ для долгого нажатия
import com.yandex.mapkit.map.InputListener
import android.app.AlertDialog
import java.util.Locale

import com.yandex.mapkit.search.Session as SearchSession
import kotlin.math.abs

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

    private var savedAzimuth: Float? = null

    // ГЛАВНОЕ ИСПРАВЛЕНИЕ: Выносим слушатель карты в переменную класса, чтобы его не удалил GC
    private val inputListener = object : InputListener {
        override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
            // Обычный короткий клик игнорируем
        }

        override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
            // При долгом клике вызываем наше меню
            showLongClickDialog(point)
        }
    }

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
        val fabCompass = findViewById<FloatingActionButton>(R.id.fabCompass)
        val btnZoomIn = findViewById<MaterialButton>(R.id.btnZoomIn)
        val btnZoomOut = findViewById<MaterialButton>(R.id.btnZoomOut)

        mapObjects = mapView.map.mapObjects.addCollection()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        searchManagerFrom = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        searchManagerTo = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)

        mapView.map.move(CameraPosition(Point(55.751244, 37.618423), 11.0f, 0.0f, 0.0f))

        // ПРИВЯЗЫВАЕМ СЛУШАТЕЛЬ ДОЛГОГО НАЖАТИЯ К КАРТЕ
        mapView.map.addInputListener(inputListener)

        // ===================== УПРАВЛЕНИЕ КАРТОЙ =====================

        btnZoomIn.setOnClickListener {
            val pos = mapView.map.cameraPosition
            mapView.map.move(
                CameraPosition(pos.target, pos.zoom + 1.0f, pos.azimuth, pos.tilt),
                com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 0.3f), null
            )
        }

        btnZoomOut.setOnClickListener {
            val pos = mapView.map.cameraPosition
            mapView.map.move(
                CameraPosition(pos.target, pos.zoom - 1.0f, pos.azimuth, pos.tilt),
                com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 0.3f), null
            )
        }

        fabCompass.setOnClickListener {
            val currentCamera = mapView.map.cameraPosition
            if (abs(currentCamera.azimuth) > 1.0f) {
                savedAzimuth = currentCamera.azimuth
                mapView.map.move(
                    CameraPosition(currentCamera.target, currentCamera.zoom, 0.0f, currentCamera.tilt),
                    com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 0.5f), null
                )
            } else {
                val targetAzimuth = savedAzimuth ?: 0.0f
                mapView.map.move(
                    CameraPosition(currentCamera.target, currentCamera.zoom, targetAzimuth, currentCamera.tilt),
                    com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 0.5f), null
                )
                savedAzimuth = null
            }
        }

        fabMyLocation.setOnClickListener {
            requestLocationAndMove()
        }

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

                val useMyLoc = (fromQuery == "Моё местоположение" && pointA != null)
                // Если в поле Откуда координаты (от долгого нажатия)
                val isCoordsFrom = fromQuery.matches(Regex(".*[0-9]+\\.[0-9]+.*"))
                // Если в поле Куда координаты
                val isCoordsTo = toQuery.matches(Regex(".*[0-9]+\\.[0-9]+.*"))

                if (useMyLoc || isCoordsFrom) {
                    pointA?.let {
                        mapObjects.addPlacemark(it).apply {
                            setIcon(ImageProvider.fromResource(this@MainActivity, android.R.drawable.ic_menu_mylocation))
                        }
                    }
                    if (!isCoordsTo) searchPlace(toQuery, isFrom = false)
                } else {
                    pointA = null
                    searchPlace(fromQuery, isFrom = true)
                }

                if (!isCoordsTo) {
                    if (!useMyLoc && !isCoordsFrom) searchPlace(toQuery, isFrom = false)
                } else {
                    pointB?.let {
                        mapObjects.addPlacemark(it).apply {
                            setIcon(ImageProvider.fromResource(this@MainActivity, android.R.drawable.ic_menu_compass))
                        }
                    }
                }

                if ((useMyLoc || isCoordsFrom) && isCoordsTo && pointA != null && pointB != null) {
                    buildRoute(pointA!!, pointB!!)
                }

            } else {
                Toast.makeText(this, "Заполните оба поля", Toast.LENGTH_SHORT).show()
            }
        }

        requestLocationPermission()
    }

    // ===================== МЕНЮ ДОЛГОГО КЛИКА =====================
    private fun showLongClickDialog(point: Point) {
        val options = arrayOf("Отсюда", "Сюда")
        AlertDialog.Builder(this)
            .setTitle("Выбрать точку")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> setPointFromMap(point, isFrom = true)
                    1 -> setPointFromMap(point, isFrom = false)
                }
            }
            .show()
    }

    private fun setPointFromMap(point: Point, isFrom: Boolean) {
        // Красиво форматируем координаты для текстового поля
        val coordsText = String.format(Locale.US, "%.5f, %.5f", point.latitude, point.longitude)

        if (isFrom) {
            pointA = point
            etFrom.setText(coordsText)
        } else {
            pointB = point
            etTo.setText(coordsText)
        }

        // Очищаем карту от старого маршрута
        mapObjects.clear()
        drivingSession?.cancel()

        // Заново рисуем маркер А (если он есть)
        pointA?.let {
            mapObjects.addPlacemark(it).apply {
                setIcon(ImageProvider.fromResource(this@MainActivity, android.R.drawable.ic_menu_mylocation))
            }
        }
        // Заново рисуем маркер Б (если он есть)
        pointB?.let {
            mapObjects.addPlacemark(it).apply {
                setIcon(ImageProvider.fromResource(this@MainActivity, android.R.drawable.ic_menu_compass))
            }
        }

        // АВТОМАРШРУТ: Если обе точки заданы - сразу строим путь!
        if (pointA != null && pointB != null) {
            buildRoute(pointA!!, pointB!!)
        }
    }

    // ===================== ГЕОЛОКАЦИЯ =====================
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
        }
    }

    private fun requestLocationAndMove() {
        val madiPoint = Point(55.800318, 37.531608)
        pointA = madiPoint
        etFrom.setText("Моё местоположение")

        mapView.map.move(CameraPosition(madiPoint, 15.0f, 0.0f, 0.0f))

        mapObjects.clear()
        drivingSession?.cancel()

        mapObjects.addPlacemark(madiPoint).apply {
            setIcon(ImageProvider.fromResource(this@MainActivity, android.R.drawable.ic_menu_mylocation))
        }

        pointB?.let {
            mapObjects.addPlacemark(it).apply {
                setIcon(ImageProvider.fromResource(this@MainActivity, android.R.drawable.ic_menu_compass))
            }
            buildRoute(pointA!!, pointB!!)
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

                            // Плавно отъезжаем, чтобы показать весь маршрут
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