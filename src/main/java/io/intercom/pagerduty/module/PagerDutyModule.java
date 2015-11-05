package io.intercom.pagerduty.module;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import com.squareup.pagerduty.incidents.PagerDuty;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

public class PagerDutyModule extends AbstractModule {

    @Override
    protected void configure() {
        install(
            new FactoryModuleBuilder()
                .implement(RatedAlarm.class, CachingRatedAlarm.class)
                .build(RatedAlarmFactory.class)
        );
    }

    @Singleton
    @Provides
    public PagerDuty providePagerDuty(@Named("PagerDutyApiKey") String apiKey) {
        return PagerDuty.create(apiKey);
    }

    @Singleton
    @Provides
    @Named("PagerDutyRatedAlarmCache")
    public Cache<String, Long> provideAlarmCache() {
        return CacheBuilder
            .newBuilder()
            .maximumSize(512L)
            .expireAfterWrite(3605L, TimeUnit.SECONDS)
            .build();
    }

}
