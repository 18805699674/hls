package com.test.hls;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.ByteArrayDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CustomDataSource implements DataSource {

    private final DataSource baseDataSource;
    private final byte[] replacementKeyData;
    private final Uri uri;

    public CustomDataSource(DataSource baseDataSource, byte[] replacementKeyData, Uri uri) {
        this.baseDataSource = baseDataSource;
        this.uri = uri;
        this.replacementKeyData = replacementKeyData;
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        return baseDataSource.open(dataSpec);
    }

    @Nullable
    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        return baseDataSource.read(buffer, offset, readLength);
    }

    @Override
    public void close() throws IOException {
        baseDataSource.close();
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return baseDataSource.getResponseHeaders();
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {
        baseDataSource.addTransferListener(transferListener);
    }

    private boolean isKeyUri(Uri uri) {
        // 根据需要判断是否为密钥请求的 URI
        // 返回 true 表示是密钥请求，需要拦截和修改
        // 返回 false 表示不是密钥请求，继续正常处理
        return uri.getLastPathSegment().endsWith(".key");
    }

    private byte[] readOriginalKeyData(DataSpec dataSpec) throws IOException {
        // 执行请求并获取返回的数据
        DataSource dataSource = baseDataSource;
        try {
            dataSource.open(dataSpec);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = dataSource.read(buffer, 0, buffer.length)) != C.RESULT_END_OF_INPUT) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } finally {
            dataSource.close();
        }
    }

    private byte[] replaceKeyData(byte[] originalKeyData, byte[] replacementKeyData) {
        // 在这里执行替换操作，将原始的密钥数据内容替换为你需要的内容
        // 返回替换后的密钥数据
        byte[] replacedKeyData = new byte[originalKeyData.length];
        System.arraycopy(replacementKeyData, 0, replacedKeyData, 0, replacementKeyData.length);
        return replacedKeyData;
    }

    private ByteArrayDataSource createReplacedDataSource(byte[] replacedKeyData) {
        return new ByteArrayDataSource(replacedKeyData);
    }
}
