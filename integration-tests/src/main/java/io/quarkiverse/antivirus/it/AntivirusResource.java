package io.quarkiverse.antivirus.it;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import io.quarkiverse.antivirus.runtime.Antivirus;
import io.quarkiverse.antivirus.runtime.AntivirusException;
import io.quarkiverse.antivirus.runtime.AntivirusScanResult;

@Path("/antivirus")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.MEDIA_TYPE_WILDCARD)
@ApplicationScoped
public class AntivirusResource {

    @Inject
    Antivirus antivirus;

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload")
    public Response upload(@MultipartForm @Valid final UploadRequest fileUploadRequest) {
        final String fileName = fileUploadRequest.getFileName();
        final InputStream data = fileUploadRequest.getData();
        try {
            // copy the stream to make it resettable
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    IOUtils.toBufferedInputStream(data).readAllBytes());

            // scan the file and check the results
            List<AntivirusScanResult> results = antivirus.scan(fileName, inputStream);
            for (AntivirusScanResult result : results) {
                if (result.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new WebApplicationException(result.getMessage(), result.getStatus());
                }
            }

            // reset the stream
            inputStream.reset();

            // write the file out to disk
            final File tempFile = File.createTempFile("fileName", "tmp");
            tempFile.deleteOnExit();
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (AntivirusException | IOException e) {
            throw new BadRequestException(e);
        }

        return Response.ok().build();
    }
}
