package org.nimio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.nimio.app.feature.account.domain.AccountRepository
import org.nimio.app.feature.account.domain.LocalProfileRepository

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var accountRepository: AccountRepository
    @Inject lateinit var profileRepository: LocalProfileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NimioApp(
                accountRepository = accountRepository,
                profileRepository = profileRepository
            )
        }
    }
}
