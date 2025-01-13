package org.lab.utils;

import io.minio.*;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.security.InvalidKeyException;

public class MinioUtils {
    public static final String BUCKET = "is-lab-3-bucket";

    public static MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint("http://127.0.0.1:9000")
                .credentials("xepXUXjo9QzHDoilV4iS", "VAV2DqctsEpzsylne24dPf783tykQpiq2os15KbV")
                .build();
    }

    public static void upload(MinioClient minioClient, String fileName, InputStream fileInputStream) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET)
                    .object(fileName)
                    .stream(fileInputStream, fileInputStream.available(), -1)
                    .contentType("application/json")
                    .build());
        } catch (InvalidKeyException e) {
            throw new WebApplicationException("Invalid credentials for MinIO: " + e.getMessage(), Response.Status.FORBIDDEN);
        } catch (Exception e) {
            throw new WebApplicationException("MinIO: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static void rollbackUpload(MinioClient minioClient, String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(BUCKET).object(fileName).build());
        } catch (InvalidKeyException e) {
            throw new WebApplicationException("Invalid credentials for MinIO: " + e.getMessage(), Response.Status.FORBIDDEN);
        } catch (Exception e) {
            throw new WebApplicationException("MinIO: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static InputStream download(MinioClient minioClient, String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(fileName)
                            .build());
        } catch (InvalidKeyException e) {
            throw new WebApplicationException("Invalid credentials for MinIO: " + e.getMessage(), Response.Status.FORBIDDEN);
        } catch (Exception e) {
            throw new WebApplicationException("MinIO: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static String appendId(String fileName, Integer actionId) {
        if (fileName.endsWith(".json"))
            fileName = fileName.substring(0, fileName.length() - 5);
        return fileName.concat("--").concat(actionId.toString()).concat(".json");
    }
}
