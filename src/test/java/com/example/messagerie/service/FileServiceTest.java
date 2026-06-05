package com.example.messagerie.service;

import com.example.messagerie.config.StorageProperties;
import com.example.messagerie.model.Conversation;
import com.example.messagerie.model.FileAttachment;
import com.example.messagerie.model.NotificationType;
import com.example.messagerie.model.User;
import com.example.messagerie.repository.FileAttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileAttachmentRepository fileRepo;
    @Mock
    private NotificationService notificationService;

    private StorageProperties storageProperties;

    @InjectMocks
    private FileService fileService;

    @TempDir
    Path tempDir;

    private User sender;
    private User recipient;
    private Conversation conv;

    @BeforeEach
    void setup() {
        storageProperties = new StorageProperties();
        storageProperties.setRootDir(tempDir.toString());
        // Recrée le service avec les props initialisées
        fileService = new FileService(fileRepo, notificationService, storageProperties);

        sender = new User();
        sender.setId(1L);
        sender.setName("Alice");

        recipient = new User();
        recipient.setId(2L);
        recipient.setName("Bob");

        conv = new Conversation();
        conv.setId(10L);
    }

    @Test
    void upload_success_persistsFile_andNotifies() throws IOException {
        byte[] content = "hello".getBytes();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("doc.txt");
        when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(content));
        when(file.getSize()).thenReturn((long) content.length);

        // capture de l'entité sauvegardée pour renseigner un id simulé
        when(fileRepo.save(any(FileAttachment.class))).thenAnswer(inv -> {
            FileAttachment a = inv.getArgument(0);
            a.setId(42L);
            return a;
        });

        FileAttachment saved = fileService.upload(conv, sender, recipient, file);

        assertThat(saved.getId()).isEqualTo(42L);
        assertThat(saved.getOriginalName()).isEqualTo("doc.txt");
        assertThat(saved.getStoredFilename()).isNotBlank();
        assertThat(saved.getSize()).isEqualTo(content.length);

        // Le fichier doit exister sur disque
        Path stored = tempDir.resolve(saved.getStoredFilename());
        assertThat(Files.exists(stored)).isTrue();
        assertThat(Files.readAllBytes(stored)).isEqualTo(content);

        // Notification envoyée au destinataire
        verify(notificationService).notify(recipient, NotificationType.FILE_RECEIVED, 42L, "Nouveau fichier: doc.txt");
        // Et la persistance appelée
        verify(fileRepo, atLeastOnce()).save(any(FileAttachment.class));
    }

    @Test
    void upload_fails_whenFileIsNullOrEmpty() {
        assertThatThrownBy(() -> fileService.upload(conv, sender, recipient, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fichier manquant");

        MultipartFile empty = mock(MultipartFile.class);
        when(empty.isEmpty()).thenReturn(true);
        assertThatThrownBy(() -> fileService.upload(conv, sender, recipient, empty))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fichier manquant");
    }

    @Test
    void delete_marksDeleted_andNotifies() {
        FileAttachment att = new FileAttachment();
        att.setId(7L);
        att.setSender(sender);

        when(fileRepo.save(any(FileAttachment.class))).thenAnswer(inv -> inv.getArgument(0));

        FileAttachment result = fileService.delete(att, recipient);

        assertThat(result.isDeleted()).isTrue();
        verify(fileRepo).save(att);
        verify(notificationService).notify(recipient, NotificationType.FILE_DELETED, 7L, "Fichier supprimé");
    }

    @Test
    void list_delegatesToRepository() {
        FileAttachment a = new FileAttachment();
        when(fileRepo.findByConversationOrderByCreatedAtAsc(conv)).thenReturn(List.of(a));

        List<FileAttachment> list = fileService.list(conv);
        assertThat(list).containsExactly(a);
        verify(fileRepo).findByConversationOrderByCreatedAtAsc(conv);
    }

    @Test
    void download_throwsIfDeleted() {
        FileAttachment att = new FileAttachment();
        att.setStoredFilename("x.bin");
        att.setDeleted(true);

        assertThatThrownBy(() -> fileService.download(att))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("supprimé");
    }

    @Test
    void download_throwsIfMissingOnDisk() {
        FileAttachment att = new FileAttachment();
        att.setStoredFilename("missing.bin");

        assertThatThrownBy(() -> fileService.download(att))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    void download_readsBytesFromDisk() throws IOException {
        byte[] data = new byte[] {1, 2, 3};
        String stored = "file123.bin";
        Files.write(tempDir.resolve(stored), data);

        FileAttachment att = new FileAttachment();
        att.setStoredFilename(stored);

        byte[] read = fileService.download(att);
        assertThat(read).isEqualTo(data);
    }
}
