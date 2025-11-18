package uz.tenzorsoft.scaleapplication.service;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.AttachmentDto;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.AttachmentResponse;
import uz.tenzorsoft.scaleapplication.repository.AttachRepository;
import uz.tenzorsoft.scaleapplication.sentDataNavoi.S3service.S3Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AttachService implements BaseService<AttachEntity, AttachResponse, Object> {

    private final AttachRepository attachRepository;
    private final TruckService truckService;
    private final TruckPhotoService truckPhotoService;
    private final LogService logService;

    private final S3Service s3Service;

    private static final Logger log = LoggerFactory.getLogger(AttachService.class);

    // Kamera rasmlari uchun: to'g'ridan S3 ga yuklash
    @Transactional(propagation =  Propagation.REQUIRES_NEW)
    public AttachResponse saveToSystem(byte[] file) {
        try {
            if (file == null || file.length == 0) {
                throw new IllegalArgumentException("File bo'sh yoki null");
            }

            // 1. S3 ga yuklash → toza URL
            String s3Url = s3Service.uploadFile(file);

            // 2. AttachEntity yaratish — faqat kerakli fieldlar!
            AttachEntity entity = new AttachEntity();
            entity.setFileName("cam_" + System.currentTimeMillis() + ".jpg");  // bitta nom yetarli
            entity.setPath(s3Url);                                        // S3 URL
            entity.setSize((long) file.length);
            entity.setContentType("image/jpeg");
            entity.setIsSentToCloud(false);

            // 3. DB ga saqlash
            AttachEntity saved = attachRepository.save(entity);

            // 4. Log
            log.info("Kamera rasmi S3 ga yuklandi → AttachID: {}, URL: {}", saved.getId(), s3Url);

            // 5. Response — ID ni qaytarish shart!
            AttachResponse response = new AttachResponse();
            response.setId(saved.getId());      // saveTruck uchun kerak!
            response.setPath(s3Url);            // agar kerak bo‘lsa

            return response;

        } catch (Exception e) {
            log.error("S3 yuklashda xato: {}", e.getMessage(), e);
            logService.save(new LogEntity(5L, Instances.truckNumber, "S3 upload error: " + e.getMessage()));
            throw new RuntimeException("S3 ga yuklanmadi: " + e.getMessage(), e);
        }
    }

    // Agar boshqa joydan MultipartFile kelsa (masalan, frontenddan) — hozircha ishlatilmaydi
    // public AttachResponse saveToSystem(MultipartFile file) { ... } — o'chirilgan

    public AttachEntity findById(Long attachId) {
        return attachRepository.findById(attachId).orElse(null);
    }

    public List<AttachmentResponse> getNotSentData() {
        List<AttachmentResponse> result = new ArrayList<>();
        List<AttachEntity> notSentData = attachRepository.findByIsSentToCloud(false);

        for (AttachEntity attach : notSentData) {
            TruckEntity truck = truckService.findByTruckPhoto(truckPhotoService.findByAttach(attach));
            byte[] imageBytes = getImageBytes(attach.getPath());
            if (imageBytes.length == 0) continue;

            AttachmentResponse response = new AttachmentResponse(
                    truck == null ? null : truck.getIdOnServer(),
                    attach.getOriginalName(),
                    attach.getSize(),
                    attach.getType(),
                    attach.getContentType(),
                    attach.getPath(),
                    null,
                    truckPhotoService.findAttachStatus(attach),
                    attach.getCreatedAt(),
                    imageBytes
            );
            response.setId(attach.getId());
            response.setIdOnServer(attach.getIdOnServer());
            result.add(response);
            break; // Faqat bitta rasm jo'natish uchun
        }
        return result;
    }

    public List<AttachmentDto> getNotSentDataNew() {
        List<AttachmentDto> result = new ArrayList<>();
        List<AttachEntity> notSentData = attachRepository.findByIsSentToCloud(false);

        for (AttachEntity attach : notSentData) {
            TruckEntity truck = truckService.findByTruckPhoto(truckPhotoService.findByAttach(attach));
            byte[] imageBytes = getImageBytes(attach.getPath());
            if (imageBytes.length == 0) continue;

            AttachmentDto response = AttachmentDto.builder()
                    .localId(attach.getId())
                    .originName(attach.getOriginalName())
                    .size(attach.getSize())
                    .type(attach.getType())
                    .contentType(attach.getContentType())
                    .attachStatus(truckPhotoService.findAttachStatus(attach))
                    .bytes(imageBytes)
                    .webViewServerId(truck == null ? null : truck.getIdOnServer())
                    .build();

            result.add(response);
        }
        return result;
    }

    // Faqat S3 dan o'qish
    private byte[] getImageBytes(String path) {
        if (path == null || !path.contains("s3.tenzorsoft.uz")) {
            log.warn("S3 URL emas: {}", path);
            return new byte[0];
        }
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        return s3Service.downloadFile(fileName);
    }

    public byte[] getBytesById(Long id) {
        AttachEntity attach = attachRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attach topilmadi: " + id));

        String path = attach.getPath();
        if (path == null || !path.contains("s3.tenzorsoft.uz")) {
            return new byte[0];
        }

        String fileName = path.substring(path.lastIndexOf("/") + 1);
        return s3Service.downloadFile(fileName);
    }

    public void dataSentNew(List<AttachmentDto> notSentData, Map<Long, Long> attachMap, boolean status) {
        if (attachMap == null || attachMap.isEmpty()) return;

        notSentData.forEach(attach -> {
            Long serverId = attachMap.get(attach.getLocalId());
            AttachEntity entity = attachRepository.findById(attach.getLocalId())
                    .orElseThrow(() -> new RuntimeException("Attach topilmadi: " + attach.getLocalId()));
            entity.setIsSentToCloud(status);
            entity.setIdOnServer(serverId);
            attachRepository.save(entity);
        });
    }

    @Override
    public AttachResponse entityToResponse(AttachEntity entity) {
        return new AttachResponse(
                entity.getId(),
                entity.getOriginalName(),
                entity.getFileName(),
                entity.getSize(),
                entity.getType(),
                entity.getContentType(),
                entity.getPath(),
                entity.getCreatedAt()
        );
    }

    @Override
    public AttachEntity requestToEntity(Object request) {
        return null;
    }

    // Test uchun: mahalliy fayl emas, S3 dan foydalaning
    public AttachResponse getTestingImages() {
        throw new RuntimeException("Test rejimida S3 dan foydalaning");
    }

    public AttachResponse getCameraImgTesting() {
        throw new RuntimeException("Test rejimida S3 dan foydalaning");
    }
}







