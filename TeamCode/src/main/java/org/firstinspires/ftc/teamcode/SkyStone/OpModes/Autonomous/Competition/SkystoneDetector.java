package org.firstinspires.ftc.teamcode.SkyStone.OpModes.Autonomous.Competition;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcontroller.internal.Core.Utility.CustomPhoneCameraSkyStone;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;
import org.openftc.easyopencv.OpenCvPipeline;

public class SkystoneDetector {

    OpMode opMode;
    OpenCvCamera camera;

    CustomPipeline pipeline;

    private final Point BLUE_LEFT_TL = new Point(90,90);
    private final Point BLUE_LEFT_BR = new Point(140, 120);
    private final Point BLUE_MIDDLE_TL = new Point(185, 90);
    private final Point BLUE_MIDDLE_BR = new Point(235,  120);
    private final Point BLUE_RIGHT_TL = new Point(265, 90);
    private final Point BLUE_RIGHT_BR = new Point(315, 120);

    private final Point RED_LEFT_TL = new Point(0,135);
    private final Point RED_LEFT_BR = new Point(50, 165);
    private final Point RED_MIDDLE_TL = new Point(62, 140);
    private final Point RED_MIDDLE_BR = new Point(125,  170);
    private final Point RED_RIGHT_TL = new Point(155, 140);
    private final Point RED_RIGHT_BR = new Point(215, 170);

    private Point leftTL;
    private Point leftBR;
    private Point middleTL;
    private Point middleBR;
    private Point rightTL;
    private Point rightBR;

    private RGBColor left;
    private RGBColor middle;
    private RGBColor right;

    public SkystoneDetector(OpMode op, boolean useWebcam, boolean isRed){

        opMode = op;

        int cameraMonitorViewId = opMode.hardwareMap.appContext.getResources().getIdentifier(
                "cameraMonitorViewId", "id", opMode.hardwareMap.appContext.getPackageName());

        if (useWebcam){
            camera = OpenCvCameraFactory.getInstance().createWebcam(opMode.hardwareMap.get(WebcamName.class, "Webcam"), cameraMonitorViewId);
        }
        else{
            camera = OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        }

        pipeline = new CustomPipeline();
        camera.openCameraDevice();
        camera.setPipeline(pipeline);
        camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);

        leftTL = (isRed) ? RED_LEFT_TL : BLUE_LEFT_TL;
        leftBR = (isRed) ? RED_LEFT_BR : BLUE_LEFT_BR;
        middleTL = (isRed) ? RED_MIDDLE_TL : BLUE_MIDDLE_TL;
        middleBR = (isRed) ? RED_MIDDLE_BR : BLUE_MIDDLE_BR;
        rightTL = (isRed) ? RED_RIGHT_TL : BLUE_RIGHT_TL;
        rightBR = (isRed) ? RED_RIGHT_BR : BLUE_RIGHT_BR;
    }

    public void stopStreaming(){
        camera.stopStreaming();
    }

    public CustomPhoneCameraSkyStone.SkyStonePosition getDecision(){
        int leftValue = left.getValue();
        int middleValue = middle.getValue();
        int rightValue = right.getValue();

        if (leftValue < middleValue && leftValue < rightValue){
            return CustomPhoneCameraSkyStone.SkyStonePosition.LEFT;
        }
        if (middleValue < leftValue && middleValue < rightValue){
            return CustomPhoneCameraSkyStone.SkyStonePosition.MIDDLE;
        }
        if (rightValue < leftValue && rightValue < middleValue){
            return CustomPhoneCameraSkyStone.SkyStonePosition.RIGHT;
        }
        return CustomPhoneCameraSkyStone.SkyStonePosition.UNKNOWN;
    }

    class CustomPipeline extends OpenCvPipeline {

        @Override
        public Mat processFrame(Mat input){

            left = getAverageColor(input, leftTL, leftBR);
            middle = getAverageColor(input, middleTL, middleBR);
            right = getAverageColor(input, rightTL, rightBR);

            int thickness = 4;
            Scalar leftColor = new Scalar(255,0,0);
            Scalar middleColor = new Scalar(255,0,0);
            Scalar rightColor = new Scalar(255,0,0);
            CustomPhoneCameraSkyStone.SkyStonePosition position = getDecision();
            if (position == CustomPhoneCameraSkyStone.SkyStonePosition.LEFT){
                leftColor = new Scalar(0,255,0);
            }
            else if (position == CustomPhoneCameraSkyStone.SkyStonePosition.MIDDLE){
                middleColor = new Scalar(0,255,0);
            }
            else if (position == CustomPhoneCameraSkyStone.SkyStonePosition.RIGHT){
                rightColor = new Scalar(0,255,0);
            }

            Imgproc.rectangle(input, leftTL, leftBR, leftColor, thickness);
            Imgproc.rectangle(input, middleTL, middleBR, middleColor, thickness);
            Imgproc.rectangle(input, rightTL, rightBR, rightColor, thickness);

            //sendTelemetry();

            return input;
        }

        private RGBColor getAverageColor(Mat mat, Point topLeft, Point bottomRight){
            int red = 0;
            int green = 0;
            int blue = 0;
            int total = 0;

            for (int x = (int)topLeft.x; x < bottomRight.x; x++){
                for (int y = (int)topLeft.y; y < bottomRight.y; y++){
                    red += mat.get(y,x)[0];
                    green += mat.get(y,x)[1];
                    blue += mat.get(y,x)[2];
                    total++;
                }
            }

            red /= total;
            green /= total;
            blue /= total;

            return new RGBColor(red, green, blue);
        }

        private void sendTelemetry(){
            opMode.telemetry.addLine("Left :" + " R " + left.getRed() + " G " + left.getGreen() + " B " + left.getBlue());
            opMode.telemetry.addLine("Middle :" + " R " + middle.getRed() + " G " + middle.getGreen() + " B " + middle.getBlue());
            opMode.telemetry.addLine("Right :" + " R " + right.getRed() + " G " + right.getGreen() + " B " + right.getBlue());
            opMode.telemetry.update();
        }

    }

}
