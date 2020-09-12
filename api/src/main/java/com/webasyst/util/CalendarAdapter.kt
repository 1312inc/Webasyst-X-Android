package com.webasyst.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarAdapter : JsonDeserializer<Calendar> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-DD HH:mm:ss", Locale.ROOT)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(json.asString)!!
        return calendar
    }
}
