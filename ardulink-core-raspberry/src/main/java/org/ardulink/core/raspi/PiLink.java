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

package org.ardulink.core.raspi;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.Pin.Type.ANALOG;
import static org.ardulink.core.Pin.Type.DIGITAL;
import static com.pi4j.io.gpio.PinMode.ANALOG_INPUT;
import static com.pi4j.io.gpio.PinMode.DIGITAL_INPUT;
import static com.pi4j.io.gpio.PinMode.DIGITAL_OUTPUT;
import static com.pi4j.io.gpio.PinMode.PWM_OUTPUT;
import static com.pi4j.io.gpio.PinPullResistance.PULL_DOWN;
import static com.pi4j.io.gpio.RaspiPin.getPinByName;
import static com.pi4j.io.gpio.event.PinEventType.ANALOG_VALUE_CHANGE;
import static com.pi4j.io.gpio.event.PinEventType.DIGITAL_STATE_CHANGE;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import java.io.IOException;
import java.util.List;

import org.ardulink.util.ListMultiMap;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import org.ardulink.core.events.DefaultDigitalPinValueChangedEvent;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class PiLink extends AbstractListenerLink {

	private final GpioController gpioController = GpioFactory.getInstance();
	private final ListMultiMap<GpioPin, GpioPinListener> listeners = new ListMultiMap<GpioPin, GpioPinListener>();

	@Override
	public void close() throws IOException {
		this.gpioController.shutdown();
		super.close();
	}

	@Override
	public void startListening(Pin pin) throws IOException {
		if (pin.is(ANALOG)) {
			addListener(pin, analogAdapter());
		} else if (pin.is(DIGITAL)) {
			addListener(pin, digitalAdapter());
		} else {
			throw new IllegalStateException("Unknown pin type of pin " + pin);
		}
	}

	private void addListener(Pin pin, GpioPinListener listener) {
		GpioPin gpioPin = getOrCreate(pin.pinNum(), pi4jInputMode(pin));
		gpioPin.setPullResistance(PULL_DOWN);
		gpioPin.addListener(listener);
		this.listeners.put(gpioPin, listener);
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		GpioPin gpioPin = getOrCreate(pin.pinNum(), pi4jInputMode(pin));
		List<GpioPinListener> list = listeners.asMap().get(gpioPin);
		if (list == null) {
			return;
		}
		for (GpioPinListener gpioPinListener : list) {
			gpioPin.removeListener(gpioPinListener);
			listeners.remove(gpioPin, gpioPinListener);
		}
	}

	@Override
	public void switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		GpioPinPwmOutput pin = (GpioPinPwmOutput) getOrCreate(
				analogPin.pinNum(), DIGITAL_OUTPUT);
		pin.setMode(PWM_OUTPUT);
		pin.setPwm(value);
	}

	@Override
	public void switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		GpioPinDigitalOutput pin = (GpioPinDigitalOutput) getOrCreate(
				digitalPin.pinNum(), DIGITAL_OUTPUT);
		if (value) {
			pin.high();
		} else {
			pin.low();
		}
	}

	@Override
	public void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		throw notSupported();
	}

	@Override
	public void sendTone(Tone tone) throws IOException {
		throw notSupported();
	}

	@Override
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		throw notSupported();
	}

	@Override
	public void sendCustomMessage(String... messages) throws IOException {
		throw notSupported();
	}

	private GpioPin getOrCreate(int address, PinMode pinMode) {
		for (GpioPin gpioPin : gpioController.getProvisionedPins()) {
			com.pi4j.io.gpio.Pin pin = gpioPin.getPin();
			if (pin.getAddress() == address) {
				checkState(pin.getSupportedPinModes().contains(pinMode),
						"Pin %s does not provide %s", pin, pinMode);
				return gpioPin;
			}
		}
		return create(address, pinMode);
	}

	private GpioPin create(int address, PinMode pinMode) {
		String name = "GPIO " + address;
		return gpioController.provisionPin(
				checkNotNull(getPinByName(name),
						"Pin with name %s does not exist", name), pinMode);
	}

	private GpioPinListenerDigital digitalAdapter() {
		return new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent event) {
				if (event.getEventType() == DIGITAL_STATE_CHANGE) {
					fireStateChanged(new DefaultDigitalPinValueChangedEvent(
							digitalPin(event.getPin().getPin().getAddress()),
							event.getState().isHigh()));
				}
			}
		};
	}

	private GpioPinListenerAnalog analogAdapter() {
		return new GpioPinListenerAnalog() {
			@Override
			public void handleGpioPinAnalogValueChangeEvent(
					GpioPinAnalogValueChangeEvent event) {
				if (event.getEventType() == ANALOG_VALUE_CHANGE) {
					fireStateChanged(new DefaultAnalogPinValueChangedEvent(
							analogPin(event.getPin().getPin().getAddress()),
							(int) event.getValue()));
				}
			}
		};
	}

	private static PinMode pi4jInputMode(Pin pin) {
		if (pin.is(ANALOG)) {
			return ANALOG_INPUT;
		} else if (pin.is(DIGITAL)) {
			return DIGITAL_INPUT;
		}
		throw new IllegalStateException("Illegal pin type of pin " + pin);
	}

	private UnsupportedOperationException notSupported() {
		return new UnsupportedOperationException("not supported");
	}

}
