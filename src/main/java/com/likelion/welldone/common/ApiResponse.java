package com.likelion.welldone.common;

public record ApiResponse<T>(boolean isSuccess, String code, String message, T result) {
  public static <T> ApiResponse<T> success(String message, T result) {
    return new ApiResponse<>(true, "COMMON_200", message, result);
  }
  public static <T> ApiResponse<T> success(T result) {
    return success("요청에 성공했습니다.", result);
  }
  public static <T> ApiResponse<T> fail(String code, String message) {
    return new ApiResponse<>(false, code, message, null);
  }
}