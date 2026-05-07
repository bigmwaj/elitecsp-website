package ca.elitecsp.job.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobSummaryDto {
    private String jobId;
    private String title;
    private String location;
    private String department;
    private String summary;
    private String postedDate;
}
