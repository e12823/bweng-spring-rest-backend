package at.technikum.springrestbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private MinioFileStorageService minioFileStorageService;

    @InjectMocks
    private FileStorageService fileStorageService;

    @Test
    void storeFile_shouldThrow_whenFileIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileStorageService.storeFile(null));

        assertEquals("File is empty", ex.getMessage());
    }

    @Test
    void storeFile_shouldThrow_whenFileIsEmpty() {
        MultipartFile file = new MockMultipartFile("file", new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileStorageService.storeFile(file));

        assertEquals("File is empty", ex.getMessage());
    }

    @Test
    void storeFile_shouldThrow_whenContentTypeIsUnknown() {
        MultipartFile file = new MockMultipartFile("file", "f.bin", null, new byte[]{1, 2});

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileStorageService.storeFile(file));

        assertEquals("File type is unknown", ex.getMessage());
    }

    @Test
    void storeFile_shouldThrow_whenContentTypeUnsupported() {
        MultipartFile file = new MockMultipartFile("file", "f.txt", "text/plain", new byte[]{1});

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileStorageService.storeFile(file));

        assertEquals("Unsupported file type: text/plain", ex.getMessage());
    }

    @Test
    void storeFile_shouldDelegateToMinio_whenContentTypeAllowed() {
        MultipartFile file = new MockMultipartFile("file", "img.jpg", "image/jpeg", new byte[]{1, 2});
        when(minioFileStorageService.storeFile(file)).thenReturn("https://cdn/file.jpg");

        String url = fileStorageService.storeFile(file);

        assertEquals("https://cdn/file.jpg", url);
        verify(minioFileStorageService).storeFile(file);
    }
}
