package ca.elitecsp.job.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class JobSummaryDto {
    private String jobId;
    private String icon;
    private String type;
    private String category;
    private String title;
    private String location;
    private String summary;
    private String postedDate;
    private String expirationDate;
}
