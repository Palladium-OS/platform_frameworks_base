/*
 * Copyright (C) 2019 The Android Open Source Project
 * Copyright (C) 2021 Project 404
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.keyguard.clock;

import android.os.BatteryManager;
import android.app.WallpaperManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;


import androidx.core.graphics.ColorUtils;

import com.android.internal.colorextraction.ColorExtractor;
import com.android.systemui.R;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.plugins.ClockPlugin;

import java.lang.Math;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


import static com.android.systemui.statusbar.phone.KeyguardClockPositionAlgorithm.CLOCK_USE_DEFAULT_Y;

/**
 * Plugin for the default clock face used only to provide a preview.
 */
public class PalladiumClockController implements ClockPlugin {

    /**
     * Resources used to get title and thumbnail.
     */
    private final Resources mResources;

    /**
     * LayoutInflater used to inflate custom clock views.
     */
    private final LayoutInflater mLayoutInflater;

    /**
     * Extracts accent color from wallpaper.
     */
    private final SysuiColorExtractor mColorExtractor;

    /**
     * Renders preview from clock view.
     */
    private final ViewPreviewer mRenderer = new ViewPreviewer();

    /**
     * Helper to extract colors from wallpaper palette for clock face.
     */
    private final ClockPalette mPalette = new ClockPalette();

    /**
     * Root view of clock.
     */
    private ClockLayout mView;

    /**
     * Text clock for time, date, day and month
     */
    private TextClock mTimeHH;
    private TextClock mTimeMM;
    private TextClock mDate;
    private TextView mBatt;
    private ProgressBar progressC;

    /**
     * Time and calendars to check the date
     */
    private final Calendar mTimeCal = Calendar.getInstance(TimeZone.getDefault());
    private TimeZone mTimeZone;

    /**
     * Create a DefaultClockController instance.
     *
     * @param res            Resources contains title and thumbnail.
     * @param inflater       Inflater used to inflate custom clock views.
     * @param colorExtractor Extracts accent color from wallpaper.
     */
    public PalladiumClockController(Resources res, LayoutInflater inflater,
                              SysuiColorExtractor colorExtractor) {
        mResources = res;
        mLayoutInflater = inflater;
        mColorExtractor = colorExtractor;
    }

    private void createViews() {
        mView = (ClockLayout) mLayoutInflater
                .inflate(R.layout.palladium_clock, null);
        mTimeHH = mView.findViewById(R.id.clockTimeHHView);
        mTimeMM = mView.findViewById(R.id.clockTimeMMView);
        mDate = mView.findViewById(R.id.clockDateView);
        mBatt = mView.findViewById(R.id.battery);
        progressC=mView.findViewById(R.id.progressBar);
    }

    @Override
    public void onDestroyView() {
        mView = null;
        mTimeHH = null;
        mTimeMM = null;
        mDate = null;
        mBatt= null;
    }

    @Override
    public String getName() {
        return "palladium";
    }

    @Override
    public String getTitle() {
        return "Palladium";
    }

    @Override
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(mResources, R.drawable.palladium_thumbnail);
    }

    @Override
    public Bitmap getPreview(int width, int height) {
        setTextColor(Color.WHITE);
        View previewView = getView();

        // Initialize state of plugin before generating preview.
        ColorExtractor.GradientColors colors = mColorExtractor.getColors(
                WallpaperManager.FLAG_LOCK);
        setColorPalette(colors.supportsDarkText(), colors.getColorPalette());
        onTimeTick();

        return mRenderer.createPreview(previewView, width, height);
    }

    @Override
    public View getView() {
        if (mView == null) {
            createViews();
        }
        return mView;
    }

    @Override
    public View getBigClockView() {
        return null;
    }

    @Override
    public int getPreferredY(int totalHeight) {
        return CLOCK_USE_DEFAULT_Y;
    }

    @Override
    public void setStyle(Style style) {
    }

    @Override
    public void setTextColor(int color) {
        mTimeMM.setTextColor(color);
        mDate.setTextColor(color);
    }

    @Override
    public void setColorPalette(boolean supportsDarkText, int[] colorPalette) {
    
        if (colorPalette == null || colorPalette.length == 0) {
            return;
        }
        final int accentC = colorPalette[Math.max(0, colorPalette.length - 5)];
        mTimeHH.setTextColor(accentC);

    }

    @Override
    public void setDarkAmount(float darkAmount) {
        if (mView != null){
            mView.setDarkAmount(darkAmount);
        }
    }

    public void SetBatteryPer(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mBatt.getContext().getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level * 100 / (float)scale;
        int fp=(int)batteryPct;
        String btper=String.valueOf(fp);
        mBatt.setText(btper+"%");
        progressC.setProgress(fp);
    }

    @Override
    public void onTimeTick() {
        mView.onTimeChanged();
        mTimeHH.refreshTime();
        mTimeMM.refreshTime();
        mDate.refreshTime();
        SetBatteryPer();
    }

    @Override
    public void onTimeZoneChanged(TimeZone timeZone) {
        onTimeTick();
    }

    @Override
    public boolean shouldShowStatusArea() {
        return false;
    }
}
