package com.palladium.android.systemui;

import android.content.Context;

import com.palladium.android.systemui.dagger.DaggerGlobalRootComponentPalladium;
import com.palladium.android.systemui.dagger.GlobalRootComponentPalladium;

import com.android.systemui.SystemUIFactory;
import com.android.systemui.dagger.GlobalRootComponent;

public class SystemUIPalladiumFactory extends SystemUIFactory {
    @Override
    protected GlobalRootComponent buildGlobalRootComponent(Context context) {
        return DaggerGlobalRootComponentPalladium.builder()
                .context(context)
                .build();
    }
}
