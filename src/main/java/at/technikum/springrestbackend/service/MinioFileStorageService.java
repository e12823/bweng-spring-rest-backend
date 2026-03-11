package at.technikum.springrestbackend.service;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class MinioFileStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioFileStorageService(
            @Value("${bucket.host}") String host,
            @Value("${bucket.port}") int port,
            @Value("${bucket.access-key}") String accessKey,
            @Value("${bucket.access-secret}") String secretKey,
            @Value("${bucket.name}") String bucketName
    ) {
        this.bucketName = bucketName;

        this.minioClient = MinioClient.builder()
                .endpoint("http://" + host + ":" + port)
                .credentials(accessKey, secretKey)
                .build();

        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO bucket", e);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename() == null
                    ? "file"
                    : file.getOriginalFilename();

            String objectName = UUID.randomUUID() + "-" + originalFilename;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("File upload failed", e);
        }
    }
}