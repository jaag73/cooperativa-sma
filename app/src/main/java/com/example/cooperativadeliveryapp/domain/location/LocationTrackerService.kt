package com.example.cooperativadeliveryapp.domain.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.example.cooperativadeliveryapp.domain.model.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
open class LocationTrackerService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    open suspend fun getCurrentLocation(): GeoLocation {
        return try {
            val location: Location? = fusedLocationClient.lastLocation.await()
            if (location != null) {
                GeoLocation(location.latitude, location.longitude)
            } else {
                GeoLocation(20.9144, -100.7452) // Default SMA
            }
        } catch (e: Exception) {
            GeoLocation(20.9144, -100.7452)
        }
    }

    // Haversine formula to calculate distance in kilometers
    open fun calculateDistanceKm(loc1: GeoLocation, loc2: GeoLocation): Double {
        val r = 6371.0 // Earth radius in km
        val latDistance = Math.toRadians(loc2.latitude - loc1.latitude)
        val lonDistance = Math.toRadians(loc2.longitude - loc1.longitude)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(loc1.latitude)) * cos(Math.toRadians(loc2.latitude)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
