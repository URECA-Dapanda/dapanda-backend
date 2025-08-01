package com.dapanda.fcm_token.repository;

import com.dapanda.fcm_token.entity.NotificationEntity;
import com.dapanda.member.entity.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

	List<NotificationEntity> findByMemberOrderByCreatedAtDesc(Member member);

}
