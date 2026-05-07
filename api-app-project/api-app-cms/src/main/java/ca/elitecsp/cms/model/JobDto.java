package ca.elitecsp.cms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "jobs")
public class JobDto {

    @JacksonXmlProperty(localName = "jobId")
    private String id;

    private JobCategory category;

    private JobType type;

    private String icon;

    private LocalDate expirationDate;

    @JacksonXmlProperty(localName = "location")
    private String location;

    @JacksonXmlProperty(localName = "title_fr")
    private String title_fr;

    @JacksonXmlProperty(localName = "title_en")
    private String title_en;

    @JacksonXmlProperty(localName = "description_fr")
    private String description_fr;

    @JacksonXmlProperty(localName = "description_en")
    private String description_en;
}
