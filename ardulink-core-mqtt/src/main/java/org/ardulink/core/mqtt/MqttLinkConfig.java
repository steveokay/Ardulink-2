/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.ardulink.core.mqtt;

import static org.ardulink.util.Preconditions.checkNotNull;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.ardulink.core.linkmanager.LinkConfig;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MqttLinkConfig implements LinkConfig {

	private static final String LOCALHOST = "localhost";

	@Named("host")
	@NotNull
	private String host = LOCALHOST;

	@Named("port")
	@Min(1)
	@Max(2 << 16 - 1)
	private int port = 1883;

	@Named("topic")
	@NotNull
	private String topic = normalize("home/devices/ardulink/");

	@Named("clientid")
	@NotNull
	private String clientId = "ardulink-mqtt-link";

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host == null ? LOCALHOST : host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = checkNotNull(normalize(topic), "topic must not be null");
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = checkNotNull(clientId, "clientId must not be null");
	}

	private static String normalize(String topic) {
		return topic.endsWith("/") ? topic : topic + "/";
	}

}
