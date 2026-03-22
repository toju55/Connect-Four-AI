package de.toju.connectfourai.model;

import java.util.List;
import java.util.Map;

public record SimulationResult(List<AIRanking> rankings, Map<String, Map<String, MatchStats>> matchStats) {
}