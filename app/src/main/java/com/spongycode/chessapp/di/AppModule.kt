package com.spongycode.chessapp.di

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import com.spongycode.chessapp.BuildConfig
import com.spongycode.chessapp.repository.ChessRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideRepository(@ApplicationContext context: Context): ChessRepository {
        return ChessRepository(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        val dbUrl = BuildConfig.FIREBASE_DB_URL
        return FirebaseDatabase.getInstance(dbUrl).apply {
            setPersistenceEnabled(true)
        }
    }
}