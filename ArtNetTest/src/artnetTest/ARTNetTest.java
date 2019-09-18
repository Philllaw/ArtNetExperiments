/*
 * Code by Phill
 * https://github.com/Philllaw
 */
package artnetTest;

/**
 *
 * @author Phill
 */
public class ARTNetTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArtNetSimpleFadersController artNetSimpleFadersController = new ArtNetSimpleFadersController();
        artNetSimpleFadersController.start();
//        
//        int packetsToSend = 40 * 5;
//        try {
//            ArtNetSender sender = new ArtNetSender("192.168.5.102", null, "192.168.5.75", ArtNetSender.ARTNET_PORT);
//            sender.start();
//            for (int i = 0; i < packetsToSend; i++) {
//                byte[] values = {(byte) (i * 255 / 40), (byte) (128 + i * 255 / 40)};
//                sender.setDMXValues(1, values);
//                System.out.println(values[0] + " " + values[1]);
//                wait(25);
//            }
//            sender.stop();
//        } catch (Exception e) { //if there is an error
//            System.out.println(e.getMessage());//print error
//            e.printStackTrace();
//        }
    }

    public static void wait(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
        }
    }
}
