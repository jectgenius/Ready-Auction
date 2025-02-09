package com.readyauction.app.chat.service;


import com.readyauction.app.auction.dto.ProductDto;
import com.readyauction.app.auction.entity.Product;
import com.readyauction.app.auction.entity.Winner;
import com.readyauction.app.chat.dto.ChatProductDto;
import com.readyauction.app.chat.dto.ChatProfileDto;
import com.readyauction.app.chat.dto.ChatRoomDto;
import com.readyauction.app.chat.dto.MessageDto;
import com.readyauction.app.chat.entity.ChatMessage;
import com.readyauction.app.chat.entity.ChatRoom;
import com.readyauction.app.chat.repository.ChatMemberRepository;
import com.readyauction.app.chat.repository.ChatMessageRepository;
import com.readyauction.app.chat.repository.ChatProductRepository;
import com.readyauction.app.chat.repository.ChatRoomRepository;
import com.readyauction.app.user.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatProductRepository chatProductRepository;
    private final ChatMemberRepository chatMemberRepository;

    public void save(MessageDto messageDto) {
        ChatMessage chatMessage = messageDto.toChatMessageEntity();
        chatMessageRepository.save(chatMessage);
    }

    public MessageDto findAll() {
        return MessageDto.builder().build();
    }

    public List<ChatRoomDto> findChatRoomsByMemberId(Long memberId) {

        List<ChatRoom> chatRoomList = chatRoomRepository.findChatRoomsByMemberId(memberId);
        return chatRoomList.stream().map(ChatRoomDto::toChatRoomDto).toList();
    }

    public ChatRoomDto findChatRoomByProductId(Long productId) {
        ChatRoom chatRoom = chatRoomRepository.findChatRoomByProductId(productId);
        return ChatRoomDto.toChatRoomDto(chatRoom);
    }

    public List<MessageDto> findChatMessagesByChatRoomId(Long id) {
        List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessagesByChatRoomId(id);
        return chatMessageList.stream().map(MessageDto::toChatMessageDto).toList();
    }

    public ChatProductDto findProductById(Long productId) {
        Product product = chatProductRepository.findProductById(productId);
        return ChatProductDto.toChatProductDto(product);
    }

    public ChatProfileDto findMembers(Long sellerId, Long winnerId) {
        Member seller = chatMemberRepository.findMemberById(sellerId);
        Member winner = chatMemberRepository.findMemberById(winnerId);
        return ChatProfileDto.toChatProfileDto(seller, winner);
    }
}
