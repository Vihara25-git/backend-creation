package com.sgic.defect_tracker.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AzureBlobStorageService {

    private final BlobServiceClient blobServiceClient;

    @Value("${azure.storage.container-name:def-16b}")
    private String containerName;

    public AzureBlobStorageService(
            @org.springframework.beans.factory.annotation.Autowired(required = false) BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    public String uploadImage(MultipartFile file) throws IOException {

        if (blobServiceClient == null) {
            throw new IllegalStateException("Azure Blob Storage is not configured. Please set AZURE_STORAGE_CONNECTION_STRING.");
        }

        BlobContainerClient containerClient =
                blobServiceClient.getBlobContainerClient(containerName);

        if (!containerClient.exists()) {
            containerClient.create();
        }

        String originalFileName = file.getOriginalFilename();

        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(
                    originalFileName.lastIndexOf(".")
            );
        }

        String blobName = UUID.randomUUID() + extension;

        BlobClient blobClient =
                containerClient.getBlobClient(blobName);

        BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentType(file.getContentType());

        blobClient.upload(file.getInputStream(), file.getSize(), true);
        blobClient.setHttpHeaders(headers);

        // The storage account has anonymous blob access disabled, so the bare
        // blob URL returns "PublicAccessNotPermitted" when the browser loads it
        // in an <img> tag. Append a read-only SAS token so the URL authorizes
        // itself. generateSas() signs with the account key from the connection
        // string, so nothing needs to be enabled on the account.
        BlobServiceSasSignatureValues sasValues =
                new BlobServiceSasSignatureValues(
                        OffsetDateTime.now().plusYears(5),
                        new BlobSasPermission().setReadPermission(true));

        return blobClient.getBlobUrl() + "?" + blobClient.generateSas(sasValues);
    }
}