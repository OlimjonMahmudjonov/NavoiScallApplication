package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
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

    public AttachResponse saveToSystem(byte[] file) {
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
*/
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
}
