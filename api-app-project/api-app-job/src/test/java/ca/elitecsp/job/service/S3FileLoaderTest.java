package ca.elitecsp.job.service;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class S3FileLoaderTest {

    @Test
    void loadFile_success_returnsBytes() {
        S3Client client = mock(S3Client.class);
        S3FileLoader loader = new S3FileLoader(client);

        byte[] content = "excel-content".getBytes();
        ResponseBytes<GetObjectResponse> bytes = ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), content);
        when(client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(bytes);

        byte[] result = loader.loadFile("bucket", "jobs/jobs.xlsx");

        assertArrayEquals(content, result);
    }

    @Test
    void loadFile_failure_throwsS3DownloadException() {
        S3Client client = mock(S3Client.class);
        S3FileLoader loader = new S3FileLoader(client);

        when(client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(new RuntimeException("boom"));

        assertThrows(S3DownloadException.class, () -> loader.loadFile("bucket", "jobs/jobs.xlsx"));
    }
}
