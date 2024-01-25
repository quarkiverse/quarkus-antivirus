package io.quarkiverse.antivirus.it;

import java.io.InputStream;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

import lombok.Data;

@Data
public class UploadRequest {

    @RestForm("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    @NotNull
    private InputStream data;

    @RestForm("fileName")
    @PartType(MediaType.TEXT_PLAIN)
    @NotBlank
    private String fileName;

}