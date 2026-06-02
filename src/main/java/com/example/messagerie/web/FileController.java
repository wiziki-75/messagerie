package com.example.messagerie.web;

import com.example.messagerie.model.Conversation;
import com.example.messagerie.model.FileAttachment;
import com.example.messagerie.model.User;
import com.example.messagerie.repository.ConversationRepository;
import com.example.messagerie.repository.FileAttachmentRepository;
import com.example.messagerie.service.FileService;
import com.example.messagerie.service.UserService;
import com.example.messagerie.web.dto.Dtos;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileService fileService;
    private final ConversationRepository conversationRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final UserService userService;

    public FileController(FileService fileService,
                          ConversationRepository conversationRepository,
                          FileAttachmentRepository fileAttachmentRepository,
                          UserService userService) {
        this.fileService = fileService;
        this.conversationRepository = conversationRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.userService = userService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Dtos.UploadFileResponse upload(@RequestParam Long conversationId,
                                          @RequestParam Long senderId,
                                          @RequestPart("file") MultipartFile file) throws IOException {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation introuvable"));
        User sender = userService.require(senderId);
        Long aId = conv.getUserA().getId();
        Long bId = conv.getUserB().getId();
        if (!senderId.equals(aId) && !senderId.equals(bId)) {
            throw new IllegalArgumentException("L'expéditeur n'appartient pas à la conversation");
        }
        User recipient = senderId.equals(aId) ? conv.getUserB() : conv.getUserA();
        FileAttachment saved = fileService.upload(conv, sender, recipient, file);
        return new Dtos.UploadFileResponse(saved.getId(), saved.getOriginalName(), saved.getSize());
    }

    @PostMapping("/delete")
    public FileAttachment delete(@RequestBody Dtos.DeleteFileRequest req) {
        FileAttachment att = fileAttachmentRepository.findById(req.fileId())
                .orElseThrow(() -> new IllegalArgumentException("Fichier introuvable"));
        Conversation conv = att.getConversation();
        User recipient = att.getSender().getId().equals(conv.getUserA().getId()) ? conv.getUserB() : conv.getUserA();
        return fileService.delete(att, recipient);
    }

    @GetMapping("/conversation/{conversationId}")
    public List<FileAttachment> list(@PathVariable Long conversationId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation introuvable"));
        return fileService.list(conv);
    }
}
