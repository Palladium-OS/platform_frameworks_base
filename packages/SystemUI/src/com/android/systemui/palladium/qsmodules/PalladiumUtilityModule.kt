/*
 * Copyright (C) 2023 Palladium-OS
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

package com.android.systemui.palladium.qsmodules

import com.android.systemui.qs.tileimpl.QSTileImpl
import com.android.systemui.qs.tiles.UsbTetherTile
import com.android.systemui.qs.tiles.CompassTile
import com.android.systemui.qs.tiles.CPUInfoTile
import com.android.systemui.qs.tiles.FPSInfoTile
import com.android.systemui.qs.tiles.MonoToggleTile
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
interface PalladiumUtilityModule {

    /** Inject UsbTetherTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(UsbTetherTile.TILE_SPEC)
    fun bindUsbTetherTile(usbTetherTile: UsbTetherTile): QSTileImpl<*>   

    /** Inject CompassTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(CompassTile.TILE_SPEC)
    fun bindCompassTile(compassTile: CompassTile): QSTileImpl<*>  

    /** Inject CPUInfoTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(CPUInfoTile.TILE_SPEC)
    fun bindCPUInfoTile(cpuInfoTile: CPUInfoTile ): QSTileImpl<*>   

    /** Inject FPSInfoTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(FPSInfoTile.TILE_SPEC)
    fun bindFPSInfoTile(fpsInfoTile: FPSInfoTile): QSTileImpl<*>         
    
    /** Inject MonoToggleTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(MonoToggleTile.TILE_SPEC)
    fun bindMonoToggleTile(monoToggleTile: MonoToggleTile): QSTileImpl<*>            
   
}
