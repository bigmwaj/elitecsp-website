package ca.elitecsp.job.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDetailsDto {
    private String jobId;
    private String title;
    private String location;
    private String department;
    private String summary;
    private String postedDate;
    private String description;

    @Builder.Default
    private List<String> responsibilities = new ArrayList<>();

    @Builder.Default
    private List<String> requirements = new ArrayList<>();

    @Builder.Default
    private List<String> benefits = new ArrayList<>();

    @Builder.Default
    private Map<String, String> attributes = new LinkedHashMap<>();
}
