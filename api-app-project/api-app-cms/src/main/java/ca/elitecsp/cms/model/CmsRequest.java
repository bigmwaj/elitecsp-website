package ca.elitecsp.cms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API Gateway request payload for the CMS Lambda.
 *
 * <p>The handler expects a JSON body such as:
 * <pre>{@code
 * {
 *   "bucketName": "my-bucket",
 *   "fileKey":    "jobs/jobs.xml"
 * }
 * }</pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsRequest {

    /** S3 bucket name that contains the XML file. */
    private String bucketName;

    /** S3 object key (path) of the XML file. */
    private String fileKey;
}
