/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.tencent.tws.assistant.drawable;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.view.animation.LinearInterpolator;

import com.tencent.tws.assistant.utils.FloatProperty;

/**
 * Draws a ripple background.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class TwsRippleBackground extends TwsRippleComponent {
    private static final TimeInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

    private static final int OPACITY_ENTER_DURATION = 600;
    private static final int OPACITY_ENTER_DURATION_FAST = 120;
    private static final int OPACITY_EXIT_DURATION = 480;

    // Software rendering properties.
    private float mOpacity = 0;

    public TwsRippleBackground(TwsRippleDrawable owner, Rect bounds) {
        super(owner, bounds);
    }

    public boolean isVisible() {
        return mOpacity > 0;
    }

    @Override
    protected boolean drawSoftware(Canvas c, Paint p) {
        boolean hasContent = false;

        final int origAlpha = p.getAlpha();
        final int alpha = (int) (origAlpha * mOpacity + 0.5f);
        if (alpha > 0) {
            p.setAlpha(alpha);
            c.drawCircle(0, 0, mTargetRadius, p);
            p.setAlpha(origAlpha);
            hasContent = true;
        }

        return hasContent;
    }

    @Override
    protected Animator createSoftwareEnter(boolean fast) {
        // Linear enter based on current opacity.
        final int maxDuration = fast ? OPACITY_ENTER_DURATION_FAST : OPACITY_ENTER_DURATION;
        final int duration = (int) ((1 - mOpacity) * maxDuration);

        final ObjectAnimator opacity = ObjectAnimator.ofFloat(this, OPACITY, 1);
        opacity.setAutoCancel(true);
        opacity.setDuration(duration);
        opacity.setInterpolator(LINEAR_INTERPOLATOR);

        return opacity;
    }

    @Override
    protected Animator createSoftwareExit() {
        final AnimatorSet set = new AnimatorSet();

        // Linear exit after enter is completed.
        final ObjectAnimator exit = ObjectAnimator.ofFloat(this, TwsRippleBackground.OPACITY, 0);
        exit.setInterpolator(LINEAR_INTERPOLATOR);
        exit.setDuration(OPACITY_EXIT_DURATION);
        exit.setAutoCancel(true);

        final AnimatorSet.Builder builder = set.play(exit);

        // Linear "fast" enter based on current opacity.
        final int fastEnterDuration = (int) ((1 - mOpacity) * OPACITY_ENTER_DURATION_FAST);
        if (fastEnterDuration > 0) {
            final ObjectAnimator enter = ObjectAnimator.ofFloat(this, TwsRippleBackground.OPACITY, 1);
            enter.setInterpolator(LINEAR_INTERPOLATOR);
            enter.setDuration(fastEnterDuration);
            enter.setAutoCancel(true);

            builder.after(enter);
        }

        return set;
    }

    private static abstract class BackgroundProperty extends FloatProperty<TwsRippleBackground> {
        public BackgroundProperty(String name) {
            super(name);
        }
    }

    private static final BackgroundProperty OPACITY = new BackgroundProperty("opacity") {
        @Override
        public void setValue(TwsRippleBackground object, float value) {
            object.mOpacity = value;
            object.invalidateSelf();
        }

        @Override
        public Float get(TwsRippleBackground object) {
            return object.mOpacity;
        }
    };

    @Override
    protected int getRippleStyle() {
        return TwsRippleDrawable.RIPPLE_STYLE_NORMAL;
    }
}
