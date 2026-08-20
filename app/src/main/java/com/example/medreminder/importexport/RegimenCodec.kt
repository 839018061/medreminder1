package com.example.medreminder.importexport

import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Regimen(
    val version: Int = 1,
    val drugs: List<DrugItem> = emptyList()
)

@Serializable
data class DrugItem(
    val name: String,
    val dosage: String,
    val remark: String = "",
    val times: List<TimeItem> = emptyList()
)

@Serializable
data class TimeItem(
    val time: String,
    val repeatDays: String,
    val relation: String = "无"
)

object RegimenCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(regimen: Regimen): String = json.encodeToString(regimen)

    fun decode(text: String): Regimen = json.decodeFromString(text)

    fun regimenToEntities(regimen: Regimen): List<Pair<Drug, List<DoseSchedule>>> =
        regimen.drugs.map { item ->
            val drug = Drug(name = item.name, dosage = item.dosage, remark = item.remark)
            val schedules = item.times.map { t ->
                DoseSchedule(drugId = drug.id, time = t.time, repeatDays = t.repeatDays, relation = t.relation)
            }
            drug to schedules
        }

    fun entitiesToRegimen(drugs: List<Drug>, schedules: List<DoseSchedule>): Regimen =
        Regimen(
            drugs = drugs.map { d ->
                DrugItem(
                    name = d.name,
                    dosage = d.dosage,
                    remark = d.remark,
                    times = schedules.filter { it.drugId == d.id }.map { s ->
                        TimeItem(s.time, s.repeatDays, s.relation)
                    }
                )
            }
        )
}
