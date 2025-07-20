package com.dapanda.chat.repository;

import java.util.Optional;

public interface ChatRoomRepositoryCustom {

	Optional<Long> findExistingChatRoomIdOnProduct(Long productId, Long sellerId, Long buyerId);
}
