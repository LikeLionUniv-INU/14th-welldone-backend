package com.likelion.welldone.repository;

import com.likelion.welldone.entity.LoungeMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoungeMessageRepository extends JpaRepository<LoungeMessage, java.util.UUID> {
  List<LoungeMessage> findTop50ByGroupTagOrderByCreatedAtDesc(String groupTag);
}