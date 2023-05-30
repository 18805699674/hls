package com.test.hls;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CustomDataSourceFactory implements DataSource.Factory {

    private final DataSource.Factory baseDataSourceFactory;

    public CustomDataSourceFactory(DataSource.Factory baseDataSourceFactory) {
        this.baseDataSourceFactory = baseDataSourceFactory;
    }

    @Override
    public DataSource createDataSource() {
        return new CustomDataSource();
    }

    private class CustomDataSource implements DataSource {

        private DataSource baseDataSource;

        @Override
        public void addTransferListener(TransferListener transferListener) {
//            baseDataSource.addTransferListener(transferListener);
        }

        @Override
        public long open(DataSpec dataSpec) throws IOException {
            baseDataSource = baseDataSourceFactory.createDataSource();
            return baseDataSource.open(dataSpec);
        }

        @Nullable
        @Override
        public Uri getUri() {
            return baseDataSource.getUri();
        }

        @Override
        public Map<String, List<String>> getResponseHeaders() {
            return DataSource.super.getResponseHeaders();
        }

        @Override
        public int read(byte[] buffer, int offset, int readLength) throws IOException {
            int bytesRead = baseDataSource.read(buffer, offset, readLength);
            if (bytesRead > 0) {
                // 对读取到的数据进行处理
                byte[] processedData = processBytes(Arrays.copyOfRange(buffer, offset, offset + bytesRead));
                System.arraycopy(processedData, 0, buffer, offset, processedData.length);
                bytesRead = processedData.length;
            }
            return bytesRead;
        }

        @Override
        public void close() throws IOException {
            baseDataSource.close();
        }

        private byte[] processBytes(byte[] data) {
            // 在这里对数据进行处理，可以是加密、解密、转换等操作
            // 替换为你的具体处理逻辑
            if("n25e78bd4l7we2tw".equals(new String(data))) {
                return "n15e87bc3l7we1tw".getBytes();
            }
            return data;
        }
    }
}
