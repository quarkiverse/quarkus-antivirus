package io.quarkiverse.antivirus.runtime;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.bind.DatatypeConverter;

import io.vertx.core.json.JsonObject;
import lombok.extern.jbosslog.JBossLog;

/**
 * {@link AntivirusEngine} implementation for VirusTotal API.
 * <p>
 * Find more info about VirusTotal on <a href="https://developers.virustotal.com/reference">VirusTotal Public API v3.0</a>.
 */
@ApplicationScoped
@JBossLog
public class VirusTotalEngine implements AntivirusEngine {

    @Inject
    VirusTotalRuntimeConfig config;

    @Override
    public boolean isEnabled() {
        return config.enabled() && config.key().isPresent();
    }

    @Override
    public AntivirusScanResult scan(String filename, InputStream inputStream) {
        AntivirusScanResult.AntivirusScanResultBuilder result = AntivirusScanResult.builder().engine("VirusTotal")
                .fileName(filename);
        if (!config.enabled()) {
            return result.status(404).message("VirusTotal scanner is currently disabled!").build();
        }
        log.infof("Starting the virus scan for file: %s", filename);
        try {
            String message;
            HttpURLConnection connection = openConnection(filename, inputStream);
            int code = connection.getResponseCode();
            result.status(code);
            switch (code) {
                case 200:
                    result.message("OK");
                    break;
                case 204:
                    message = "Virus Total Request rate limit exceeded. You are making more requests than allowed. "
                            + "You have exceeded one of your quotas (minute, daily or monthly). Daily quotas are reset every day at 00:00 UTC.";
                    return result.message(message).build();
                case 400:
                    message = "Bad request. Your request was somehow incorrect. "
                            + "This can be caused by missing arguments or arguments with wrong values.";
                    return result.message(message).build();
                case 403:
                    message = "Forbidden. You don't have enough privileges to make the request. "
                            + "You may be doing a request without providing an API key or you may be making a request "
                            + "to a Private API without having the appropriate privileges.";
                    return result.message(message).build();
                case 404:
                    message = "Not Found. This file has never been scanned by VirusTotal before.";
                    return result.message(message).build();
                default:
                    message = "Unexpected HTTP code " + code + " calling Virus Total web service.";
                    return result.message(message).build();
            }

            try (InputStream response = connection.getInputStream()) {
                final String payload = convertInputStreamToString(response);
                result.payload(payload);
                final JsonObject json = new JsonObject(payload);
                return handleBodyResponse(filename, json, result);
            }
        } catch (IOException ex) {
            log.warn("Cannot perform virus scan");
            return result.status(400).message("Cannot perform virus scan").payload(ex.getMessage()).build();
        } catch (URISyntaxException ex) {
            log.error("Malformed URL for VirusTotal API");
            return result.status(500).message("Cannot perform virus scan").payload("Server configuration error.").build();
        }
    }

    private AntivirusScanResult handleBodyResponse(String filename, JsonObject json,
            AntivirusScanResult.AntivirusScanResultBuilder result) {
        JsonObject data = json.getJsonObject("data");
        if (data == null) {
            return result.status(200).build();
        }
        JsonObject attributes = data.getJsonObject("attributes");
        if (attributes == null) {
            return result.status(200).build();
        }
        JsonObject totalVotes = attributes.getJsonObject("total_votes");
        if (totalVotes == null) {
            return result.status(200).build();
        }
        int votes = totalVotes.getInteger("malicious", 0);
        if (votes >= config.minimumVotes()) {
            String name = attributes.getString("meaningful_name");
            log.debugf(String.format("Retrieved %s meaningful name.", name));
            if (name != null && !name.isEmpty()) {
                final String error = String.format("Scan detected virus '%s' in file '%s'!", name, filename);
                return result.status(400).message(error).build();
            }
        }
        return result.status(200).build();
    }

    protected HttpURLConnection openConnection(String filename, InputStream inputStream)
            throws IOException, URISyntaxException {
        HttpURLConnection connection;
        try {
            String key = config.key().orElseThrow(RuntimeException::new);
            String hash = md5Hex(filename, convertInputStreamToByteArray(inputStream));
            log.debugf("File Hash = %s", hash);
            URL url = new URI(String.format(config.url(), hash)).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("x-apikey", key);
            connection.setRequestMethod("GET");
            connection.connect();
        } catch (IOException e) {
            throw new AntivirusException(filename, e.getMessage(), e);
        }

        return connection;
    }

    protected String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        return stringBuilder.toString();
    }

    protected byte[] convertInputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        return byteArrayOutputStream.toByteArray();
    }

    protected String md5Hex(String filename, byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            return DatatypeConverter.printHexBinary(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new AntivirusException(filename, e.getMessage());
        }
    }
}