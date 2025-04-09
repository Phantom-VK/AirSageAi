package com.vikram.airsageai.utils

import com.google.firebase.database.PropertyName


data class GasReading(
    @get:PropertyName("CO (PPM)")
    @set:PropertyName("CO (PPM)")
    var CO_PPM: Double? = null,

    @get:PropertyName("CO2 (PPM)")
    @set:PropertyName("CO2 (PPM)")
    var CO2_PPM: Double? = null,

    @get:PropertyName("NH3 (PPM)")
    @set:PropertyName("NH3 (PPM)")
    var NH3_PPM: Double? = null,

    @get:PropertyName("NOx (PPM)")
    @set:PropertyName("NOx (PPM)")
    var NOx_PPM: Double? = null,

    @get:PropertyName("LPG (PPM)")
    @set:PropertyName("LPG (PPM)")
    var LPG_PPM: Double? = null,

    @get:PropertyName("Methane (PPM)")
    @set:PropertyName("Methane (PPM)")
    var Methane_PPM: Double? = null,

    @get:PropertyName("Hydrogen (PPM)")
    @set:PropertyName("Hydrogen (PPM)")
    var Hydrogen_PPM: Double? = null
)




