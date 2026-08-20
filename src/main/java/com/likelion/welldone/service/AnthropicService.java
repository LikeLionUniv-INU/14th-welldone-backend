package com.likelion.welldone.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Anthropic API 호출 전담 서비스. API 키는 서버(application.yaml -> 환경변수)에만 존재하며
 * 컨트롤러/프론트로는 절대 노출되지 않습니다.
 */
@Service
public class AnthropicService {

  @Value("${anthropic.api-key}")
  private String apiKey;

  @Value("${anthropic.model}")
  private String model;

  private final HttpClient httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(10))
      .build();

  private final ObjectMapper objectMapper = JsonMapper.builder().build();

  /**
   * 스케줄 + 카테고리 + 사전질문 응답을 기반으로 AI 웰니스 루틴(1주차)을 생성합니다.
   * (기능명세서 STEP4~5: AI 분석 → 루틴 제안)
   */
  public JsonNode generateWellnessRoutines(JsonNode schedule, Iterable<String> categories, JsonNode preferences) throws Exception {
    String systemPrompt = """
                당신은 교대근무자(간호사 등)를 위한 웰니스 루틴 설계 전문가입니다.
                사용자의 근무 스케줄(D/E/N/OFF)과 선택한 웰니스 카테고리, 사전질문 응답을 바탕으로
                1주차 루틴 세트를 JSON으로만 반환하세요. 다른 설명 텍스트는 포함하지 마세요.""";

    String userPrompt = """
                근무 스케줄: %s
                선택 카테고리: %s
                사전질문 응답: %s

                다음 JSON 스키마로만 응답하세요:
                {
                  "briefingSummary": "string (월간 바이오브리핑 요약 1~2문장)",
                  "week1Routines": [
                    {
                      "group": "string (예: 출근 전 / 퇴근 후 수면 골든타임 / 근무 중)",
                      "routines": [
                        { "name": "string", "type": "VIDEO | GENERAL", "durationMinutes": number, "frequency": "string" }
                      ]
                    }
                  ]
                }""".formatted(schedule.toString(), String.join(", ", categories), preferences.toString());

    String rawText = callMessages(systemPrompt, userPrompt, 2000);
    return objectMapper.readTree(cleanJson(rawText));
  }

  /**
   * 지난 달 활동 데이터를 기반으로 월간 바이오브리핑 리포트를 생성합니다.
   * 매월 1일 배치 잡(MonthlyReportJob)에서 호출됩니다.
   */
  public JsonNode generateMonthlyReport(Map<String, Object> userSummary) throws Exception {
    String systemPrompt = """
                당신은 교대근무자의 웰니스 데이터를 분석해 월간 리포트를 작성하는 전문가입니다.
                achievementRate에 따라 badge를 다음 규칙으로 정하세요:
                - 70~100%: badge=BOOSTER, badgeLabel="Booster Mode", badgeMessage="높은 달성률로 루틴 전반을 완수했어요!"
                - 30~60%:  badge=MAINTAIN, badgeLabel="Maintain Mode", badgeMessage="핵심 습관 위주로 꾸준히 유지하고 있어요!"
                - 0~20%:   badge=SURVIVAL, badgeLabel="Survival Mode", badgeMessage="휴식 중심으로 루틴 강도가 완화된 상태예요"
                riskPeriods의 reason은 "연속 야간 3일 이상" 또는 "급격한 듀티 전환" 중 하나로 작성하세요.
                JSON으로만 응답하세요.""";

    String userPrompt = """
                지난 달 활동 요약: %s

                다음 JSON 스키마로 응답하세요:
                {
                  "achievementRate": number,
                  "activeDays": number,
                  "badge": "BOOSTER | MAINTAIN | SURVIVAL",
                  "badgeLabel": "string",
                  "badgeMessage": "string",
                  "recoveryRate": number,
                  "guideMessage": "string (회복 응원 문구)",
                  "categorySummary": [
                    { "category": "string (풀네임)", "achievementRate": number }
                  ],
                  "riskPeriods": [
                    { "label": "string (예: 7월 2주차 야간근무)", "reason": "string", "tip": "string" }
                  ]
                }""".formatted(objectMapper.writeValueAsString(userSummary));

    String rawText = callMessages(systemPrompt, userPrompt, 1500);
    return objectMapper.readTree(cleanJson(rawText));
  }

  private String callMessages(String systemPrompt, String userPrompt, int maxTokens) throws Exception {
    Map<String, Object> body = Map.of(
        "model", model,
        "max_tokens", maxTokens,
        "system", systemPrompt,
        "messages", new Object[]{ Map.of("role", "user", "content", userPrompt) }
    );

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.anthropic.com/v1/messages"))
        .header("Content-Type", "application/json")
        .header("x-api-key", apiKey)
        .header("anthropic-version", "2023-06-01")
        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() >= 300) {
      throw new RuntimeException("Anthropic API 호출 실패 (status=" + response.statusCode() + "): " + response.body());
    }

    JsonNode responseJson = objectMapper.readTree(response.body());
    JsonNode contentArray = responseJson.get("content");
    for (JsonNode block : contentArray) {
      if ("text".equals(block.get("type").asText())) {
        return block.get("text").asText();
      }
    }
    throw new RuntimeException("Anthropic 응답에서 text 블록을 찾을 수 없습니다.");
  }

  private String cleanJson(String raw) {
    return raw.trim().replaceAll("```json|```", "").trim();
  }

  /**
   * 명세서 10번 API 형식(weeklyBriefing + groups)에 맞춰 루틴을 생성합니다.
   */
  public JsonNode generateRoutineSuggestion(JsonNode schedule, Iterable<String> categories, JsonNode preferences) throws Exception {
    String systemPrompt = """
                당신은 교대근무자(간호사 등)를 위한 웰니스 루틴 설계 전문가입니다.
                사용자의 근무 스케줄(D/E/N/OFF)과 선택한 웰니스 카테고리, 사전질문 응답을 바탕으로
                루틴 제안을 JSON으로만 반환하세요. 다른 설명 텍스트는 포함하지 마세요.""";

    String userPrompt = """
                근무 스케줄: %s
                선택 카테고리: %s
                사전질문 응답: %s

                다음 JSON 스키마로만 응답하세요:
                {
                  "weeklyBriefing": "string (한 문장 요약)",
                  "groups": [
                    {
                      "situation": "string (예: 출근 전 / 퇴근 후 수면 골든타임 / 근무 중)",
                      "count": number,
                      "routines": [
                        { "routineName": "string", "cycle": "string (예: 매일)", "suggestedTime": "string (예: 취침 1시간 전)" }
                      ]
                    }
                  ]
                }""".formatted(schedule.toString(), String.join(", ", categories), preferences.toString());

    String rawText = callMessages(systemPrompt, userPrompt, 2000);
    return objectMapper.readTree(cleanJson(rawText));
  }

}