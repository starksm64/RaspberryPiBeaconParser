RaspberryPiBeaconParser
=======================

Repository for BLE beacon scanning on raspberry pi using Java. See the native repository for the much more efficient version of the beacon scanner: [NativeRaspberryPiBeaconParser](https://github.com/starksm64/NativeRaspberryPiBeaconParser)


# Sample Command Lines
To run the parser on a host while pulling the hcidump raw output over an ssh connection:

	org.jboss.summit2015.beacon.HCIDumpParser -scannerID PiScanner1 stream-ssh -host 192.168.1.95 -sshUsername root -sshPassword root0FPi 
	
