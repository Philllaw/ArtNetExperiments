/*
 * Code by Phill
 * https://github.com/Philllaw
 */
package artnetTest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Phill
 */
public class ArtNetReceiver {

    public static int ARTNET_PORT = 6454;
    static int SEQUENCE_OFFSET = 12;
    static int PHYSICAL_OFFSET = 13;
    static int UNIVERSE_OFFSET = 14;
    static int LENGTH_OFFSET = 16;
    static int DATA_OFFSET = 18;
    static int HEADER_LENGTH = 18;

    String listenIP;
    int listenPort;
    List<ArtNetDataListener> artNetDatalisteners;

    DatagramSocket theUDPSocket;
    Thread listenThread;
    Boolean runThread;
    byte[] receiveBuffer = new byte[1000];
    byte[] artNetHeader = {0x41, 0x72, 0x74, 0x2d, 0x4e, 0x65, 0x74, 0x00, 0x00, 0x50, 0x00, 0x0e}; //header is"Art-Net\0" + 0x5000 + 0 + 14 (Protocol Hi + Low)

    public ArtNetReceiver(String listenIP, int listenPort) {
        this.listenIP = listenIP;
        this.listenPort = listenPort;
        artNetDatalisteners = new ArrayList<>();
    }

    public void start() throws UnknownHostException, SocketException {
        InetAddress InetListenIP = InetAddress.getByName(listenIP);
        theUDPSocket = new DatagramSocket(listenPort, InetListenIP);
        theUDPSocket.setReuseAddress(true); //in case port has not closed after last use.
        runThread = true;
        listenThread = new Thread() {
            @Override
            public void run() {
                DatagramPacket receivedPacket;
                while (runThread) {
                    receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    try {
                        theUDPSocket.receive(receivedPacket);
                        InetAddress IPAddress = receivedPacket.getAddress();
                        //System.out.println("recv: " + IPAddress.toString() + ":" + receivedPacket.getPort() + " len: " + receivedPacket.getLength());
                        decodeArtNetDMPacket(receivedPacket);
                    } catch (IOException ex) {
                        System.out.println(ex.toString());

                    }
                }
                theUDPSocket.close();
            }

            private void decodeArtNetDMPacket(DatagramPacket receivedPacket) {
                //check length
                receivedData theData = new receivedData();
                if (receivedPacket.getLength() > 530 || receivedPacket.getLength() < 20) {
                    //System.out.println("Invalid length");
                    return;
                }
                for (int i = 0; i < artNetHeader.length; i++) {
                    if (artNetHeader[i] != receivedPacket.getData()[i]) {
                        //System.out.println("Unknown Header");
                        return;
                    }
                }

                theData.phyPort = receivedPacket.getData()[PHYSICAL_OFFSET];
                //universe is little endian
                theData.universe = ((receivedPacket.getData()[UNIVERSE_OFFSET + 1] & 0xFF) << 8) + (receivedPacket.getData()[UNIVERSE_OFFSET] & 0xFF);
                //datalength is big endian
                int dataLength = ((receivedPacket.getData()[LENGTH_OFFSET] & 0xFF) << 8) + (receivedPacket.getData()[LENGTH_OFFSET + 1] & 0xFF);
                if (dataLength > 512 || dataLength < 2 || (dataLength & 0x01) != 0) { //make sure 2-512 and even
                    //System.out.println("Invalid data length");
                    return;
                }
                if (dataLength + HEADER_LENGTH != receivedPacket.getLength()) {
                    //System.out.println("Claimed length doesn't match packet size.");
                    return;
                }
                theData.data = new int[dataLength];
                for (int i = 0; i < dataLength; i++) {
                    theData.data[i] = receivedPacket.getData()[DATA_OFFSET + i] & 0xFF;
                }
                //System.out.println("packet good");
                notifyArtNetDataListeners(theData);
                
            }
        };
        listenThread.start();
    }

    public void stop() {
        runThread = false;

    }

    public class receivedData {

        int phyPort;
        int universe;
        int[] data;

        public receivedData() {
            phyPort = 0;
            universe = 0;
            data = null;
        }

    }

    public interface ArtNetDataListener {
        void ArtNetDataReceived(receivedData theData);
    }

    public void addArtNetDataListener(ArtNetDataListener thelistener) {
        artNetDatalisteners.add(thelistener);
    }

    void notifyArtNetDataListeners(receivedData theData) {
        for (ArtNetDataListener listener : artNetDatalisteners) {
            listener.ArtNetDataReceived(theData);
        }
    }

}
