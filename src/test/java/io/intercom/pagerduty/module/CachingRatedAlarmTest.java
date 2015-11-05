package io.intercom.pagerduty.module;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.inject.Named;
import javax.inject.Singleton;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CachingRatedAlarmTest {


    private static Injector injector;

    @BeforeClass
    public static void beforeClass() {
        injector = Guice.createInjector(new PagerDutyModule(), new AbstractModule() {

            @Override
            protected void configure() {
            }

            @Singleton
            @Provides
            @Named("PagerDutyApiKey")
            public String providePagerDuty() {
                return "noop";
            }
        });
    }

    @Test
    public void testDebounce() {
        final RatedAlarmFactory factory = injector.getInstance(RatedAlarmFactory.class);

        // can't spy on PagerDuty or RatedAlarm directly
        CachingRatedAlarm alarm = (CachingRatedAlarm) factory.createAlarm();
        assertNotNull(alarm);
        alarm = spy(alarm);

        doReturn(null)
            .when(alarm)
            .alarmPagerDuty(anyObject(), startsWith("myKey"), startsWith("myDesc"));

        doReturn(null)
            .when(alarm)
            .alarmPagerDuty(anyObject(), startsWith("myOtherKey"), startsWith("myOtherDesc"));

        alarm.alarm("myKey", "myDesc");
        alarm.alarm("myKey", "myDesc");
        alarm.alarm("myOtherKey", "myOtherDesc");

        verify(alarm, times(1))
            .alarmPagerDuty(anyObject(), startsWith("myKey"), startsWith("myDesc"));

        verify(alarm, times(1))
            .alarmPagerDuty(anyObject(), startsWith("myOtherKey"), startsWith("myOtherDesc"));
    }


}