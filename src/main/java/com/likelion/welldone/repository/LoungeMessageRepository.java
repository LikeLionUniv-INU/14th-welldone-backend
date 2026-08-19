package com.likelion.welldone.repository;

import com.likelion.welldone.entity.LoungeMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LoungeMessageRepository extends JpaRepository<LoungeMessage, UUID> {
  List<LoungeMessage> findTop100ByOrderByCreatedAtAsc();
  List<LoungeMessage> findByCreatedAtAfterOrderByCreatedAtAsc(Instant since);
}