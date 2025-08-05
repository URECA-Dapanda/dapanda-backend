package com.dapanda.fcmToken.repository;

import com.dapanda.fcmToken.entity.NotificationEntity;
import com.dapanda.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

	List<NotificationEntity> findByMemberOrderByCreatedAtDesc(Member member);

}
