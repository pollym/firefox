/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.fenix.home.logocontrol

import android.view.ViewGroup
import org.mozilla.fenix.longfox.LongFoxFeature

class LogoController(private val longFoxFeature: LongFoxFeature,
                     private val container: ViewGroup?) {

    fun handleLogoClicked() {
        if (container==null) return
        longFoxFeature.start(container = container)
    }

}
