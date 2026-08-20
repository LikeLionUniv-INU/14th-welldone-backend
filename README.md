<div align="center">

# 🌙 Well - Done

**나의 근무 스케줄에 맞춰 웰니스 루틴을 설계하는 AI 서비스**

교대 근무자(간호사, 생산직, 서비스직 등)를 위한 맞춤형 웰니스 루틴 추천 & 회복 관리 서비스

</div>

---

## 서비스 소개

`Well Done`은 **불규칙한 교대 근무로 자기관리가 어려운 사람들**을 위한 웰니스 루틴 서비스입니다.

일반적인 건강관리 앱은 "매일 아침 7시 기상, 매일 저녁 9시 취침"처럼 고정된 하루를 전제로 하지만, 3교대 근무자에게는 통하지 않습니다. Well Done은 사용자의 **실제 근무 스케줄(Day/Evening/Night/Off)을 입력받아, AI가 그 스케줄에 맞춘 웰니스 루틴을 1주일 단위로 제안**합니다.

- 출근 전/퇴근 후/취침 전 등 **근무 컨텍스트별로 다른 루틴**을 추천
- 매달 자동으로 생성되는 **AI 웰니스 리포트**로 지난달을 돌아보고, 다음 달의 회복 포인트를 미리 안내
- 같은 근무조(Day/Evening/Night/Off)끼리 소통할 수 있는 **듀티 라운지**로 동료애와 동기부여 제공

---


## 기술 스택

### Backend
| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0 (spring-boot-starter-webmvc) |
| Build Tool | Gradle |
| JSON | Jackson 3.x (`tools.jackson`) |
| Database | PostgreSQL (Supabase, Session Pooler) |
| AI |Gemini API |

### Infra / DevOps
| 분류 | 기술 |
|---|---|
| Server | Gabia g-cloud (Ubuntu 22.04) |
| Reverse Proxy | Nginx |
| SSL | Let's Encrypt (Certbot) + sslip.io |
| Deploy | `nohup ./gradlew bootRun` |
| API 문서 | Notion |
| 협업 | GitHub, Notion, Postman |

---

## 아키텍처

```
[Client (Frontend, Vercel)]
        │  HTTPS
        ▼
[Nginx Reverse Proxy] ── SSL (Let's Encrypt via sslip.io)
        │
        ▼
[Spring Boot 4.0 (Gabia g-cloud, Ubuntu 22.04)]
        │
        ├── Supabase PostgreSQL (Session Pooler)
        └── Gemini API (AI 루틴 생성 / 월간 리포트 생성)
```

---

## 📋 API 구성 (총 32개)

| 도메인 | 설명 |
|---|---|
| Auth | 회원가입, 로그인 (마스터 계정 방식) |
| Onboarding | 스케줄 입력, 카테고리/취향 선택, AI 루틴 생성·제안·적용 |
| Home | 오늘의 루틴/일정, 웰니스 게이지, 루틴 시작·일시정지·완료·사진인증 |
| My | 주간 기록, 월간 AI 리포트(달성률/카테고리/골든타임/다음달 예측) |
| Lounge | 듀티 라운지 메인, 듀티톡 조회·작성, 리워드샵 |
| Settings | 스케줄표 갱신 요청 |

---

## 팀 구성

| Backend | jeon1105 |

| Backend | sujincyan |

---

## 실행 방법

```bash
git clone https://github.com/LikeLionUniv-INU/14th-welldone-backend.git
cd 14th-welldone-backend

# application-local.yaml에 DB/AI API 키 등 개인 설정 추가 (gitignore 처리됨)

./gradlew bootRun
```

---

## 📌 프로젝트 배경

해커톤이라는 시간 제약 속에서, **완성도보다 "핵심 가치를 명확히 전달하는 것"**에 집중했습니다.
- 로그인/회원가입은 마스터 계정 방식으로 단순화
- 실시간 기능(듀티톡)은 웹소켓 대신 REST 폴링으로 우선 구현 (추후 확장 가능하도록 설계)
- AI가 필요한 두 영역(루틴 생성, 월간 리포트)은 각각 특성에 맞게 **온디맨드 생성**과 **배치 생성** 방식을 구분해서 적용
