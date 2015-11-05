# pagerduty-guice

A dumb-as-you-like Guice module for setting up a PagerDuty client from the excellent 
[pagerduty-incidents]
(https://github.com/square/pagerduty-incidents) library.

## Usage

To add the module, include `PagerDutyModule` in the list of injected modules. You'll need to
 provide a Named string called `"PagerDutyApiKey"` that contains your key. 
 
 
```java
    final Injector injector = Guice.createInjector(new PagerDutyModule(), new AbstractModule() {
        @Override
        protected void configure() {
        }
    
        @Singleton
        @Provides
        @Named("PagerDutyApiKey")
        public String providePagerDutyApiKey() {
            return System.getProperty("PD_API_KEY", System.getenv("PD_API_KEY"));
        }
    });
``` 

After that a `PagerDuty` will be available via `@Inject`. There's also a  `PagerDutyFakeModule` 
you can use for testing and development, which doesn't require the named `"PagerDutyApiKey"`.

The `RatedAlarmFactory` is available via `@Inject` and supplies a `RatedAlarm` whose `alarm` 
method is rate limited with best effort to once per hour. It caches your incident key with a 
timestamp to the nearest hour and debounces further alarms for the rest of that hour until a new 
timestamp qualifier comes around. This is a handy way to fire an alarm and not overload your 
PagerDuty setup. 


## Adding a dependency

The distribution is hosted on [bintray](https://bintray.com/intercom/intercom-maven/pagerduty-guice/view).


For Maven, Add jcenter to your repositories in `pom.xml` or `settings.xml`:

```xml
<repositories>
  <repository>
    <id>jcenter</id>
    <url>http://jcenter.bintray.com</url>
  </repository>
</repositories>
```

and add the project declaration to your `pom.xml`:

```xml
<dependency>
  <groupId>io.intercom.module</groupId>
  <artifactId>pagerduty-guice</artifactId>
  <version>0.0.1</version>
</dependency>
```

Or for Gradle, add jcenter to your `repositories` block:

```groovy
repositories {
  jcenter()
}
```

and add the project to the `dependencies` block in your `build.gradle`:

```groovy
dependencies {
    compile 'io.intercom.module:pagerduty-guice:0.0.1'
}
```

## License


    Copyright 2015 Intercom, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


