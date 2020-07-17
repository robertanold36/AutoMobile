package com.car.service.automobile.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component

@Component
interface AppComponent {

    @Component.Factory
    interface Factory{
        fun create(@BindsInstance context: Context):AppComponent
    }

}