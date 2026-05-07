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
public class ParsedJobWorkbook {

    @Builder.Default
    private List<JobSummaryDto> jobs = new ArrayList<>();

    @Builder.Default
    private Map<String, JobSummaryDto> jobsById = new LinkedHashMap<>();

    @Builder.Default
    private Map<String, JobDetailsDto> detailsById = new LinkedHashMap<>();
}
