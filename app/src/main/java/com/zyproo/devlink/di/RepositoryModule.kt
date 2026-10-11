package com.zyproo.devlink.di


import com.zyproo.devlink.data.repository.AuthRepository
import com.zyproo.devlink.data.repository.AuthRepositoryImpl
import com.zyproo.devlink.data.repository.ProfileRepository
import com.zyproo.devlink.data.repository.ProfileRepositoryImpl
import com.zyproo.devlink.data.repository.SkillRepository
import com.zyproo.devlink.data.repository.SkillRepositoryImpl
import com.zyproo.devlink.data.repository.NotificationRepository
import com.zyproo.devlink.data.repository.NotificationRepositoryImpl
import com.zyproo.devlink.data.repository.ProjectRepository
import com.zyproo.devlink.data.repository.ProjectRepositoryImpl
import com.zyproo.devlink.data.repository.CertificateRepository
import com.zyproo.devlink.data.repository.CertificateRepositoryImpl
import com.zyproo.devlink.data.repository.VerificationRepository
import com.zyproo.devlink.data.repository.VerificationRepositoryImpl
import com.zyproo.devlink.data.repository.ChatRepository
import com.zyproo.devlink.data.repository.ChatRepositoryImpl
import com.zyproo.devlink.data.repository.TeamRepository
import com.zyproo.devlink.data.repository.TeamRepositoryImpl
import com.zyproo.devlink.data.repository.JobRepository
import com.zyproo.devlink.data.repository.JobRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    abstract fun bindSkillRepository(impl: SkillRepositoryImpl): SkillRepository

    @Binds
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository

    @Binds
    abstract fun bindCertificateRepository(impl: CertificateRepositoryImpl): CertificateRepository

    @Binds
    abstract fun bindVerificationRepository(impl: VerificationRepositoryImpl): VerificationRepository

    @Binds
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    abstract fun bindTeamRepository(impl: TeamRepositoryImpl): TeamRepository

    @Binds
    abstract fun bindJobRepository(impl: JobRepositoryImpl): JobRepository

    @Binds
    abstract fun bindFileRepository(impl: com.zyproo.devlink.data.repository.FileRepositoryImpl): com.zyproo.devlink.data.repository.FileRepository

    @Binds
    abstract fun bindFeedbackRepository(impl: com.zyproo.devlink.data.repository.FeedbackRepositoryImpl): com.zyproo.devlink.data.repository.FeedbackRepository

    @Binds
    abstract fun bindAdminRepository(impl: com.zyproo.devlink.data.repository.AdminRepositoryImpl): com.zyproo.devlink.data.repository.AdminRepository
}
