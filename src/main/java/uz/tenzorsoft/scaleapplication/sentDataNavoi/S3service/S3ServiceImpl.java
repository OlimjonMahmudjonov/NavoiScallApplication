package uz.tenzorsoft.scaleapplication.sentDataNavoi.S3service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final String BUCKET = "s3-tenzorsoft";
    private final String END_POINT = "https://s3.tenzorsoft.uz";
    private final String ACCESS_KEY = "s3_tenzorsoft_minio_user";
    private final String SECRET_KEY = "b7Qbuf8DpyAdZmlok2X3PSlD6n9jilSuhctZRd2b";

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(END_POINT)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();
        log.info("Minio client muvaffaqiyatli yaratildi");

        // Bucket mavjudligini tekshirish va yaratish
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
                log.info("Bucket yaratildi: {}", BUCKET);
            }
        } catch (Exception e) {
            log.warn("Bucket tekshirishda xato: {}", e.getMessage());
        }
    }

    @Override
    public String uploadFile(byte[] image) {
        try {
            String fileName = "cam_" + System.currentTimeMillis() + ".jpg";

            // Rasmni yuklash
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(fileName)
                            .stream(new ByteArrayInputStream(image), image.length, -1)
                            .contentType("image/jpeg")
                            .build()
            );

            // TOZA URL — hech qachon o‘chmaydi!
            String cleanUrl = END_POINT + "/" + BUCKET + "/" + fileName;
            log.info("Rasm yuklandi: {}", cleanUrl);
            return cleanUrl;

        } catch (Exception e) {
            log.error("S3 ga yuklashda xato: {}", e.getMessage(), e);
            throw new RuntimeException("Rasm S3 ga yuklanmadi: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(String fileName) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(fileName)
                        .build())) {
            byte[] bytes = stream.readAllBytes();
            log.info("Fayl yuklandi: {}", fileName);
            return bytes;
        } catch (Exception e) {
            log.error("Fayl yuklashda xato: {}", fileName, e);
            return new byte[0];
        }
    }
}