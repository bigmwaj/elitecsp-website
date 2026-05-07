package ca.elitecsp.job.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDetailDto {
    private String jobId;
    private String title;
    private String location;
    private String department;
    private String summary;
    private String postedDate;
    private String description;
    private List<String> responsibilities;
    private List<String> requirements;
    private List<String> benefits;
}
