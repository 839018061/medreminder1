package com.example.medreminder.importexport

import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug
import org.json.JSONArray
import org.json.JSONObject

object RegimenCodec {
    fun encode(drugs: List<Drug>, schedules: List<DoseSchedule>): String {
        val root = JSONObject()
        val drugsArr = JSONArray()
        drugs.forEach { d ->
            val o = JSONObject()
            o.put("name", d.name)
            o.put("dosage", d.dosage)
            o.put("note", d.note)
            val scheds = JSONArray()
            schedules.filter { it.drugId == d.id }.forEach { s ->
                val so = JSONObject()
                so.put("time", s.time)
                so.put("timing", s.timing)
                so.put("repeatDays", JSONArray(s.repeatDays))
                scheds.put(so)
            }
            o.put("schedules", scheds)
            drugsArr.put(o)
        }
        root.put("version", 1)
        root.put("drugs", drugsArr)
        return root.toString()
    }

    data class ImportedDrug(val drug: Drug, val schedules: List<DoseSchedule>)

    fun decode(json: String): List<ImportedDrug> {
        val root = JSONObject(json)
        val drugsArr = root.getJSONArray("drugs")
        val result = mutableListOf<ImportedDrug>()
        for (i in 0 until drugsArr.length()) {
            val o = drugsArr.getJSONObject(i)
            val name = o.optString("name", "未知药品")
            val dosage = o.optString("dosage", "")
            val note = o.optString("note", "")
            val schedules = mutableListOf<DoseSchedule>()
            if (o.has("schedules")) {
                val arr = o.getJSONArray("schedules")
                for (j in 0 until arr.length()) {
                    val so = arr.getJSONObject(j)
                    val repeat = mutableListOf<Int>()
                    val rd = so.optJSONArray("repeatDays")
                    if (rd != null) for (k in 0 until rd.length()) repeat.add(rd.getInt(k))
                    schedules.add(
                        DoseSchedule(
                            drugId = 0,
                            time = so.optString("time", "08:00"),
                            timing = so.optString("timing", "早餐后"),
                            repeatDays = repeat.ifEmpty { listOf(1,2,3,4,5,6,7) }
                        )
                    )
                }
            }
            result.add(ImportedDrug(Drug(name = name, dosage = dosage, note = note), schedules))
        }
        return result
    }
}
