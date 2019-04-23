package com.example.websocketdemo.controller;

import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.controller.WebSocketEventListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.example.websocketdemo.util.JsonUtil;

@Controller
public class ChatController {
	
	@Autowired
    private SimpMessageSendingOperations messagingTemplate;
	private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }
    
    @RequestMapping(value = "/chat.sendPicture", method = RequestMethod.POST)
    @ResponseBody
    public String uploadimageFile(MultipartHttpServletRequest request) {

    	String userName = request.getParameter("username");
    	MultipartFile imageFile = request.getFile("photo");
    	String predix = "/";
    	if (!imageFile.isEmpty()) {
            String imageName = userName + "_" + imageFile.getOriginalFilename();
            String path = request.getServletContext().getRealPath(predix) + imageName;
            File localImageFile = new File(path);
            try {
                imageFile.transferTo(localImageFile); //文件转存到服务器文件中
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(ChatMessage.MessageType.PIC);
                chatMessage.setSender(userName);
                chatMessage.setContent(request.getContextPath() + predix + imageName);
                messagingTemplate.convertAndSend("/topic/public", chatMessage);
                return path;
            } catch (IOException e) {
                logger.error("图片上传失败：" + e.getMessage());
                return "upload false";
            }
        }

    	return "error";
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

}
