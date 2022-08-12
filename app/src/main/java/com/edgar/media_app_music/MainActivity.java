package com.edgar.media_app_music;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.edgar.media_app_music.model.song;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView tvNameSong, tvCurrentTimeSong, tvTotalTimeSong;
    private SeekBar sbProgressSong;
    private ImageButton ibSkipPrevious, ibNavigateBefore, ibPlayOrPause, ibStop, ibNavigateNext, ibSkipNext;
    private ImageView ivAvatarAuthor;

    private int POSITION_SONG;
    private String fileName = "SongStatus";
    private ArrayList<song> listSong;
    private MediaPlayer mediaPlayer;
    private SharedPreferences sharedPreferences;
    private Handler handler;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Init();
        CreateMapping();
        Setup();
    }

    private void Init() {
        listSong = this.getDataSong(); // Khởi tạo dữ liệu bài hát

        // Trong trường hợp chưa lưu lại vị trí bài hát trước đó (App lần đầu tiên khởi chạy)
        // => Khởi tạo vị trí bài hát mặc định (pos=0)
        if(GetPositionSong() == -1) {
            SetPositionSong(0);
        } else {
            POSITION_SONG = GetPositionSong();
        }

        // Khởi tạo Handler
        handler = new Handler();
        // Khởi tạo animation
        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.disc_rotate);
    }

    private void CreateMapping() {
        tvNameSong = (TextView) findViewById(R.id.tvNameSong);
        tvCurrentTimeSong = (TextView) findViewById(R.id.tvCurrentTimeSong);
        tvTotalTimeSong = (TextView) findViewById(R.id.tvTotalTimeSong);
        sbProgressSong = (SeekBar) findViewById(R.id.sbProgressSong);
        ibSkipPrevious = (ImageButton) findViewById(R.id.ibSkipPrevious);
        ibNavigateBefore = (ImageButton) findViewById(R.id.ibNavigateBefore);
        ibPlayOrPause = (ImageButton) findViewById(R.id.ibPlayOrPause);
        ibStop = (ImageButton) findViewById(R.id.ibStop);
        ibNavigateNext = (ImageButton) findViewById(R.id.ibNavigateNext);
        ibSkipNext = (ImageButton) findViewById(R.id.ibSkipNext);
        ivAvatarAuthor = (ImageView) findViewById(R.id.ivAvatarAuthor);
    }

    private void Setup() {
        // Khởi tạo bài hát vào trong media player
        InitSongInfo(POSITION_SONG);

        // Nút Play - Pause bài hát
        ibPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()) {
                    HandlePlayOrPause(true, R.drawable.ic_baseline_play_arrow_48); // dừng bài hát
                } else {
                    HandlePlayOrPause(false, R.drawable.ic_baseline_pause_48); // phát bài hát
                }
            }
        });

        // Nút Stop bài hát
        ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HandleStop();
            }
        });

        // Nút Next bài hát
        ibSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HandleNext();
            }
        });

        // Nút Preious bài hát
        ibSkipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HandlePervious();
            }
        });

        // Xét sự kiện cho seekbar
        sbProgressSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(sbProgressSong.getProgress());
            }
        });

        // Nút load 10s tiếp theo của bài hát
        ibNavigateNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
                // Update thông tin bài hát lên Seekbar và CurrentTime
                UpdateSong();
            }
        });

        // Nút load 10s trước đó của bài hát
        ibNavigateBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
                // Update thông tin bài hát lên Seekbar và CurrentTime
                UpdateSong();
            }
        });

        // Một hàm ẩn chạy liên tục check tiến độ bài hát của app
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update thông tin bài hát lên Seekbar và CurrentTime
                UpdateSong();

                // Kiểm tra khi bài hát đã kết thúc
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        HandleNext(); // Chuyển sang bài tiếp theo
                    }
                });

                Log.e("LOOP", "YES");

                // Loop vô tận
                handler.postDelayed(this, 100);
            }
        }, 0);
    }

    //============================================================================================//
    // Hàm lấy dữ liệu bài hát
    private ArrayList<song> getDataSong() {
        ArrayList<song> temp = new ArrayList<>();
        temp.add(new song("Bình yên những phút giây", R.raw.binh_yen_nhung_phut_giay));
        temp.add(new song("Chạy ngay đi", R.raw.chay_ngay_di));
        temp.add(new song("Chúng ta không thuộc về nhau", R.raw.chung_ta_khong_thuoc_ve_nhau));
        temp.add(new song("Hãy trao cho anh", R.raw.hay_trao_cho_anh));
        temp.add(new song("Lạc trôi", R.raw.lac_troi));
        temp.add(new song("Muộn rồi mà sao vẫn còn", R.raw.muon_roi_ma_sao_con));
        temp.add(new song("Nơi này có anh", R.raw.noi_nay_co_anh));
        temp.add(new song("Remember me", R.raw.remember_me));
        return temp;
    }

    // Hàm khởi tạo bài hát
    private void InitSongInfo(int pos) {
        mediaPlayer = MediaPlayer.create(MainActivity.this, listSong.get(pos).getFile());

        // In thông tin lên màn hình
        sbProgressSong.setMax(mediaPlayer.getDuration()); // load trạng thái thời gian bài hát
        tvNameSong.setText(listSong.get(pos).getName()); // load tên bài hát
        LoadTotalTime(); // load tổng thời gian bài hát
    }

    // Hàm lưu lại vị trí bài hát trước đó
    private void SetPositionSong(int pos) {
        sharedPreferences = this.getSharedPreferences(fileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("POS", pos);
        editor.apply();
    }

    // Hàm lấy lại vị trí bài hát trước đó
    private int GetPositionSong() {
        sharedPreferences = this.getSharedPreferences(fileName, MODE_PRIVATE);
        return sharedPreferences.getInt("POS", -1);
    }

    // Hàm load tổng thời gian bài hát
    private void LoadTotalTime() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        tvTotalTimeSong.setText(simpleDateFormat.format(mediaPlayer.getDuration()));
    }

    @SuppressLint("SimpleDateFormat")
    private void UpdateSong() {
        // In thông tin trạng thái bài hát
        tvCurrentTimeSong.setText(new SimpleDateFormat("mm:ss").format(mediaPlayer.getCurrentPosition()));
        sbProgressSong.setProgress(mediaPlayer.getCurrentPosition());
    }

    //============================================================================================//
    // Xử lí play - pause bài hát
    private void HandlePlayOrPause(boolean isPlaying, int DrawIcon) {
        if (isPlaying) {
            mediaPlayer.pause(); // đừng bài hát
            ivAvatarAuthor.clearAnimation();
        } else {
            mediaPlayer.start(); // phát bài hát
            ivAvatarAuthor.startAnimation(animation);
        }

        // In giá trị lên màn hình
        ibPlayOrPause.setImageResource(DrawIcon);
    }

    // Xử lí stop bài hát
    private void HandleStop() {
        // Load bài hát
        mediaPlayer.stop();
        mediaPlayer.release(); // giải phóng bài hát
        InitSongInfo(POSITION_SONG); // khởi tạo lại giá trị bài hát ban đầu

        // In giá trị lên màn hình
        Toast.makeText(this, "Đã dừng bài hát!", Toast.LENGTH_SHORT).show();
        ibPlayOrPause.setImageResource(R.drawable.ic_baseline_play_arrow_48);
        ivAvatarAuthor.clearAnimation();
    }

    // Xử lí giảm index bài hát
    private void HandlePervious() {
        mediaPlayer.stop(); // đừng bài hát hiện tại
        mediaPlayer.release(); // giải phóng bài hát
        POSITION_SONG--; // Giảm index bài hát trong danh sách
        if(POSITION_SONG < 0) {
            POSITION_SONG = listSong.size() - 1; // index về cuối
        }

        // Load bài hát
        SetPositionSong(POSITION_SONG);
        InitSongInfo(POSITION_SONG);
        mediaPlayer.start();

        // In giá trị lên màn hình
        ibPlayOrPause.setImageResource(R.drawable.ic_baseline_pause_48);
        ivAvatarAuthor.startAnimation(animation);
    }

    // Xử lí tăng index bài hát
    private void HandleNext() {
        mediaPlayer.stop(); // đừng bài hát hiện tại
        mediaPlayer.release(); // giải phóng bài hát
        POSITION_SONG++; // Tăng index bài hát trong danh sách
        if(POSITION_SONG > listSong.size() - 1) {
            POSITION_SONG = 0; // index lên đầu
        }

        // Load bài hát
        SetPositionSong(POSITION_SONG);
        InitSongInfo(POSITION_SONG);
        mediaPlayer.start();

        // In giá trị lên màn hình
        ibPlayOrPause.setImageResource(R.drawable.ic_baseline_pause_48);
        ivAvatarAuthor.startAnimation(animation);
    }
}