package com.zyproo.devlink.di


import com.google.gson.Gson
import com.zyproo.devlink.BuildConfig
import com.zyproo.devlink.data.remote.AuthApi
import com.zyproo.devlink.data.remote.ProfileApi
import com.zyproo.devlink.data.remote.SkillApi
import com.zyproo.devlink.data.remote.NotificationApi
import com.zyproo.devlink.data.remote.ProjectApi
import com.zyproo.devlink.data.remote.CertificateApi
import com.zyproo.devlink.data.remote.VerificationApi
import com.zyproo.devlink.data.remote.ChatApi
import com.zyproo.devlink.data.remote.TeamApi
import com.zyproo.devlink.data.remote.JobApi
import com.zyproo.devlink.data.remote.AdminApi
import com.zyproo.devlink.data.remote.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {

            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        hostInterceptor: com.zyproo.devlink.data.remote.HostInterceptor,
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(hostInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi = retrofit.create(ProfileApi::class.java)

    @Provides
    @Singleton
    fun provideSkillApi(retrofit: Retrofit): SkillApi = retrofit.create(SkillApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideProjectApi(retrofit: Retrofit): ProjectApi = retrofit.create(ProjectApi::class.java)

    @Provides
    @Singleton
    fun provideCertificateApi(retrofit: Retrofit): CertificateApi = retrofit.create(CertificateApi::class.java)

    @Provides
    @Singleton
    fun provideVerificationApi(retrofit: Retrofit): VerificationApi = retrofit.create(VerificationApi::class.java)

    @Provides
    @Singleton
    fun provideChatApi(retrofit: Retrofit): ChatApi = retrofit.create(ChatApi::class.java)

    @Provides
    @Singleton
    fun provideTeamApi(retrofit: Retrofit): TeamApi = retrofit.create(TeamApi::class.java)

    @Provides
    @Singleton
    fun provideJobApi(retrofit: Retrofit): JobApi = retrofit.create(JobApi::class.java)

    @Provides
    @Singleton
    fun provideFileApi(retrofit: Retrofit): com.zyproo.devlink.data.remote.FileApi = retrofit.create(com.zyproo.devlink.data.remote.FileApi::class.java)

    @Provides
    @Singleton
    fun provideFeedbackApi(retrofit: Retrofit): com.zyproo.devlink.data.remote.FeedbackApi = retrofit.create(com.zyproo.devlink.data.remote.FeedbackApi::class.java)

    @Provides
    @Singleton
    fun provideAdminApi(retrofit: Retrofit): AdminApi = retrofit.create(AdminApi::class.java)
}
