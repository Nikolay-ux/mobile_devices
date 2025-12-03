package com.example.myapplication1.di

import com.example.myapplication1.repository.GameRepository
import com.example.myapplication1.viewmodel.GameViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { GameRepository.getInstance(androidContext()) }
    viewModel { GameViewModel(get()) }
}