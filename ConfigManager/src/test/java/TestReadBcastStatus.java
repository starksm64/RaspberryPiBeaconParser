import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by starksm on 6/17/15.
 */
public class TestReadBcastStatus {
    public static void main(String[] args) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            byte[] data = receivePacket.getData();
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            InetAddress addr = receivePacket.getAddress();
            int port = receivePacket.getPort();
            int count = dis.readInt();
            System.out.printf("Received buffer with %d properties, from: %s\n", count, addr);
            for (int n = 0; n < count; n++) {
                int length = dis.readInt();
                byte[] strBytes = new byte[length];
                dis.readFully(strBytes);
                String name = new String(strBytes);
                length = dis.readInt();
                strBytes = new byte[length];
                dis.readFully(strBytes);
                String value = new String(strBytes);
                System.out.printf("%s=%s\n", name, value);
            }
        }
    }
}
