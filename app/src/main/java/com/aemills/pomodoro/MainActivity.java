package com.aemills.pomodoro;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;
import android.content.res.ColorStateList;
import android.graphics.Color;

public class MainActivity extends AppCompatActivity {

    private long userFocusTime = 25 * 60; // seconds
    private long userBreakTime = 5 * 60;  // seconds
    private long userLongBreakTime = 30 * 60; // seconds
    private long timeRemaining;
    private Vibrator vibrator;
    private TextView timerTextView;
    private CountDownTimer countDownTimer;
    private ImageButton startButton;
    private ImageButton settingsButton;
    private ImageView buttonBackdrop;
    private ImageView pause;
    private ImageView bgAnimation;
    private View titleBar;
    private Spinner focusTimesMenu;
    private Spinner breakTimesMenu;
    private Spinner longBreakTimesMenu;
    private TextView captionFocus;
    private TextView captionBreak;
    private TextView captionLongBreak;
    private Ringtone alertSound;
    private boolean isPaused = true;
    private boolean isActive = false;
    private boolean isStarted = false;
    private boolean isInBreak = false;
    private boolean isVisible = false;
    private int intervalCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        Uri alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        alertSound = RingtoneManager.getRingtone(getApplicationContext(), alertUri);

        // set UI elements, initialize button/menu behaviors
        setContentView(R.layout.activity_main);
        timerTextView = findViewById(R.id.timer);
        startButton = findViewById(R.id.timerButton);
        settingsButton = findViewById(R.id.settingsButton);
        buttonBackdrop = findViewById(R.id.outerCircle);
        titleBar = findViewById(R.id.titleBar);
        pause = findViewById(R.id.pause);
        bgAnimation = findViewById(R.id.bgAnimation);

        focusTimesMenu = findViewById(R.id.focus_menu);
        breakTimesMenu = findViewById(R.id.break_menu);
        longBreakTimesMenu = findViewById(R.id.longbreak_menu);

        focusTimesMenu.setVisibility(View.INVISIBLE);
        breakTimesMenu.setVisibility(View.INVISIBLE);
        longBreakTimesMenu.setVisibility(View.INVISIBLE);

        captionFocus = findViewById(R.id.caption_focus);
        captionBreak = findViewById(R.id.caption_break);
        captionLongBreak = findViewById(R.id.caption_longbreak);
        captionFocus.setVisibility(View.INVISIBLE);
        captionBreak.setVisibility(View.INVISIBLE);
        captionLongBreak.setVisibility(View.INVISIBLE);
        pause.setVisibility(View.INVISIBLE);

        timerTextView.setText("▶");
        startButton.setOnClickListener(v -> playPauseTimer());
        settingsButton.setOnClickListener(v -> showSettings());

        createDropdown();
        focusTimesMenu.setSelection(4);
        breakTimesMenu.setSelection(4);
        longBreakTimesMenu.setSelection(5);

        bgAnimation.animate().scaleX(0.5f).scaleY(0.5f).alpha(1).setDuration(0).start();

