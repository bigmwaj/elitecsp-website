package ca.elitecsp.job.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSummaryDto {
    private String jobId;
    private String title;
    private String location;
    private String department;
    private String summary;
    private String postedDate;

    @Builder.Default
    private Map<String, String> attributes = new LinkedHashMap<>();
}
