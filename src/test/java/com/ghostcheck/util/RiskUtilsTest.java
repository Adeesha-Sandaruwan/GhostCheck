package com.ghostcheck.util;

import com.ghostcheck.entity.BreachRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiskUtilsTest {

    @Test
    void normalizeRiskScore_handlesEmptyAndBounds() {
        assertEquals(0, RiskUtils.normalizeRiskScore(List.of()));
        int score = RiskUtils.normalizeRiskScore(List.of(
            BreachRecord.builder().sourceName("A").breachDate(Instant.now()).exposedData("email,password").build(),
            BreachRecord.builder().sourceName("B").breachDate(Instant.now().minusSeconds(3600)).exposedData("email").build()
        ));
        assertTrue(score >= 0 && score <= 100);
    }

    @Test
    void riskLabel_mapsRanges() {
        assertEquals("Low", RiskUtils.riskLabel(0));
        assertEquals("Low", RiskUtils.riskLabel(33));
        assertEquals("Moderate", RiskUtils.riskLabel(34));
        assertEquals("Moderate", RiskUtils.riskLabel(66));
        assertEquals("High", RiskUtils.riskLabel(67));
        assertEquals("High", RiskUtils.riskLabel(100));
    }

    @Test
    void riskColor_mapsRanges() {
        assertEquals("green", RiskUtils.riskColor(0));
        assertEquals("green", RiskUtils.riskColor(33));
        assertEquals("yellow", RiskUtils.riskColor(34));
        assertEquals("yellow", RiskUtils.riskColor(66));
        assertEquals("red", RiskUtils.riskColor(67));
        assertEquals("red", RiskUtils.riskColor(100));
    }
}

