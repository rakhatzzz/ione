package com.diploma.ione.service

import com.diploma.ione.domain.ZoneType
import com.diploma.ione.repo.CategoryZoneRepo
import com.diploma.ione.repo.TestCategoryRepo
import org.springframework.stereotype.Service

@Service
class TestScoringService(
    private val categoryRepo: TestCategoryRepo,
    private val zoneRepo: CategoryZoneRepo
) {
    fun resolveZones(scoresByCategory: Map<Long, Int>): Pair<Map<Long, ZoneType>, ZoneType> {
        val categoryIds = scoresByCategory.keys.toList()
        val zones = zoneRepo.findAllByCategoryIdIn(categoryIds).groupBy { it.category.id!! }

        val zoneByCategory = mutableMapOf<Long, ZoneType>()

        for ((catId, score) in scoresByCategory) {

            val thresholds = zones[catId].orEmpty()

            val matched = thresholds.firstOrNull {
                score >= it.minScore && score <= it.maxScore
            } ?: thresholds.maxByOrNull { it.priority }

            if (matched == null) {
                zoneByCategory[catId] = ZoneType.GREEN
                continue
            }

            zoneByCategory[catId] = matched.zone
        }

        val maxZone = zoneByCategory.values.maxByOrNull { severity(it) } ?: ZoneType.GREEN
        return zoneByCategory to maxZone
    }

    private fun severity(z: ZoneType): Int = when (z) {
        ZoneType.GREEN -> 0
        ZoneType.YELLOW -> 1
        ZoneType.RED -> 2
        ZoneType.BLACK -> 3
    }
}
