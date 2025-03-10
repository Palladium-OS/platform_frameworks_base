/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.widget.LinearLayout;

import androidx.constraintlayout.helper.widget.Flow;

import com.android.internal.util.LatencyTracker;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.systemui.R;
import com.android.systemui.classifier.FalsingCollector;
import com.android.systemui.flags.FeatureFlags;
import com.android.systemui.flags.Flags;
import com.android.systemui.statusbar.policy.DevicePostureController;
import com.android.keyguard.PasswordTextView.QuickUnlockListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.List;

public class KeyguardPinViewController
        extends KeyguardPinBasedInputViewController<KeyguardPINView> {
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final DevicePostureController mPostureController;
    private final DevicePostureController.Callback mPostureCallback = posture ->
            mView.onDevicePostureChanged(posture);
    private LockPatternUtils mLockPatternUtils;
    private final View mDeleteButton;
    private boolean mDeleteButtonShowing = true;
    private final FeatureFlags mFeatureFlags;
    private static final int DEFAULT_PIN_LENGTH = 6;
    private static final int MIN_FAILED_PIN_ATTEMPTS = 5;
    private NumPadButton mBackspaceKey;
    private View mOkButton = mView.findViewById(R.id.key_enter);

    private long mPinLength;

    private boolean mDisabledAutoConfirmation;

    protected KeyguardPinViewController(KeyguardPINView view,
            KeyguardUpdateMonitor keyguardUpdateMonitor,
            SecurityMode securityMode, LockPatternUtils lockPatternUtils,
            KeyguardSecurityCallback keyguardSecurityCallback,
            KeyguardMessageAreaController.Factory messageAreaControllerFactory,
            LatencyTracker latencyTracker, LiftToActivateListener liftToActivateListener,
            EmergencyButtonController emergencyButtonController,
            FalsingCollector falsingCollector,
            DevicePostureController postureController,
            FeatureFlags featureFlags) {
        super(view, keyguardUpdateMonitor, securityMode, lockPatternUtils, keyguardSecurityCallback,
                messageAreaControllerFactory, latencyTracker, liftToActivateListener,
                emergencyButtonController, falsingCollector, featureFlags);
        mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        mPostureController = postureController;
        mLockPatternUtils = lockPatternUtils;
        mFeatureFlags = featureFlags;
        mBackspaceKey = view.findViewById(R.id.delete_button);
        mPinLength = mLockPatternUtils.getPinLength(KeyguardUpdateMonitor.getCurrentUser());
        mDeleteButton = mView.findViewById(R.id.delete_button);
    }

    @Override
    protected void onViewAttached() {
        super.onViewAttached();

        int passwordLength = mLockPatternUtils.getPinPasswordLength(
                KeyguardUpdateMonitor.getCurrentUser());

        mPasswordEntry.setQuickUnlockListener(new QuickUnlockListener() {
            public void onValidateQuickUnlock(String password) {
                if (password != null) {
                    int length = password.length();
                    if (length > 0) {
                        showDeleteButton(true, true);
                    } else if (length == 0) {
                        showDeleteButton(false, true);
                    }
                    if (length == passwordLength) {
                        verifyPasswordAndUnlock();
                    }
                }
            }
        });

        showDeleteButton(false, false);

        View okButton = mView.findViewById(R.id.key_enter);
        if (okButton != null) {
            /* show okButton only if password length is unset
               because quick unlock won't work */
            if (passwordLength != -1) {
                okButton.setVisibility(View.INVISIBLE);
                Flow flow = (Flow) mView.findViewById(R.id.flow1);
                if (flow != null) {
                    List<Integer> ids = Arrays.stream(flow.getReferencedIds())
                                            .boxed().collect(Collectors.toList());
                    Collections.swap(ids, 9 /* delete_button */, 11 /* key_enter */);
                    flow.setReferencedIds(ids.stream().mapToInt(i -> i).toArray());
                }
            }
        }

        View cancelBtn = mView.findViewById(R.id.cancel_button);
        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(view -> {
                getKeyguardSecurityCallback().reset();
                getKeyguardSecurityCallback().onCancelClicked();
            });
        }
        mPasswordEntry.setUserActivityListener(this::onUserInput);
        mPostureController.addCallback(mPostureCallback);
    }

    protected void onUserInput() {
        super.onUserInput();
        if (isAutoPinConfirmEnabledInSettings()) {
            updateAutoConfirmationState();
            if (mPasswordEntry.getText().length() == mPinLength
                    && mOkButton.getVisibility() == View.INVISIBLE) {
                verifyPasswordAndUnlock();
            }
        }
    }

    @Override
    protected void onViewDetached() {
        super.onViewDetached();
        mPostureController.removeCallback(mPostureCallback);
    }

    @Override
    public void startAppearAnimation() {
        if (mFeatureFlags.isEnabled(Flags.AUTO_PIN_CONFIRMATION)) {
            mPasswordEntry.setUsePinShapes(true);
            updateAutoConfirmationState();
        }
        super.startAppearAnimation();
    }

    @Override
    void resetState() {
        super.resetState();
        showDeleteButton(false, false);
    }    

    @Override
    public boolean startDisappearAnimation(Runnable finishRunnable) {
        return mView.startDisappearAnimation(
                mKeyguardUpdateMonitor.needsSlowUnlockTransition(), finishRunnable);
    }

    @Override
    protected void handleAttemptLockout(long elapsedRealtimeDeadline) {
        super.handleAttemptLockout(elapsedRealtimeDeadline);
        updateAutoConfirmationState();
    }

    private void updateAutoConfirmationState() {
        mDisabledAutoConfirmation = mLockPatternUtils.getCurrentFailedPasswordAttempts(
                KeyguardUpdateMonitor.getCurrentUser()) >= MIN_FAILED_PIN_ATTEMPTS;
        updateOKButtonVisibility();
        updateBackSpaceVisibility();
        updatePinHinting();
    }

    /**
     * Updates the visibility of the OK button for auto confirm feature
     */
    private void updateOKButtonVisibility() {
        if (isAutoPinConfirmEnabledInSettings() && !mDisabledAutoConfirmation) {
            mOkButton.setVisibility(View.INVISIBLE);
        } else {
            mOkButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Updates the visibility and the enabled state of the backspace.
     * Visibility changes are only for auto confirmation configuration.
     */
    private void updateBackSpaceVisibility() {
        boolean isAutoConfirmation = isAutoPinConfirmEnabledInSettings();
        mBackspaceKey.setTransparentMode(/* isTransparentMode= */
                isAutoConfirmation && !mDisabledAutoConfirmation);
        if (isAutoConfirmation) {
            if (mPasswordEntry.getText().length() > 0
                    || mDisabledAutoConfirmation) {
                mBackspaceKey.setVisibility(View.VISIBLE);
            } else {
                mBackspaceKey.setVisibility(View.INVISIBLE);
            }
        }
    }
    /** Updates whether to use pin hinting or not. */
    void updatePinHinting() {
        mPasswordEntry.setIsPinHinting(isAutoPinConfirmEnabledInSettings() && isPinHinting()
                && !mDisabledAutoConfirmation);
    }

    /**
     * Responsible for identifying if PIN hinting is to be enabled or not
     */
    private boolean isPinHinting() {
        return mLockPatternUtils.getPinLength(KeyguardUpdateMonitor.getCurrentUser())
                == DEFAULT_PIN_LENGTH;
    }

    /**
     * Responsible for identifying if auto confirm is enabled or not in Settings
     */
    private boolean isAutoPinConfirmEnabledInSettings() {
        //Checks if user has enabled the auto confirm in Settings
        return mLockPatternUtils.isAutoPinConfirmEnabled(KeyguardUpdateMonitor.getCurrentUser());
    }

    private void showDeleteButton(boolean show, boolean animate) {
        int visibility = show ? View.VISIBLE : View.INVISIBLE;
        if (mDeleteButton != null && mDeleteButtonShowing != show) {
            mDeleteButtonShowing = show;
            if (animate) {
                mDeleteButton.setAlpha(show ? 0.0f : 1.0f);
                mDeleteButton.animate()
                    .alpha(show ? 1.0f : 0.0f)
                    .setDuration(show ? 250 : 450)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (show) mDeleteButton.setVisibility(visibility);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!show) mDeleteButton.setVisibility(visibility);
                        }
                    });
            } else {
                mDeleteButton.setVisibility(visibility);
            }
        }
    }

}
