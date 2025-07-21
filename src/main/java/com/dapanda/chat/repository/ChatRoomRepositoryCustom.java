package com.dapanda.chat.repository;

import com.dapanda.chat.dto.request.ReadJoiningChatRoomRequest;
import com.dapanda.chat.dto.response.ReadJoiningChatRoomResponse;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepositoryCustom {

	Optional<Long> findExistingChatRoomIdOnProduct(Long productId, Long sellerId, Long buyerId);

	List<ReadJoiningChatRoomResponse> findJoiningChatRoom(ReadJoiningChatRoomRequest request);
}
