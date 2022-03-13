package com.palladium.android.systemui.dagger;

import com.android.systemui.dagger.DefaultComponentBinder;
import com.android.systemui.dagger.DependencyProvider;
import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.dagger.SystemUIBinder;
import com.android.systemui.dagger.SysUIComponent;
import com.android.systemui.dagger.SystemUIModule;

import com.palladium.android.systemui.keyguard.KeyguardSliceProviderPalladium;
import com.palladium.android.systemui.smartspace.KeyguardSmartspaceController;

import dagger.Subcomponent;

@SysUISingleton
@Subcomponent(modules = {
        DefaultComponentBinder.class,
        DependencyProvider.class,
        SystemUIBinder.class,
        SystemUIModule.class,
        SystemUIPalladiumModule.class })
public interface SysUIComponentPalladium extends SysUIComponent {
    @SysUISingleton
    @Subcomponent.Builder
    interface Builder extends SysUIComponent.Builder {
        SysUIComponentPalladium build();
    }

    /**
     * Member injection into the supplied argument.
     */
    void inject(KeyguardSliceProviderPalladium keyguardSliceProviderPalladium);

    @SysUISingleton
    KeyguardSmartspaceController createKeyguardSmartspaceController();
}
