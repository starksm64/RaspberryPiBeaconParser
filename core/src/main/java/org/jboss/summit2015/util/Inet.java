package org.jboss.summit2015.util;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Networking related utils
 */
public class Inet {
    public static String MACADDR_FORMAT = "%02x:%02x:%02x:%02x:%02x:%02x";

    public static class InterfaceConfig {
        private String macaddr;
        private List<InterfaceAddress> addressList;

        public InterfaceConfig(String macaddr, List<InterfaceAddress> addressList) {
            this.macaddr = macaddr;
            this.addressList = addressList;
        }

        public String getMacaddr() {
            return macaddr;
        }

        public List<InterfaceAddress> getAddressList() {
            return addressList;
        }
        public String toString() {
            return String.format("{macaddr=%s, addresses=%s}", macaddr, addressList);
        }
    }

    /**
     * Return a list of all NetworkInterface hardware addresses in %x:%x:%x:%x:%x:%x format
     *
     * @return NetworkInterface hardware addresses
     * @throws SocketException
     */
    public static List<String> getAllHardwareAddress() throws SocketException {
        ArrayList<String> addresses = new ArrayList<>();
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            byte[] macaddr = iface.getHardwareAddress();
            if (macaddr == null)
                continue;
            String key = String.format(MACADDR_FORMAT, macaddr[0], macaddr[1], macaddr[2], macaddr[3], macaddr[4], macaddr[5]);
            addresses.add(key);
        }
        return addresses;
    }

    /**
     * Return a list of all NetworkInterface hardware addresses and addresses
     * @return
     * @throws SocketException
     */
    public static List<InterfaceConfig> getAllAddress() throws SocketException {
        ArrayList<InterfaceConfig> addresses = new ArrayList<>();
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            byte[] macaddr = iface.getHardwareAddress();
            if (macaddr == null)
                continue;
            String key = String.format(MACADDR_FORMAT, macaddr[0], macaddr[1], macaddr[2], macaddr[3], macaddr[4], macaddr[5]);
            List<InterfaceAddress> addressList = iface.getInterfaceAddresses();
            InterfaceConfig ic = new InterfaceConfig(key, addressList);
            addresses.add(ic);
        }
        return addresses;
    }

}
