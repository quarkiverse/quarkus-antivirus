package io.quarkiverse.antivirus.runtime;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.inject.Inject;
import jakarta.xml.bind.DatatypeConverter;

import org.jboss.logging.Logger;

import io.vertx.core.json.JsonObject;

/**
 * {@link AntivirusEngine} implementation for VirusTotal API.
 * <p>
 * Find more info about VirusTotal on <a href="https://developers.virustotal.com/reference">VirusTotal Public API v3.0</a>.
 */
public class VirusTotalEngine implements AntivirusEngine {

    private static final Logger LOG = Logger.getLogger(VirusTotalEngine.class);

    @Inject
    VirusTotalRuntimeConfig config;

    @Override
    public boolean isEnabled() {
        return config.enabled() && config.key().isPresent();
    }

    @Override
    public void scan(String filename, InputStream inputStream) {
        if (!config.enabled()) {
            LOG.debug("VirusTotal scanner is currently disabled!");
            return;
        }
        LOG.infof("Starting the virus scan for file: %s", filename);
        try {
            URLConnection connection = openConnection(filename, inputStream);
            try (InputStream response = connection.getInputStream()) {
                JsonObject json = new JsonObject(convertInputStreamToString(response));
                handleBodyResponse(filename, json);
            }
        } catch (IOException ex) {
            LOG.warn("Cannot perform virus scan");
            throw new AntivirusException(filename, "Cannot perform virus scan", ex);
        }
    }

    protected void handleBodyResponse(String filename, JsonObject json) {
        JsonObject data = json.getJsonObject("data");
        if (data == null) {
            return;
        }
        JsonObject attributes = data.getJsonObject("attributes");
        if (attributes == null) {
            return;
        }
        JsonObject totalVotes = attributes.getJsonObject("total_votes");
        if (totalVotes == null) {
            return;
        }
        int votes = totalVotes.getInteger("malicious", 0);
        if (votes >= config.minimumVotes()) {
            String name = attributes.getString("meaningful_name");
            LOG.debugf(String.format("Retrieved %s meaningful name.", name));
            if (name != null && !name.isEmpty()) {
                final String error = String.format("Scan detected virus '%s' in file '%s'!", name, filename);
                throw new AntivirusException(filename, error);
            }
        }
    }

    protected URLConnection openConnection(String filename, InputStream inputStream) throws IOException {
        HttpURLConnection connection;
        try {
            String key = config.key().orElseThrow(RuntimeException::new);
            String hash = md5Hex(filename, convertInputStreamToByteArray(inputStream));
            LOG.debugf("File Hash = %s", hash);
            URL url = new URL(String.format(config.url(), hash));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("x-apikey", key);
            connection.setRequestMethod("GET");
            connection.connect();
        } catch (IOException e) {
            throw new AntivirusException(filename, e.getMessage(), e);
        }

        int code = connection.getResponseCode();
        switch (code) {
            case 200:
                // OK
                break;
            case 204:
                throw new AntivirusException(filename,
                        "Virus Total Request rate limit exceeded. You are making more requests than allowed. "
                                + "You have exceeded one of your quotas (minute, daily or monthly). Daily quotas are reset every day at 00:00 UTC.");
            case 400:
                throw new AntivirusException(filename, "Bad request. Your request was somehow incorrect. "
                        + "This can be caused by missing arguments or arguments with wrong values.");
            case 403:
                throw new AntivirusException(filename, "Forbidden. You don't have enough privileges to make the request. "
                        + "You may be doing a request without providing an API key or you may be making a request "
                        + "to a Private API without having the appropriate privileges.");
            case 404:
                if (config.allowUnknown()) {
                    LOG.infof("'%s' has never been scanned by VirusTotal before.", filename);
                } else {
                    throw new AntivirusException(filename, "Not Found. This file has never been scanned by VirusTotal before.");
                }
                break;
            default:
                throw new AntivirusException(filename, "Unexpected HTTP code " + code + " calling Virus Total web service.");
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