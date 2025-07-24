package com.dapanda.chat.repository;

import com.dapanda.chat.dto.request.ReadChatMessageHistoryRequest;
import com.dapanda.chat.dto.response.ReadChatMessageHistoryResponse;

import java.util.List;

public interface ChatMessageRepositoryCustom {

	List<ReadChatMessageHistoryResponse> findChatMessageHistory(ReadChatMessageHistoryRequest request);
}
