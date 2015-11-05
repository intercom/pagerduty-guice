package io.intercom.pagerduty.module;

public interface RatedAlarm {

    /**
     * Fire an alarm into PagerDuty, attempting to cap it to once per hour.<p>
     *
     * The deduplication check for debouncing an incident key is local to the running JVM and not
     * distributed across JVMs.
     *
     * @param incidentKey The PagerDuty incident key that will be debounced
     * @param description An incident description
     * @return true If the alarm fired, false if it was debounced
     */
    boolean alarm(String incidentKey, String description);
}
