package com.dapanda.chat.repository;

import com.dapanda.chat.dto.request.ReadChatMessageHistoryRequest;
import com.dapanda.chat.dto.response.SendChatMessageResponse;

import java.util.List;

public interface ChatMessageRepositoryCustom {

	List<SendChatMessageResponse> findChatMessageHistory(ReadChatMessageHistoryRequest request);
}
