package me.progneo.unifychat.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import me.progneo.unifychat.data.repository.vk.UsersRepositoryImpl as VkUsersRepositoryImpl
import me.progneo.unifychat.domain.repository.vk.UsersRepository as VkUsersRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVkUsersRepository(
        usersRepositoryImpl: VkUsersRepositoryImpl,
    ): VkUsersRepository
}
