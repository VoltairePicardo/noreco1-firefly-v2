package com.noreco1.fireflyv2.common.facade;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class FTPClientPool {

    private static final Logger log = LoggerFactory.getLogger(FTPClientPool.class);
    private static final int MAX_POOL_SIZE = 5;

    private final LinkedBlockingQueue<FTPClient> pool = new LinkedBlockingQueue<>(MAX_POOL_SIZE);

    @Autowired
    private Environment env;

    public FTPClient borrow() throws IOException {
        FTPClient client = pool.poll();
        if (client != null) {
            if (isValid(client)) return client;
            silentDisconnect(client);
        }
        return newConnection();
    }

    public void returnToPool(FTPClient client) {
        if (client == null) return;
        if (isValid(client)) {
            if (!pool.offer(client)) silentDisconnect(client);
        } else {
            silentDisconnect(client);
        }
    }

    public boolean upload(InputStream inputStream, String filename) {
        FTPClient client = null;
        try {
            client = borrow();
            return client.storeFile(filename, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            returnToPool(client);
        }
    }

    public InputStream retrieve(String filename) {
        FTPClient client = null;
        try {
            client = borrow();
            try (InputStream ftpStream = client.retrieveFileStream(filename)) {
                if (ftpStream == null) return null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = ftpStream.read(buf)) != -1) baos.write(buf, 0, n);
                client.completePendingCommand();
                return new ByteArrayInputStream(baos.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            returnToPool(client);
        }
    }

    public boolean delete(String filename) {
        FTPClient client = null;
        try {
            client = borrow();
            return client.deleteFile(filename);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            returnToPool(client);
        }
    }

    private boolean isValid(FTPClient client) {
        try {
            return client.isConnected() && client.sendNoOp();
        } catch (IOException e) {
            return false;
        }
    }

    private FTPClient newConnection() throws IOException {
        FTPClient client = new FTPClient();
        client.connect(
            env.getProperty("ftp.server.address"),
            Integer.parseInt(env.getProperty("ftp.server.port"))
        );
        if (!client.login(
            env.getProperty("ftp.server.username"),
            env.getProperty("ftp.server.password")
        )) {
            client.disconnect();
            throw new IOException("FTP login failed");
        }
        client.enterLocalPassiveMode();
        client.setFileType(FTPClient.BINARY_FILE_TYPE);
        return client;
    }

    private void silentDisconnect(FTPClient client) {
        try {
            if (client.isConnected()) {
                client.logout();
                client.disconnect();
            }
        } catch (IOException ignored) {}
    }

    @PreDestroy
    public void shutdown() {
        FTPClient client;
        while ((client = pool.poll()) != null) silentDisconnect(client);
        log.info("FTP connection pool shut down.");
    }
}
