package org.ardulink.camel;

import java.io.IOException;
import java.util.Map;

import org.apache.camel.Consumer;
import org.apache.camel.MultipleConsumersSupport;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.messages.api.LinkMessageAdapter;
import org.ardulink.util.URIs;

public class ArdulinkEndpoint extends DefaultEndpoint implements MultipleConsumersSupport {

	private Link link;
	private LinkMessageAdapter linkMessageAdapter;
	
	public ArdulinkEndpoint(ArdulinkComponent ardulinkComponent, String uri, String remaining, Map<String, Object> parameters) {
		super(uri, ardulinkComponent);
		link = LinkManager.getInstance().getConfigurer(URIs.newURI(uri)).newLink();
		try {
			linkMessageAdapter = new LinkMessageAdapter(link);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Link Message adapter init fails", e);
		}
	}

	@Override
	public Producer createProducer() throws Exception {
		ArdulinkProducer producer = new ArdulinkProducer(this);

		return producer;
	}

	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
		ArdulinkConsumer consumer = new ArdulinkConsumer(this, processor);

		return consumer;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	@Override
	public boolean isMultipleConsumersSupported() {
		return true;
	}

	public Link getLink() {
		return link;
	}

	public LinkMessageAdapter getLinkMessageAdapter() {
		return linkMessageAdapter;
	}
	
}
