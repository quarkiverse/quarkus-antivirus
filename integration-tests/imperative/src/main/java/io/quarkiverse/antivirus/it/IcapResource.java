package io.quarkiverse.antivirus.it;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import io.quarkiverse.antivirus.runtime.AntivirusException;
import io.quarkiverse.antivirus.runtime.AntivirusScanResult;
import io.quarkiverse.antivirus.runtime.IcapEngine;
import lombok.extern.jbosslog.JBossLog;

@Path("/icap")
@Produces(MediaType.MEDIA_TYPE_WILDCARD)
@Consumes(MediaType.MEDIA_TYPE_WILDCARD)
@ApplicationScoped
@JBossLog
public class IcapResource {

    @Inject
    IcapEngine engine;

    @GET
    @Path("/valid")
    public String scanValid() {
        byte[] file = "\"Hello antivirus\"".getBytes(StandardCharsets.UTF_8);

        AntivirusScanResult result = engine.scan("valid.txt", new ByteArrayInputStream(file));
        if (result.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new WebApplicationException(result.getMessage(), result.getStatus());
        }
        return "File is valid!";
    }

    @GET
    @Path("/invalid")
    public String scanInvalid() {
        byte[] file = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes(StandardCharsets.UTF_8);
        AntivirusScanResult result = engine.scan("invalid.txt", new ByteArrayInputStream(file));
        if (result.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new WebApplicationException(result.getMessage(), result.getStatus());
        }
        return "File is invalid!";
    }

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload")
    public Response upload(@MultipartForm @Valid final UploadRequest fileUploadRequest) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final String fileName = fileUploadRequest.getFileName();
        final InputStream data = fileUploadRequest.getData();
        log.infof("Uploading document %s", fileUploadRequest);
        try {
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    IOUtils.toBufferedInputStream(data).readAllBytes());

            AntivirusScanResult result = engine.scan(fileName, inputStream);
            if (result.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new WebApplicationException(result.getMessage(), result.getStatus());
            }

            // write the file out to disk
            final File tempFile = File.createTempFile("fileName", "tmp");
            tempFile.deleteOnExit();
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            IOUtils.copy(inputStream, outputStream);
            log.infof("File '%s' is successfully uploaded.", fileName);
        } catch (AntivirusException | IOException e) {
            throw new BadRequestException(e);
        } finally {
            stopWatch.stop();
            log.infof("File '%s' processed in %s.", fileName, stopWatch.toString());
        }

        return Response.ok("Scanned successfully in " + stopWatch.toString()).status(Response.Status.CREATED).build();
    }
}
