package com.renato.tvz_raspored.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.renato.tvz_raspored.data.model.CourseInfo
import java.lang.reflect.Type

class ScheduleItemDeserializer : JsonDeserializer<CourseInfo> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): CourseInfo {
        val jsonObject = json?.asJsonObject

        val title = jsonObject?.get("title")?.asString.orEmpty()
        val parts = title.split("<br/>")

        return if (parts.size >= 5) {
            val color = if(jsonObject?.get("color")?.isJsonNull != true) {
                jsonObject?.get("color")?.asString.orEmpty()
            } else if(parts[0].contains("Audit")) {
                "#F08080"
            } else {
                "#6BA5C2"
            }

            CourseInfo(
                title = parts[0].substring(8, parts[0].length).split("</strong>").joinToString(" "),
                lecturer = parts[1],
                classRoom = parts[2],
                direction = parts[3],
                start = jsonObject?.get("start")?.asString.orEmpty(),
                end = jsonObject?.get("end")?.asString.orEmpty(),
                color = color
            )
        } else {
            CourseInfo(title, "", "", "", "", "", "")
        }
    }
}