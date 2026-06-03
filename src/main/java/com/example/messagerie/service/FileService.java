package com.example.messagerie.service;

import com.example.messagerie.config.StorageProperties;
import com.example.messagerie.model.*;
import com.example.messagerie.repository.FileAttachmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private static final Logger activityLog = LoggerFactory.getLogger("file.activity");

    private final FileAttachmentRepository fileRepo;
    private final NotificationService notificationService;
    private final StorageProperties storageProperties;

    public FileService(FileAttachmentRepository fileRepo, NotificationService notificationService,
                       StorageProperties storageProperties) {
        this.fileRepo = fileRepo;
        this.notificationService = notificationService;
        this.storageProperties = storageProperties;
    }

    private Path ensureRoot() throws IOException {
        Path root = Paths.get(storageProperties.getRootDir());
        Files.createDirectories(root);
        return root;
    }

    @Transactional
    public FileAttachment upload(Conversation conv, User sender, User recipient, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Fichier manquant");
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String stored = UUID.randomUUID() + "_" + original;
        Path dest = ensureRoot().resolve(stored);
        Files.copy(file.getInputStream(), dest);

        FileAttachment att = new FileAttachment();
        att.setConversation(conv);
        att.setSender(sender);
        att.setOriginalName(original);
        att.setStoredFilename(stored);
        att.setSize(file.getSize());
        FileAttachment saved = fileRepo.save(att);

        activityLog.info("USER: {} (id={}) | CONV: #{} | ACTION: fichier envoyé", sender.getName(), sender.getId(), conv.getId());
        notificationService.notify(recipient, NotificationType.FILE_RECEIVED, saved.getId(), "Nouveau fichier: " + original);
        return saved;
    }

    @Transactional
    public FileAttachment delete(FileAttachment att, User recipient) {
        att.setDeleted(true);
        FileAttachment saved = fileRepo.save(att);
        activityLog.info("USER: {} (id={}) | FILE: #{} | ACTION: fichier supprimé", att.getSender().getName(), att.getSender().getId(), saved.getId());
        notificationService.notify(recipient, NotificationType.FILE_DELETED, saved.getId(), "Fichier supprimé");
        return saved;
    }

    @Transactional(readOnly = true)
    public List<FileAttachment> list(Conversation conversation) {
        return fileRepo.findByConversationOrderByCreatedAtAsc(conversation);
    }

    public byte[] download(FileAttachment att) throws IOException {
        if (att.isDeleted()) throw new IllegalArgumentException("Ce fichier a été supprimé");
        Path file = ensureRoot().resolve(att.getStoredFilename());
        if (!Files.exists(file)) throw new IllegalArgumentException("Fichier introuvable sur le serveur");
        return Files.readAllBytes(file);
    }
}
