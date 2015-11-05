package io.intercom.pagerduty.module;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.inject.Inject;

import com.squareup.pagerduty.incidents.NotifyResult;
import com.squareup.pagerduty.incidents.PagerDuty;
import com.squareup.pagerduty.incidents.Trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.inject.Named;

class CachingRatedAlarm implements RatedAlarm {

    private static final Logger logger = LoggerFactory.getLogger("PagerDutyRatedAlarm");

    private final PagerDuty pagerDuty;
    private final Cache<String, Long> cache;

    @Inject
    public CachingRatedAlarm(
        PagerDuty pagerDuty,
        @Named("PagerDutyRatedAlarmCache") Cache<String, Long> cache
    ) {
        this.pagerDuty = pagerDuty;
        this.cache = cache;
    }

    /**
     * Fire an alarm into PagerDuty, attempting to cap it to once per hour.<p>
     *
     * The deduplication check for debouncing an incident key is local to the running JVM and not
     * distributed across JVMs.
     *
     * @param incidentKey The PagerDuty incident key that will be debounced
     * @param description An incident description
     * @return true if the alarm fired, false if it was debounced
     */
    public boolean alarm(String incidentKey, String description) {
        Preconditions.checkNotNull(incidentKey, "An incident key must be provided");
        Preconditions.checkNotNull(description, "A description must be provided");

        final String window = buildWindowedIncidentKey(incidentKey);
        if (isIncidentKeyCached(window)) {
            logger.debug("pagerduty_alarm op=debounce_alarm incidentKey={} incidentKeyWindow={}",
                incidentKey, window);
            return false;
        } else {
            cache.put(window, 1L);
            alarmPagerDuty(pagerDuty, window, description);
            logger.debug("pagerduty_alarm op=alarm incidentKey={} incidentKeyWindow={}",
                incidentKey, window);
            return true;
        }
    }

    @VisibleForTesting
    NotifyResult alarmPagerDuty(PagerDuty pagerDuty, String incidentKey, String description) {
        return pagerDuty.notify(
            new Trigger.Builder(description).withIncidentKey(incidentKey).build()
        );
    }

    private String buildWindowedIncidentKey(String incidentKey) {
        return incidentKey + ":" + currentHourMillis();
    }

    private long currentHourMillis() {
        return LocalDateTime
            .now(Clock.systemUTC())
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .atZone(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli();
    }

    private boolean isIncidentKeyCached(String incidentKey) {
        return readCache(incidentKey);
    }

    private boolean readCache(String incidentKey) {
        return cache.getIfPresent(incidentKey) != null;
    }
}
