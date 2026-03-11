package at.technikum.springrestbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/gif",
            "video/mp4"
    );

    private final MinioFileStorageService minioFileStorageService;

    public FileStorageService(MinioFileStorageService minioFileStorageService) {
        this.minioFileStorageService = minioFileStorageService;
    }

    public String storeFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();

        if (contentType == null) {
            throw new IllegalArgumentException("File type is unknown");
        }

        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }

        return minioFileStorageService.storeFile(file);
    }
}