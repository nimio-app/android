package org.nimio.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.nimio.app.core.common.NimioResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.nimio.app.feature.account.domain.AccountRepository
import org.nimio.app.feature.account.domain.LocalProfileRepository
import org.nimio.app.feature.social.domain.SocialGraphRepository
import org.nimio.app.feature.status.domain.StatusRepository

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var accountRepository: AccountRepository
    @Inject lateinit var profileRepository: LocalProfileRepository
    @Inject lateinit var socialGraphRepository: SocialGraphRepository
    @Inject lateinit var statusRepository: StatusRepository
    private var syncedSessionUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleEmailVerificationIntent(intent)
        observeSessionBackedData()
        setContent {
            NimioApp(
                accountRepository = accountRepository,
                profileRepository = profileRepository,
                socialGraphRepository = socialGraphRepository,
                statusRepository = statusRepository
            )
        }
    }

    private fun observeSessionBackedData() {
        lifecycleScope.launch {
            accountRepository.observeSession().collect { session ->
                val activeUserId = session?.userId?.takeIf { it.isNotBlank() }
                if (activeUserId == null) {
                    syncedSessionUserId = null
                    return@collect
                }

                if (syncedSessionUserId == activeUserId) {
                    return@collect
                }

                syncedSessionUserId = activeUserId
                runCatching { statusRepository.refreshStatus() }
                runCatching { socialGraphRepository.refreshConnections(status = null) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleEmailVerificationIntent(intent)
    }

    private fun handleEmailVerificationIntent(intent: Intent?) {
        val data = intent?.data ?: return
        val host = data.host.orEmpty()
        if (data.scheme != "https" || (host != "nimio.org" && host != "www.nimio.org") || data.path != "/verify-email") {
            return
        }
        val token = data.getQueryParameter("token")?.trim().orEmpty()
        if (token.isBlank()) {
            Toast.makeText(this, R.string.verification_link_invalid, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            when (accountRepository.verifyEmailToken(token)) {
                is NimioResult.Success -> {
                    accountRepository.refreshSession()
                    Toast.makeText(
                        this@MainActivity,
                        R.string.verification_success,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is NimioResult.Error -> {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.verification_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
