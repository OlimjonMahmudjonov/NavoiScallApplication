package uz.tenzorsoft.scaleapplication.sentDataNavoi.S3service;

public interface S3Service {
    String uploadFile(byte[] image);
    byte[] downloadFile(String fileName);
}