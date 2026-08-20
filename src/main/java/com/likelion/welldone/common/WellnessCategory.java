package com.likelion.welldone.common;

import java.util.LinkedHashMap;
import java.util.Map;

public class WellnessCategory {
  // 풀네임 -> [4글자 라벨, 2글자 라벨]
  public static final Map<String, String[]> MAP = new LinkedHashMap<>();
  static {
    MAP.put("신체적 건강", new String[]{"신체 건강", "신체"});
    MAP.put("피부 관리", new String[]{"피부 관리", "피부"});
    MAP.put("영양적 균형", new String[]{"영양 균형", "영양"});
    MAP.put("정서적 안정", new String[]{"정서 안정", "정서"});
    MAP.put("편안한 환경", new String[]{"편한 환경", "환경"});
    MAP.put("정신적 수양", new String[]{"정신 수양", "정신"});
  }

  public static String to4(String fullName) {
    String[] v = MAP.get(fullName);
    return v != null ? v[0] : fullName;
  }
  public static String to2(String fullName) {
    String[] v = MAP.get(fullName);
    return v != null ? v[1] : fullName;
  }
}