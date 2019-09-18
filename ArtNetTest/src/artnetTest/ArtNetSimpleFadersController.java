/*
 * Code by Phill
 * https://github.com/Philllaw
 */
package artnetTest;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author Phill
 */
public class ArtNetSimpleFadersController {

    static int CHANNELS = 512;
    static int PID_LOOP_TIME_MS = 5;
    ArtNetSimpleFaders sliderFrame;
    ArtNetSender artNetSender;
    ArtNetReceiver artNetReceiver;

    float[] targetValues = new float[CHANNELS];
    float[] currentValues = new float[CHANNELS];
    float[] errorPreviousValues = new float[CHANNELS];
    float[] errorIntegralValues = new float[CHANNELS];

    float pValue = 1;
    float iValue = 0;
    float dValue = 0;

    boolean runPIDThread = false;
    Thread PIDThread;

    public ArtNetSimpleFadersController() {
        //init values;
        for (int i = 0; i < CHANNELS; i++) {
            targetValues[i] = 0;
            currentValues[i] = 0;
            errorPreviousValues[i] = 0;
            errorIntegralValues[i] = 0;
        }

        sliderFrame = new ArtNetSimpleFaders();

        artNetSender = new ArtNetSender("192.168.5.102", null, "192.168.5.75", ArtNetSender.ARTNET_PORT);
        artNetReceiver = new ArtNetReceiver("127.0.0.1", ArtNetReceiver.ARTNET_PORT);

        sliderFrame.jTextFieldPValue.setText("" + pValue);
        sliderFrame.jTextFieldIValue.setText("" + iValue);
        sliderFrame.jTextFieldDValue.setText("" + dValue);

        PIDThread = new Thread() {
            @Override
            public void run() {
                while (runPIDThread) {
                    //PID loop
                    for (int i = 0; i < CHANNELS; i++) {
                        float error = targetValues[i] - currentValues[i];
                        errorIntegralValues[i] += error * (float) PID_LOOP_TIME_MS;
                        float derivativeError = (error - errorPreviousValues[i]) / (float) PID_LOOP_TIME_MS;
                        errorPreviousValues[i] = error;
                        currentValues[i] += pValue * error + iValue * errorIntegralValues[i] + dValue * derivativeError;
                        if (i == 0) {
                            sliderFrame.jTextActual.setText("" + currentValues[i]);
                        }
                        //System.out.println("e: " + error + " de: " + derivativeError + " isum: " + errorIntegralValues[i] + " p " + pValue + " i " + iValue + " d " + dValue);
                        //artNetSender.setDMXValue(i + 1, (int) currentValues[i]);
                    }

                    try {
                        Thread.sleep(PID_LOOP_TIME_MS);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        };
        addlisteners(sliderFrame);
    }

    private void addlisteners(ArtNetSimpleFaders sliderFrame) {
        sliderFrame.jTextFieldPValue.addActionListener((ActionEvent e) -> {
            pValue = Float.valueOf(sliderFrame.jTextFieldPValue.getText());
        });
        sliderFrame.jTextFieldIValue.addActionListener((ActionEvent e) -> {
            iValue = Float.valueOf(sliderFrame.jTextFieldIValue.getText());
        });
        sliderFrame.jTextFieldDValue.addActionListener((ActionEvent e) -> {
            dValue = Float.valueOf(sliderFrame.jTextFieldDValue.getText());
        });
        sliderFrame.jSlider1.addChangeListener((ChangeEvent e) -> {
            sliderFrame.JtextTarget.setText("" + sliderFrame.jSlider1.getValue());
            targetValues[0] = sliderFrame.jSlider1.getValue();
            //artNetSender.setDMXValue(1, sliderFrame.jSlider1.getValue());
        });
    }

    void start() {
        //start art net sender
        try {
            artNetSender.start();
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        try {
            artNetReceiver.start();
            artNetReceiver.addArtNetDataListener((ArtNetReceiver.receivedData theData) -> {
                for (int i = 0; i < theData.data.length; i++) {
                    artNetSender.setDMXValue(i + 1, theData.data[i]);
                }
            });
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        //start output thread
        runPIDThread = true;
        PIDThread.start();
        sliderFrame.setVisible(true);
    }

    void stop() {
        runPIDThread = false;
        artNetSender.stop();
        artNetReceiver.stop();
    }
}
