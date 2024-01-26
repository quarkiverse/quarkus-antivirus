package io.quarkiverse.antivirus.it;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;

import io.quarkiverse.antivirus.runtime.Antivirus;
import io.quarkiverse.antivirus.runtime.AntivirusException;
import io.quarkiverse.antivirus.runtime.AntivirusScanResult;

@Path("/antivirus")
@ApplicationScoped
public class AntivirusResource {

    @Inject
    Antivirus antivirus;

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/upload")
    public List<AntivirusScanResult> upload(@Valid final UploadRequest fileUploadRequest) {
        final String fileName = fileUploadRequest.getFileName();
        final InputStream data = fileUploadRequest.getData();
        try {
            // copy the stream to make it resettable
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    IOUtils.toBufferedInputStream(data).readAllBytes());

            // scan the file and check the results
            return antivirus.scan(fileName, inputStream);
        } catch (AntivirusException | IOException e) {
            throw new BadRequestException(e);
        }
    }
}
