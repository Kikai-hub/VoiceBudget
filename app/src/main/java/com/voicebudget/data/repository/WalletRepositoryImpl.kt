package com.voicebudget.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.voicebudget.R
import com.voicebudget.data.database.FinancialGoalDao
import com.voicebudget.data.database.TransactionDao
import com.voicebudget.data.database.WalletDao
import com.voicebudget.data.database.WalletEntity
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.MAX_WALLETS
import com.voicebudget.domain.model.Wallet
import com.voicebudget.domain.repository.WalletLimitReachedException
import com.voicebudget.domain.repository.WalletRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Key of the pre-wallet global currency setting, kept only to seed the bootstrap wallet's currency. */
private val LEGACY_CURRENCY_KEY = stringPreferencesKey("currency")
private val ACTIVE_WALLET_ID_KEY = longPreferencesKey("active_wallet_id")

class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val transactionDao: TransactionDao,
    private val financialGoalDao: FinancialGoalDao,
    private val dataStore: DataStore<Preferences>,
    @param:ApplicationContext private val context: Context,
) : WalletRepository {

    override fun observeWallets(): Flow<List<Wallet>> =
        walletDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeActiveWalletId(): Flow<Long?> =
        dataStore.data.map { it[ACTIVE_WALLET_ID_KEY] }

    override fun observeActiveWallet(): Flow<Wallet?> =
        combine(observeWallets(), observeActiveWalletId()) { wallets, activeId ->
            wallets.firstOrNull { it.id == activeId } ?: wallets.firstOrNull()
        }

    override suspend fun getActiveWalletId(): Long? = observeActiveWallet().first()?.id

    override suspend fun getWalletById(id: Long): Wallet? = walletDao.getById(id)?.toDomain()

    override suspend fun createWallet(name: String, currency: Currency): Result<Wallet> {
        val existingCount = walletDao.getCount()
        if (existingCount >= MAX_WALLETS) return Result.failure(WalletLimitReachedException())
        val entity = WalletEntity(
            name = name,
            currency = currency.name,
            createdAt = System.currentTimeMillis(),
            orderIndex = existingCount,
        )
        val id = walletDao.insert(entity)
        setActiveWallet(id)
        return Result.success(entity.copy(id = id).toDomain())
    }

    override suspend fun setActiveWallet(id: Long) {
        dataStore.edit { it[ACTIVE_WALLET_ID_KEY] = id }
    }

    override suspend fun updateActiveWalletCurrency(currency: Currency) {
        val activeId = getActiveWalletId() ?: return
        val wallet = walletDao.getById(activeId) ?: return
        walletDao.update(wallet.copy(currency = currency.name))
    }

    override suspend fun ensureDefaultWallet() {
        if (walletDao.getCount() > 0) return
        val hasLegacyData = transactionDao.getCount() > 0 || financialGoalDao.getCount() > 0
        if (!hasLegacyData) return

        val legacyCurrency = dataStore.data.first()[LEGACY_CURRENCY_KEY]
            ?.let { runCatching { Currency.valueOf(it) }.getOrNull() }
            ?: Currency.RUB
        val entity = WalletEntity(
            name = context.getString(R.string.wallet_default_name, 1),
            currency = legacyCurrency.name,
            createdAt = System.currentTimeMillis(),
            orderIndex = 0,
        )
        val id = walletDao.insert(entity)
        setActiveWallet(id)
    }
}
