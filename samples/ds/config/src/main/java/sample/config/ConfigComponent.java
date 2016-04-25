/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package sample.config;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Sample Configuration", pid = "org.apache.karaf.boot.sample.config")
@interface SampleConfig {
    String stringProperty() default "default";
    int intProperty() default 0;
    boolean booleanProperty() default false;
}

@Component
@Designate(ocd = SampleConfig.class)
public class ConfigComponent {

    @Activate
    public void activate(SampleConfig sampleConfig) {
        System.out.println("We use the property there");
        System.out.println("stringProperty:" + sampleConfig.stringProperty());
        System.out.println("intProperty: " + sampleConfig.intProperty());
        System.out.println("booleanProperty: " + sampleConfig.booleanProperty());
    }

}
