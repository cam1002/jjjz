package com.glass.ledger

import android.app.Application
import com.glass.ledger.data.LedgerRepository
import com.glass.ledger.data.local.AppDatabase

class GlassLedgerApp : Application() {
    lateinit var repository: LedgerRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = LedgerRepository(AppDatabase.get(this))
    }

    companion object {
        fun from(any: Any): GlassLedgerApp =
            (any as? GlassLedgerApp)
                ?: (any as? android.content.Context)?.applicationContext as GlassLedgerApp
    }
}
