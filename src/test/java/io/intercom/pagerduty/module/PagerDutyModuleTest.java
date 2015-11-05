package io.intercom.pagerduty.module;

import com.google.common.cache.Cache;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import com.squareup.pagerduty.incidents.FakePagerDuty;
import com.squareup.pagerduty.incidents.PagerDuty;

import org.junit.Test;

import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.inject.name.Names.named;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PagerDutyModuleTest {

    @Test
    public void testFakeModule() {
        final Injector injector = Guice.createInjector(new PagerDutyFakeModule());
        final Cache ratedAlarmCache =
            injector.getInstance(Key.get(new TypeLiteral<Cache<String, Long>>() {},
                named("PagerDutyRatedAlarmCache")));
        assertNotNull(ratedAlarmCache);

        final PagerDuty pdp = injector.getInstance(PagerDuty.class);
        assertEquals(pdp.getClass(), FakePagerDuty.class);

        final RatedAlarmFactory factory = injector.getInstance(RatedAlarmFactory.class);
        assertNotNull(factory.createAlarm());
    }

    @Test
    public void testModule() {
        final Injector injector = Guice.createInjector(new PagerDutyModule(), new AbstractModule() {
            @Override
            protected void configure() {

                System.getProperty("PD_API_KEY", System.getenv("PD_API_KEY"));
            }

            @Singleton
            @Provides
            @Named("PagerDutyApiKey")
            public String providePagerDutyApiKey() {
                return "noop";
            }
        });
        final Cache ratedAlarmCache =
            injector.getInstance(Key.get(new TypeLiteral<Cache<String, Long>>() {},
                named("PagerDutyRatedAlarmCache")));
        assertNotNull(ratedAlarmCache);

        final PagerDuty pdp = injector.getInstance(PagerDuty.class);
        assertNotNull(pdp);

        final String  key = injector.getInstance(Key.get(String.class, named("PagerDutyApiKey")));
        assertNotNull(key);


        final RatedAlarmFactory factory = injector.getInstance(RatedAlarmFactory.class);
        assertNotNull(factory.createAlarm());
    }




}