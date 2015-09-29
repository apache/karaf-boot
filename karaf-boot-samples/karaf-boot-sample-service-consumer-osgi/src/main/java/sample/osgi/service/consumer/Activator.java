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
package sample.osgi.service.consumer;

import org.apache.karaf.util.tracker.BaseActivator;
import org.apache.karaf.util.tracker.annotation.RequireService;
import org.apache.karaf.util.tracker.annotation.Services;
import sample.osgi.service.provider.HelloService;

/**
 * Simple activator that retrieve the hello service and use it.
 */
@Services(
        requires = {
                @RequireService(HelloService.class)
        }
)
public class Activator extends BaseActivator {

    @Override
    public void doStart() throws Exception {
        HelloService helloService = getTrackedService(HelloService.class);

        // basic usage, in real life, it could be injected and used in a bean or whatever
        System.out.println("Hello " + helloService.hello("World"));
    }

    @Override
    public void doStop() {
        super.doStop();
    }


}