/*
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.AttachmentDto;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.AttachmentResponse;
import uz.tenzorsoft.scaleapplication.repository.AttachRepository;
import uz.tenzorsoft.scaleapplication.sentDataNavoi.S3service.S3Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AttachService implements BaseService<AttachEntity, AttachResponse, Object> {
    private final AttachRepository attachRepository;
    private final TruckService truckService;
    private final TruckPhotoService truckPhotoService;
    private final LogService logService;

    @Autowired
    private S3Service s3Service;

    private static final Logger log = LoggerFactory.getLogger(AttachService.class);
    private final String attachUploadFolder = System.getProperty("user.dir") + "/uploads/";
    private final String projectDirectory = System.getProperty("user.dir") + "/";

    public AttachResponse saveToSystem(MultipartFile file) {
        try {
            String pathFolder = getYmDString(); // 2022/04/23
            File folder = new File(attachUploadFolder + pathFolder); // attaches/2022/04/23

            if (!folder.exists()) folder.mkdirs();

            String fileName = UUID.randomUUID().toString(); // dasdasd-dasdasda-asdasda-asdasd
            String extension = getExtension(file.getOriginalFilename()); //zari.jpg

            // attaches/2022/04/23/dasdasd-dasdasda-asdasda-asdasd.jpg
            byte[] bytes = file.getBytes();
            Path path = Paths.get(attachUploadFolder + pathFolder + "/" + fileName + "." + extension);
            File f = Files.write(path, bytes).toFile();


            AttachEntity entity = new AttachEntity(
                    file.getOriginalFilename(), fileName, file.getSize(),
                    extension, file.getContentType(), path.toString()
            );
            attachRepository.save(entity);

            return entityToResponse(entity);

        } catch (IOException e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "00008: (" + getClass().getName() + ") " + e.getMessage()));
            throw new RuntimeException("File could not upload");
        }
    }

    /*public AttachResponse saveToSystem(byte[] file) {
        try {
            String pathFolder = getYmDString(); // 2022/04/23
            File folder = new File(attachUploadFolder + pathFolder); // attaches/2022/04/23

            if (!folder.exists()) folder.mkdirs();

            String fileName = UUID.randomUUID().toString(); // dasdasd-dasdasda-asdasda-asdasd

            Path path = Paths.get(attachUploadFolder + pathFolder + "/" + fileName + "." + "jpg");
            File f = Files.write(path, file).toFile();


            AttachEntity entity = new AttachEntity(
                    fileName, fileName, null,
                    "image/jpeg", "jpg", path.toString()
            );
            AttachEntity saved = attachRepository.save(entity);

            System.out.println("Attach: " + saved);

            return entityToResponse(entity);

        } catch (IOException e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "00009: (" + getClass().getName() + ") " + e.getMessage()));
            throw new RuntimeException("File could not upload");
        }
    }

*/
/*
    public AttachResponse saveToSystem(byte[] file) {
        try {
            // 1. S3 ga yuklash → URL qaytadi
            String s3Url = s3Service.uploadFile(file);

            // 2. AttachEntity yaratish
            AttachEntity entity = new AttachEntity();
            entity.setOriginalName("camera_" + System.currentTimeMillis() + ".jpg");
            entity.setFileName(UUID.randomUUID().toString() + ".jpg");
            entity.setSize((long) file.length);
            entity.setType("jpg");
            entity.setContentType("image/jpeg");
            entity.setPath(s3Url); // S3 URL saqlanadi
            entity.setIsSentToCloud(false);

            AttachEntity saved = attachRepository.save(entity);
            return entityToResponse(saved);

        } catch (Exception e) {
            log.error("S3 yuklashda xato: {}", e.getMessage());
            throw new RuntimeException("S3 ga yuklanmadi");
        }
    }

    public String getYmDString() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DATE);

        return year + "/" + month + "/" + day; // 2022/04/23
    }


    public String getExtension(String fileName) {
        if (fileName == null) {
            throw new RuntimeException("File name null");
        }
        int lastIndex = fileName.lastIndexOf(".");
        return fileName.substring(lastIndex + 1);
    }

    public AttachEntity findById(Long attachId) {
        return attachRepository.findById(attachId).orElse(null);
    }

    public List<AttachmentResponse> getNotSentData() {
        List<AttachmentResponse> result = new ArrayList<>();
        List<AttachEntity> notSentData = attachRepository.findByIsSentToCloud(false);

        if (notSentData.isEmpty()) return result;

        for (AttachEntity attach : notSentData) {
            TruckEntity truck = truckService.findByTruckPhoto(truckPhotoService.findByAttach(attach));
            byte[] imageBytes = getImageBytes(attach.getPath());
            if (imageBytes.length == 0) continue;
            AttachmentResponse response = new AttachmentResponse(
                    truck == null ? null : truck.getIdOnServer(), attach.getOriginalName(),
                    attach.getSize(), attach.getType(), attach.getContentType(), attach.getPath(), null,
                    truckPhotoService.findAttachStatus(attach), attach.getCreatedAt(), imageBytes
            );
            response.setId(attach.getId());
            response.setIdOnServer(attach.getIdOnServer());
            result.add(response);
            System.out.println("attach id >> " + attach.getId() + ". byte length >> " + imageBytes.length);
            break;
        }
        return result;
/*
        for (AttachEntity attach : notSentData) {

            TruckEntity truck = truckService.findByTruckPhoto(truckPhotoService.findByAttach(attach));

            AttachmentResponse response = new AttachmentResponse(
                    truck == null ? null : truck.getIdOnServer(), attach.getOriginalName(),
                    attach.getSize(), attach.getType(), attach.getContentType(), attach.getPath(), null,
                    truckPhotoService.findAttachStatus(attach), attach.getCreatedAt(), getImageBytes(attach.getPath())
            );
            response.setId(attach.getId());
            response.setIdOnServer(attach.getIdOnServer());
            result.add(response);

        }
        return result;
*//*
    }

    public List<AttachmentDto> getNotSentDataNew() {
        List<AttachmentDto> result = new ArrayList<>();
        List<AttachEntity> notSentData = attachRepository.findByIsSentToCloud(false);

        if (notSentData.isEmpty()) return result;

        for (AttachEntity attach : notSentData) {
            TruckEntity truck = truckService.findByTruckPhoto(truckPhotoService.findByAttach(attach));
            byte[] imageBytes = getImageBytes(attach.getPath());
            if (imageBytes.length == 0) continue;
            AttachmentDto response = AttachmentDto.builder()
                    .localId(attach.getId())
                    .originName(attach.getOriginalName())
                    .size(attach.getSize())
                    .type(attach.getType())
                    .contentType(attach.getContentType())
                    .attachStatus(truckPhotoService.findAttachStatus(attach))
                    .bytes(imageBytes)
                    .webViewServerId(truck == null ? null : truck.getIdOnServer())
                    .build();

            result.add(response);
            System.out.println("attach id >> " + attach.getId() + ". byte length >> " + imageBytes.length);
        }
        return result;
    }

    private byte[] getImageBytes(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            return new byte[]{};
        }
//
//        try {
//            BufferedImage image = ImageIO.read(new File(path));
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            ImageIO.write(image, "jpg", outputStream);
//            return outputStream.toByteArray();
//        } catch (IOException e) {
//            return new byte[]{};
//        }


    }

    public void dataSent(List<AttachmentResponse> notSentData, Map<Long, Long> attachMap) {
        if (attachMap == null || attachMap.isEmpty()) {
            return;
        }
        notSentData.forEach(attach -> {
            AttachEntity entity = attachRepository.findById(attach.getId()).orElseThrow(() -> new RuntimeException(attach.getId() + " is not found from database"));
            entity.setIsSentToCloud(true);
            entity.setIdOnServer(attachMap.get(entity.getId()));
            attachRepository.save(entity);
        });
    }

    public void dataSentNew(List<AttachmentDto> notSentData, Map<Long, Long> attachMap,boolean status) {
        if (attachMap == null || attachMap.isEmpty()) {
            return;
        }
        notSentData.forEach(attach -> {
            Long key = attachMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null && entry.getValue().equals(attach.getLocalId()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            AttachEntity entity = attachRepository.findById(attach.getLocalId()).orElseThrow(() -> new RuntimeException(attach.getLocalId() + " is not found from database"));
            entity.setIsSentToCloud(status);
            entity.setIdOnServer(key);
            attachRepository.save(entity);
        });
    }


    @Override
    public AttachResponse entityToResponse(AttachEntity entity) {
        return new AttachResponse(
                entity.getId(), entity.getOriginalName(), entity.getFileName(), entity.getSize(),
                entity.getType(), entity.getContentType(), entity.getPath(), entity.getCreatedAt()
        );
    }

    @Override
    public AttachEntity requestToEntity(Object request) {
        return null;
    }


    public AttachResponse getTestingImages() {
        File file = new File(projectDirectory + "src/main/resources/images/kirish-tepa.jpg");
        return entityToResponse(attachRepository.save(new AttachEntity(
                "kirish-tepa", "kirish-tepa", 1024L,
                "jpg", "image/jpeg", file.getAbsolutePath()
        )));
    }

    public AttachResponse getCameraImgTesting() {
        File file = new File(projectDirectory + "src/main/resources/images/kirish.jpg");
        return entityToResponse(attachRepository.save(new AttachEntity(
                "kirish", "kirish", 2048L,
                "jpg", "image/jpeg", file.getAbsolutePath()
        )));
    }

    public byte[] getBytesById(Long id) {
        AttachEntity attach = attachRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attach topilmadi: " + id));

        String path = attach.getPath();
        if (path == null || path.isEmpty()) {
            return new byte[0];
        }

        // Agar S3 URL bo'lsa – fayl nomini ajratib, yuklab olish
        if (path.contains("s3.tenzorsoft.uz")) {
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            return s3Service.downloadFile(fileName);
        }

        // Agar mahalliy fayl bo'lsa (eski usul)
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (Exception e) {
            log.error("Fayl o'qilmadi: {}", path);
            return new byte[0];
        }
    }
    /*public byte[] getBytesById(Long id ){
        AttachEntity entity =  attachRepository.findById(id).orElseThrow(() -> new RuntimeException(id + " is not found from database"));

        try {
            Path path = Paths.get(entity.getPath());
            if (!Files.exists(path)) {
                log.warn("Files not  found {}" , entity.getPath());
                return  new byte[0];
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
               log.warn("Fails reader something went  wrong {}- {} " , entity.getPath() , e.getMessage());
               return  new  byte[0];
        }
    }





}
*/