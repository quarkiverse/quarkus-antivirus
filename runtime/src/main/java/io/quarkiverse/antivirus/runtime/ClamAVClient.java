package io.quarkiverse.antivirus.runtime;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Simple client for ClamAV's clamd scanner. Provides straightforward input stream scanning. Support for basic input
 * stream scanning and PING command. Clamd protocol is explained here:
 * <a href="http://linux.die.net/man/8/clamd">ClamAV</a>
 *
 * @author <a href="https://github.com/solita/clamav-java">ClamAV Java</a>
 */
public class ClamAVClient {

    /**
     * Configuration
     */
    private final ClamAVRuntimeConfig config;

    public ClamAVClient(ClamAVRuntimeConfig config) {
        if (config.scanTimeout() < 0 || config.pingTimeout() < 0) {
            throw new IllegalArgumentException("Negative timeout value does not make sense.");
        }
        if (config.chunkSize() <= 0) {
            throw new IllegalArgumentException("Chunk size must be greater than zero.");
        }
        this.config = config;
    }

    /**
     * Get a socket to the current host and port. Partly implemented as a separate method for unit testing purposes.
     *
     * @param timeout the socket timeout
     * @return socket to host and port
     * @throws IOException if an I/O error occurs when creating the socket
     */
    protected Socket getSocket(int timeout) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(timeout);
        String host = config.host().orElseThrow();
        socket.connect(new InetSocketAddress(host, config.port()), timeout);
        return socket;
    }

    /**
     * Interpret the result from a ClamAV scan, and determine if the result means the data is clean.
     *
     * @param reply The reply from the server after scanning
     * @return true if no virus was found according to the clamd reply message
     */
    public static boolean isCleanReply(final byte[] reply) {
        final String r = new String(reply, StandardCharsets.US_ASCII);

        return r.contains("OK") && !r.contains("FOUND");
    }

    /**
     * byte conversion based on ASCII character set regardless of the current system locale.
     *
     * @param s the string to get as bytes
     * @return a byte[] array in ASCII charset
     */
    private static byte[] asBytes(final String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Reads all available bytes from the stream
     *
     * @param is the InputStream to read
     * @return the byte[] array output
     * @throws IOException if any error occurs reading stream
     */
    private static byte[] readAll(final InputStream is) throws IOException {
        final ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        final byte[] buf = new byte[2000];
        int read = is.read(buf);
        while (read > 0) {
            tmp.write(buf, 0, read);
            read = is.read(buf);
        }
        return tmp.toByteArray();
    }

    /**
     * Run PING command to CLAMD to test it is responding.
     *
     * @return true if the server responded with proper ping reply.
     * @throws IOException if there is an I/O problem communicating with CLAMD
     */
    public boolean ping() throws IOException {
        try (Socket s = getSocket(config.pingTimeout()); OutputStream os = s.getOutputStream()) {
            os.write(asBytes("zPING\0"));
            os.flush();
            try (InputStream is = s.getInputStream()) {
                byte[] buffer = new byte[4];
                int read = is.read(buffer);
                return read > 0 && Arrays.equals(buffer, asBytes("PONG"));
            }
        }
    }

    /**
     * Streams the given data to the server in chunks. The whole data is not kept in memory. This method is preferred if
     * you don't want to keep the data in memory, for instance by scanning a file on disk. Since the parameter
     * InputStream is not reset, you can not use the stream afterwards, as it will be left in an EOF-state. If your goal
     * is to scan some data, and then pass that data further, consider using {@link #scan(byte[]) scan(byte[] in)}.
     * <p>
     * Opens a socket and reads the reply. Parameter input stream is NOT closed.
     *
     * @param is data to scan. Not closed by this method!
     * @return server reply
     */
    public byte[] scan(final InputStream is) throws IOException {
        try (Socket s = getSocket(config.scanTimeout()); OutputStream outs = new BufferedOutputStream(s.getOutputStream())) {
            // handshake
            outs.write(asBytes("zINSTREAM\0"));
            outs.flush();
            final byte[] chunk = new byte[config.chunkSize()];

            // send data
            int readLen = is.read(chunk);
            while (readLen >= 0) {
                // The format of the chunk is: '<length><data>' where <length> is
                // the size of the following data in bytes expressed as a 4 byte
                // unsigned
                // integer in network byte order and <data> is the actual chunk.
                // Streaming is terminated by sending a zero-length chunk.
                final byte[] length = ByteBuffer.allocate(4).putInt(readLen).array();
                outs.write(length);
                outs.write(chunk, 0, readLen);
                readLen = is.read(chunk);
            }

            // terminate scan
            outs.write(new byte[] { 0, 0, 0, 0 });
            outs.flush();

            // read reply
            try (InputStream clamIs = s.getInputStream()) {
                return readAll(clamIs);
            }
        }
    }

    /**
     * Scans bytes for virus by passing the bytes to clamav
     *
     * @param in data to scan.
     * @return server reply
     */
    public byte[] scan(final byte[] in) throws IOException {
        return this.scan(new ByteArrayInputStream(in));
    }
}