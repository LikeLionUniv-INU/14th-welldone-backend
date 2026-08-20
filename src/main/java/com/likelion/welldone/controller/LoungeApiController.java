package com.likelion.welldone.controller;

import com.likelion.welldone.common.ApiException;
import com.likelion.welldone.common.ApiResponse;
import com.likelion.welldone.entity.DutyPoints;
import com.likelion.welldone.entity.LoungeMessage;
import com.likelion.welldone.entity.Schedule;
import com.likelion.welldone.repository.DutyPointsRepository;
import com.likelion.welldone.repository.LoungeMessageRepository;
import com.likelion.welldone.repository.ScheduleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/lounge")
public class LoungeApiController {

  private final ScheduleRepository scheduleRepository;
  private final LoungeMessageRepository loungeMessageRepository;
  private final DutyPointsRepository dutyPointsRepository;

  public LoungeApiController(ScheduleRepository scheduleRepository,
                             LoungeMessageRepository loungeMessageRepository,
                             DutyPointsRepository dutyPointsRepository) {
    this.scheduleRepository = scheduleRepository;
    this.loungeMessageRepository = loungeMessageRepository;
    this.dutyPointsRepository = dutyPointsRepository;
  }

  private int myPoint() {
    return dutyPointsRepository.findById(1).map(DutyPoints::getBalance).orElse(0);
  }

  private String todayDutyType() {
    return scheduleRepository.findById(LocalDate.now()).map(Schedule::getDutyType).orElse("OFF");
  }

  private String nicknameForTag(String tag) {
    return switch (tag) {
      case "NIGHT" -> "익명의 나이트워커";
      case "DAY" -> "익명의 데이워커";
      case "EVENING" -> "익명의 이브닝워커";
      default -> "익명";
    };
  }

  private String relativeTime(Instant createdAt) {
    long minutes = Duration.between(createdAt, Instant.now()).toMinutes();
    if (minutes < 1) return "방금 전";
    if (minutes < 60) return minutes + "분 전";
    long hours = minutes / 60;
    return hours + "시간 전";
  }

  // ===== 28. 듀티 라운지 메인 조회 =====
  @GetMapping("/main")
  public ApiResponse<Map<String, Object>> getMain() {
    String duty = todayDutyType();

    if ("OFF".equals(duty)) {
      return ApiResponse.success(Map.of(
          "myPoint", myPoint(),
          "todayDutyType", "OFF",
          "group", new java.util.HashMap<>() {{ put("group", null); }}.get("group")
      ));
    }

    // TODO: 실제 팀원 평균 달성률/참여 인원 집계 쿼리로 교체
    Map<String, Object> group = Map.of(
        "groupName", "Team " + duty,
        "tag", duty,
        "achievementRate", 0,
        "remainingRate", 80,
        "participantCount", 0,
        "rewardCondition", "80% 달성시 +100P"
    );

    Map<String, Object> result = new java.util.HashMap<>();
    result.put("myPoint", myPoint());
    result.put("todayDutyType", duty);
    result.put("group", group);
    return ApiResponse.success(result);
  }

  // ===== 29. 실시간 듀티톡 목록 조회 =====
  @GetMapping("/talks")
  public ApiResponse<Map<String, Object>> getTalks() {
    String duty = todayDutyType();
    if ("OFF".equals(duty)) {
      return ApiResponse.success(Map.of("groupTag", "OFF", "talks", List.of()));
    }

    List<Map<String, Object>> talks = loungeMessageRepository.findTop50ByGroupTagOrderByCreatedAtDesc(duty).stream()
        .map(m -> Map.<String, Object>of(
            "talkId", m.getId(),
            "nickname", m.getNickname(),
            "message", m.getText(),
            "createdAt", relativeTime(m.getCreatedAt())
        ))
        .toList();

    return ApiResponse.success(Map.of("groupTag", duty, "talks", talks));
  }

  // ===== 30. 듀티톡 작성 =====
  public record TalkRequest(String message) {}

  @PostMapping("/talks")
  public ApiResponse<Map<String, Object>> postTalk(@RequestBody TalkRequest req) {
    if (req.message() == null || req.message().isBlank() || req.message().length() > 100) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "LOUNGE_400_1", "메시지는 최대 100자까지 입력 가능합니다.");
    }
    String duty = todayDutyType();

    LoungeMessage msg = new LoungeMessage();
    msg.setText(req.message());
    msg.setGroupTag(duty);
    msg.setNickname(nicknameForTag(duty));
    msg = loungeMessageRepository.save(msg);

    return ApiResponse.success("듀티톡이 등록되었습니다.", Map.of(
        "talkId", msg.getId(),
        "createdAt", "방금 전"
    ));
  }

  // ===== 31. 리워드 상품 목록 조회 =====
  // myPoint는 28번(듀티 라운지 메인) 응답값을 그대로 이어서 쓰므로 여기서는 내려주지 않음
  // TODO: 실제 리워드 DB 테이블로 교체 (현재는 고정 목록)
  @GetMapping("/rewards")
  public ApiResponse<Map<String, Object>> getRewards() {
    List<Map<String, Object>> rewards = List.of(
        Map.of("rewardId", 11, "name", "스타벅스 아메리카노 쿠폰", "requiredPoint", 1200,
            "imageUrl", "https://cdn.welldone.app/reward/11.png")
    );
    return ApiResponse.success(Map.of("rewards", rewards));
  }
}