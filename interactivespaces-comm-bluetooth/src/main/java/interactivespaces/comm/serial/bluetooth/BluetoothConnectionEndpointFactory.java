/**
 * 
 */
package interactivespaces.comm.serial.bluetooth;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import com.google.common.collect.Lists;

/**
 * 
 * 
 * @author Keith M. Hughes
 */
public class BluetoothConnectionEndpointFactory {
	public static void main(String[] args) {
		final List<RemoteDevice> devices = Lists.newArrayList();
		
		// BluetoothDeviceDiscovery bluetoothDeviceDiscovery=new
		// BluetoothDeviceDiscovery();
		try {
			final CountDownLatch done = new CountDownLatch(1);

			LocalDevice localDevice = LocalDevice.getLocalDevice();

			System.out.println("Address: " + localDevice.getBluetoothAddress());

			System.out.println("Name: " + localDevice.getFriendlyName());

			DiscoveryAgent agent = localDevice.getDiscoveryAgent();

			System.out.println("Starting device inquiry…");

			boolean blah = agent.startInquiry(DiscoveryAgent.GIAC, new DiscoveryListener() {

				@Override
				public void deviceDiscovered(RemoteDevice device,
						DeviceClass deviceClass) {
					try {
						System.out.println("Device discovered: "
								+ device.getBluetoothAddress());
						System.out.println(device.getFriendlyName(true));
						devices.add(device);
						if (deviceClass.getMajorDeviceClass() == 1280
								&& deviceClass.getMinorDeviceClass() == 4) {
							System.out.println("Is a Wii remote!!");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				@Override
				public void inquiryCompleted(int discType) {
					switch (discType) {

					case DiscoveryListener.INQUIRY_COMPLETED:

						System.out.println("INQUIRY_COMPLETED");

						break;

					case DiscoveryListener.INQUIRY_TERMINATED:

						System.out.println("INQUIRY_TERMINATED");

						break;

					case DiscoveryListener.INQUIRY_ERROR:

						System.out.println("INQUIRY_ERROR");

						break;

					default:

						System.out.println("Unknown Response Code");

						break;

					}
					done.countDown();
				}

				@Override
				public void serviceSearchCompleted(int arg0, int arg1) {
					System.out.println("Service complete");
				}

				@Override
				public void servicesDiscovered(int arg0, ServiceRecord[] arg1) {
					System.out.println("Service discovered");
				}
			});

			System.out.println(blah);
			done.await();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
