package com.gestionprojet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for interacting with RustFS (Rust-based File Storage).
 * Provides file upload, download, and deletion capabilities via HTTP API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RustFsService {

    private final WebClient.Builder webClientBuilder;

    @Value("${rustfs.base-url:http://localhost:8081}")
    private String baseUrl;

    @Value("${rustfs.upload-path:/api/v1/upload}")
    private String uploadPath;

    @Value("${rustfs.download-path:/api/v1/download}")
    private String downloadPath;

    /**
     * Uploads a file to RustFS in the specified bucket.
     *
     * @param file the file to upload
     * @param bucket the bucket name to store the file in
     * @return the URL of the uploaded file
     * @throws IOException if file upload fails
     */
    public String uploadFile(MultipartFile file, String bucket) throws IOException {
        log.info("Uploading file to RustFS: {} ({} bytes)", file.getOriginalFilename(), file.getSize());

        WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();

        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        Map<String, Object> body = Map.of(
                "file", resource,
                "bucket", bucket
        );

        // Simple upload - returns file URL
        String fileUrl = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(uploadPath)
                        .queryParam("bucket", bucket)
                        .queryParam("filename", file.getOriginalFilename())
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", resource))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("File uploaded successfully: {}", fileUrl);
        return fileUrl;
    }

    /**
     * Uploads a file to RustFS in the default bucket.
     *
     * @param file the file to upload
     * @return the URL of the uploaded file
     * @throws IOException if file upload fails
     */
    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, "default");
    }

    /**
     * Deletes a file from RustFS by its URL.
     *
     * @param fileUrl the URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        log.info("Deleting file from RustFS: {}", fileUrl);

        WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();

        webClient.delete()
                .uri(fileUrl.replace(baseUrl, ""))
                .retrieve()
                .toBodilessEntity()
                .block();

        log.info("File deleted successfully: {}", fileUrl);
    }

    /**
     * Downloads a file from RustFS by its URL.
     *
     * @param fileUrl the URL of the file to download
     * @return the file content as byte array
     */
    public byte[] downloadFile(String fileUrl) {
        log.info("Downloading file from RustFS: {}", fileUrl);

        WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();

        return webClient.get()
                .uri(fileUrl.replace(baseUrl, ""))
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }

    /**
     * Constructs a download URL for a file by its ID.
     *
     * @param fileId the ID of the file
     * @return the complete download URL
     */
    public String getFileUrl(String fileId) {
        return baseUrl + downloadPath + "/" + fileId;
    }
}
