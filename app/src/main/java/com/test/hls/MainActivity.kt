package com.test.hls

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.test.hls.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)


        val uri = Uri.parse("https://p0-t.shmjkj.top/data/course/error/0.m3u8")
//        val uri = Uri.parse("https://p0-t.shmjkj.top/data/course/right/0.m3u8")

        val baseDataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
            .setUserAgent("userAgent")
        val customDataSourceFactory: DataSource.Factory =
            CustomDataSourceFactory(baseDataSourceFactory)

        val mediaSource: HlsMediaSource = HlsMediaSource.Factory(customDataSourceFactory)
            .createMediaSource(uri)

        val player = SimpleExoPlayer.Builder(this).build()

        binding.exoplayer.player = player

        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }

}