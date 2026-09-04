package org.daidai.browser

import android.app.Application
import org.daidai.browser.adblock.HostsBlocker
import org.daidai.browser.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Minimal manual dependency container — no DI framework, on purpose.
 * Fewer moving parts means easier reproducible builds (F-Droid friendly).
 */
class AppContainer(application: Application) {

    val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settings: SettingsRepository = SettingsRepository(application)

    val blocker: HostsBlocker = HostsBlocker(application, appScope)

    /** URLs handed to the browser from outside (link intents), consumed by the view model. */
    val pendingUrl = MutableSharedFlow<String>(extraBufferCapacity = 4)
}
