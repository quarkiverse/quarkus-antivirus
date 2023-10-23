package io.quarkiverse.antivirus.it;

import java.io.InputStream;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import lombok.Data;

@Data
public class UploadRequest {

    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    @NotNull
    private InputStream data;

    @FormParam("fileName")
    @PartType(MediaType.TEXT_PLAIN)
    @NotBlank
    private String fileName;

}