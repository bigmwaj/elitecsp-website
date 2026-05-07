package ca.elitecsp.job.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("S3FileLoaderService")
class S3FileLoaderServiceTest {

    @Test
    @DisplayName("returns bytes when object exists")
    void loadExcelFile_success() {
        S3Client s3Client = mock(S3Client.class);
        byte[] expected = "excel-content".getBytes();
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), expected);

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

        S3FileLoaderService service = new S3FileLoaderService(s3Client, "bucket", "jobs.xlsx");
        byte[] actual = service.loadExcelFile();

        assertArrayEquals(expected, actual);
    }

    @Test
    @DisplayName("throws domain exception when key is missing")
    void loadExcelFile_missingKey_throwsS3FileLoadException() {
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(NoSuchKeyException.builder().message("missing").build());

        S3FileLoaderService service = new S3FileLoaderService(s3Client, "bucket", "missing.xlsx");

        S3FileLoadException ex = assertThrows(S3FileLoadException.class, service::loadExcelFile);
        assertTrue(ex.getMessage().contains("not found"));
    }
}
