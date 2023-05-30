package com.test.hls;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class M3U8Downloader {

    private static final String M3U8_URL = "YOUR_M3U8_URL";



    private String downloadM3U8(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } finally {
            connection.disconnect();
        }

        return content.toString();
    }

    private String parseKeyUri(String m3u8Content) {
        String[] lines = m3u8Content.split("\n");
        for (String line : lines) {
            if (line.startsWith("#EXT-X-KEY")) {
                String[] keyParams = line.split(",");
                for (String param : keyParams) {
                    if (param.startsWith("URI")) {
                        return param.split("=")[1].replace("\"", "");
                    }
                }
            }
        }
        return null;
    }

    private byte[] requestKey(String keyUri) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(keyUri).openConnection();
        connection.setRequestMethod("GET");

        byte[] key = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            key = content.toString().getBytes();
        } finally {
            connection.disconnect();
        }

        return key;
    }


    private static class AesDecryptDataSourceFactory implements DataSource.Factory {
        private final byte[] encryptionKey;

        public AesDecryptDataSourceFactory(byte[] encryptionKey) {
            this.encryptionKey = encryptionKey;
        }

        @Override
        public DataSource createDataSource() {
            return new AesDecryptDataSource(encryptionKey);
        }
    }

    private static class AesDecryptDataSource implements DataSource {
        private final byte[] encryptionKey;
        private Cipher cipher;
        private InputStream inputStream;

        public AesDecryptDataSource(byte[] encryptionKey) {
            this.encryptionKey = encryptionKey;
        }

        @Override
        public void addTransferListener(TransferListener transferListener) {

        }

        @Override
        public long open(@NonNull DataSpec dataSpec) throws IOException {
            try {
                cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                SecretKeySpec secretKeySpec = new SecretKeySpec(encryptionKey, "AES");
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
                inputStream = new FileInputStream(dataSpec.uri.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return C.LENGTH_UNSET;
        }

        @Nullable
        @Override
        public Uri getUri() {
            return null;
        }

        @Override
        public Map<String, List<String>> getResponseHeaders() {
            return DataSource.super.getResponseHeaders();
        }

        @Override
        public int read(byte[] buffer, int offset, int readLength) throws IOException {
            if (inputStream == null) {
                return 0;
            }
            try {
                byte[] encryptedData = new byte[readLength];
                int bytesRead = inputStream.read(encryptedData);
                if (bytesRead == -1) {
                    return -1;
                }
                byte[] decryptedData = cipher.update(encryptedData, 0, bytesRead);
                System.arraycopy(decryptedData, 0, buffer, offset, decryptedData.length);
                return decryptedData.length;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        public void close() throws IOException {
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
        }
    }
}