        startButton.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(150).start();
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if (!isPaused) {
                            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(200).start();
                        }
                        else {
                            v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(200).start();
                        }
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }


    private void createDropdown() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.focus_times,
                R.layout.spinner_layout
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        focusTimesMenu.setAdapter(adapter);
        focusTimesMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                userFocusTime = (long) Integer.parseInt(selected) * 60;
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // NOTHING
            }
        });

        adapter = ArrayAdapter.createFromResource(
                this,
                R.array.break_times,
                R.layout.spinner_layout
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        breakTimesMenu.setAdapter(adapter);
        breakTimesMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                userBreakTime = (long) Integer.parseInt(selected) * 60;
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // NOTHING
            }
        });

        adapter = ArrayAdapter.createFromResource(
                this,
                R.array.longbreak_times,
                R.layout.spinner_layout
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        longBreakTimesMenu.setAdapter(adapter);
        longBreakTimesMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                userLongBreakTime = (long) Integer.parseInt(selected) * 60;
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // NOTHING
            }
        });
    }

    private void showSettings() {
        isVisible = !isVisible;
        int viewType;

        if (isVisible) {
            viewType = View.VISIBLE;
            settingsButton.animate().rotation(60).setDuration(300).start();
        }
        else {
            viewType = View.INVISIBLE;
            settingsButton.animate().rotation(0).setDuration(300).start();
        }

        focusTimesMenu.setVisibility    (viewType);
        breakTimesMenu.setVisibility    (viewType);
        longBreakTimesMenu.setVisibility(viewType);
        captionFocus.setVisibility      (viewType);
        captionBreak.setVisibility      (viewType);
        captionLongBreak.setVisibility  (viewType);
    }

    private void playAlertSound() {
        if (alertSound != null) {
            alertSound.play();
        }
    }

    public void colorTransition(View view, int startColor, int endColor, boolean isImage) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimation.setDuration(1000); // 1 second (1000 milliseconds)

        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatedColor = (int) animator.getAnimatedValue();
                ColorStateList colorStateList = ColorStateList.valueOf(animatedColor);
                if (isImage) {
                    ImageView imageView = (ImageView) view;
                    imageView.setImageTintList(colorStateList);
                }
                else {
                    view.setBackgroundTintList(colorStateList);
                }
            }
        });

        colorAnimation.start();
    }

    private void playPauseTimer() {
        isPaused = !isPaused;
        if (!isActive) {
            pause.setVisibility(View.INVISIBLE);
            countdown(isStarted);
        }
        else {
            pause.setVisibility(View.VISIBLE);
            countDownTimer.cancel();
        }
        isActive = !isActive;
        isStarted = true;

        alertSound.stop();

        vibrator.vibrate(50);
    }

    private void setTimerText(long millisecondsRemaining) {
        long seconds = (millisecondsRemaining / 1000) % 60;
        long minutes = (millisecondsRemaining / 1000) / 60;
        timerTextView.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
    }

    private void countdown(boolean isUnpaused) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        long breakTime = (intervalCount % 4 == 3) ? userLongBreakTime : userBreakTime;
        long timerLength = (isInBreak ? breakTime : userFocusTime)*1000;
        if (isUnpaused) {
            timerLength = timeRemaining;
        }
        countDownTimer = new CountDownTimer(timerLength, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                setTimerText(millisUntilFinished);
                timeRemaining = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                playAlertSound();

                // animate change to UI colors
                isInBreak = !isInBreak;
                if (isInBreak) {
                    intervalCount += 1;
                    colorTransition(startButton, (Color.parseColor("#FFF97D25")),
                            (Color.parseColor("#FF208FD8")), true);
                    colorTransition(buttonBackdrop, (Color.parseColor("#FFFF953F")),
                            (Color.parseColor("#FF39BAE5")), true);
                    colorTransition(titleBar, (Color.parseColor("#FFF97D25")),
                            (Color.parseColor("#FF208FD8")), false);
                }
                else {
                    colorTransition(startButton, (Color.parseColor("#FF208FD8")),
                            (Color.parseColor("#FFF97D25")), true);
                    colorTransition(buttonBackdrop, (Color.parseColor("#FF39BAE5")),
                            (Color.parseColor("#FFFF953F")), true);
                    colorTransition(titleBar, (Color.parseColor("#FF208FD8")),
                            (Color.parseColor("#FFF97D25")), false);
                }

                vibrator.vibrate(200);

                // set timer to an inactive state
                isStarted = false;
                isActive = false;
                isPaused = true;

                // if there have been 3 breaks, do an extra long one
                long breakTime = (intervalCount % 4 == 3) ? userLongBreakTime : userBreakTime;
                long timerLength = (isInBreak ? breakTime : userFocusTime)*1000;

                setTimerText(timerLength);

                // trigger timer-end animation
                bgAnimation.animate().scaleX(1.0f).scaleY(1.0f)
                        .alpha(0).setDuration(500).start();
                bgAnimation.animate().scaleX(0.5f).scaleY(0.5f)
                        .alpha(1).setDuration(0).start();
            }
        };

        countDownTimer.start();
    }
}