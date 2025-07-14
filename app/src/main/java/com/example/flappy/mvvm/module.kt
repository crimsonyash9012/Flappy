package com.example.flappy.mvvm

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appwriteModule = module {
    single{AppwriteRepository(androidContext())}
    viewModel {AppwriteViewModel(get())}
}
