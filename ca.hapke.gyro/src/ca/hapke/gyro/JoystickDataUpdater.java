package ca.hapke.gyro;

import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1115Pin;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider.ProgrammableGainAmplifierValue;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.i2c.I2CBus;

import ca.hapke.gyro.data.AdcDataType;
import ca.hapke.gyro.data.DataCluster;
import ca.hapke.gyro.data.DataType.InputType;

/**
 * TODO doesn't pull initial values at start
 * 
 * @author Mr. Hapke
 */
public class JoystickDataUpdater {

	private class AnalogPinListener implements GpioPinListenerAnalog {
		private final int pin;

		private AnalogPinListener(int pin) {
			this.pin = pin;
		}

		@Override
		public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent event) {
			double value = event.getValue();

			// percentage
			double percent = ((value * 100) / ADS1115GpioProvider.ADS1115_RANGE_MAX_VALUE);
			status.updateByIndex(pin, (int) percent);
		}

	}

	private static final int UPDATE_FREQUENCY = 50;

	private final GpioController gpio = GpioFactory.getInstance();
	private ADS1115GpioProvider gpioProvider;

	// provision gpio analog input pins from ADS1115
	private GpioPinAnalogInput[] myInputs;
	private AdcDataType status;

	public JoystickDataUpdater(DataCluster cluster) {
		this.status = (AdcDataType) cluster.getData(InputType.ADC);
	}

	public void start() {
		try {
			gpioProvider = new ADS1115GpioProvider(I2CBus.BUS_1, ADS1115GpioProvider.ADS1115_ADDRESS_0x48);
			myInputs = new GpioPinAnalogInput[] {
					gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A0, "MyAnalogInput-A0"),
					gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A1, "MyAnalogInput-A1"),
					gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A2, "MyAnalogInput-A2")
//					, gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A3, "MyAnalogInput-A3") 
			};
			gpioProvider.setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_4_096V, ADS1115Pin.ALL);

			gpioProvider.setEventThreshold(200, ADS1115Pin.ALL);
			// (a value less than 50 ms is not permitted)
			gpioProvider.setMonitorInterval(UPDATE_FREQUENCY);

			for (int i = 0; i < myInputs.length; i++) {
				myInputs[i].addListener(new AnalogPinListener(i));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void kill() {
		gpio.shutdown();
	}

}
