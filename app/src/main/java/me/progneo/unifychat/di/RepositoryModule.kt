package me.progneo.unifychat.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.progneo.unifychat.domain.repository.vk.UsersRepository as VkUsersRepository
import me.progneo.unifychat.data.repository.vk.UsersRepositoryImpl as VkUsersRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVkUsersRepository(
        usersRepositoryImpl: VkUsersRepositoryImpl
    ): VkUsersRepository
}