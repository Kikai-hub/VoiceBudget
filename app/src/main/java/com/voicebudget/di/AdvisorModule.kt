package com.voicebudget.di

import com.voicebudget.data.repository.AdvisorSettingsRepositoryImpl
import com.voicebudget.domain.advisor.AdviceGenerator
import com.voicebudget.domain.advisor.generators.CategoryGrowthAdviceGenerator
import com.voicebudget.domain.advisor.generators.TopCategoryBudgetAdviceGenerator
import com.voicebudget.domain.advisor.generators.HighSpendingAdviceGenerator
import com.voicebudget.domain.advisor.generators.IncomeTrendAdviceGenerator
import com.voicebudget.domain.advisor.generators.MonthlySummaryAdviceGenerator
import com.voicebudget.domain.advisor.generators.RecurringPaymentAdviceGenerator
import com.voicebudget.domain.advisor.generators.SavingsAdviceGenerator
import com.voicebudget.domain.advisor.generators.SmallPurchasesAdviceGenerator
import com.voicebudget.domain.repository.AdvisorSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdvisorModule {

    @Binds
    @Singleton
    abstract fun bindAdvisorSettingsRepository(
        impl: AdvisorSettingsRepositoryImpl,
    ): AdvisorSettingsRepository

    @Binds
    @IntoSet
    abstract fun bindHighSpendingGenerator(impl: HighSpendingAdviceGenerator): AdviceGenerator

    @Binds
    @IntoSet
    abstract fun bindCategoryGrowthGenerator(impl: CategoryGrowthAdviceGenerator): AdviceGenerator

    @Binds
    @IntoSet
    abstract fun bindTopCategoryBudgetGenerator(impl: TopCategoryBudgetAdviceGenerator): AdviceGenerator

    @Binds
    @IntoSet
    abstract fun bindSmallPurchasesGenerator(impl: SmallPurchasesAdviceGenerator): AdviceGenerator

    @Binds
    @IntoSet
    abstract fun bindSavingsGenerator(impl: SavingsAdviceGenerator): AdviceGenerator

    @Binds
    @IntoSet
    abstract fun bindRecurringPaymentGenerator(impl: RecurringPaymentAdviceGenerator): AdviceGenerator

    @Binds
    @IntoSet
    abstract fun bindIncomeTrendGenerator(impl: IncomeTrendAdviceGenerator): AdviceGenerator

    @Binds
    @IntoSet
    abstract fun bindMonthlySummaryGenerator(impl: MonthlySummaryAdviceGenerator): AdviceGenerator
}
