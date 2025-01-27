package org.firstinspires.ftc.teamcode.SkyStone.OpModes.Autonomous.Competition.Blue;

import android.os.Environment;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcontroller.internal.Core.Utility.CustomPhoneCameraSkyStone;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.SkyStone.MainBase;
import org.firstinspires.ftc.teamcode.SkyStone.MainBaseWebcam;
import org.firstinspires.ftc.teamcode.SkyStone.OpModes.Autonomous.Competition.SkystoneDetector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;


@Autonomous(name = "Blue 1 Place 1 Delivery Park", group = "Autonomous")
public class Blue1Place1Deliver extends LinearOpMode {

    public final static double DRIVE_SPEED = 1.0;
    public static final double MAX_TURN_SPEED = 0.5;
    public final static double MINIMUM_TURN_SPEED = 0.1;
    public final static double DISTANCE_ADJUSTMENT_SPEED = 0.15;

    public static final double COLLECTING_DISTANCE = 3;

    public static final double RIGHT_BRIDGE_DISTANCE = 56;
    public static final double MIDDLE_BRIDGE_DISTANCE = 48;
    public static final double LEFT_BRIDGE_DISTANCE =  40;

    public static final double RIGHT_FIRST_DISTANCE_TO_WALL = 23;
    public static final double RIGHT_SECOND_DISTANCE_TO_WALL = 3.4;

    public static final double MIDDLE_FIRST_DISTANCE_TO_WALL = 29.86;
    public static final double MIDDLE_SECOND_DISTANCE_TO_WALL = 9.97;

    public static final double LEFT_FIRST_DISTANCE_TO_WALL = 37.81;
    public static final double LEFT_SECOND_DISTANCE_TO_WALL = 16.12;

    public static final double AROUND_GATE_DISTANCE = 13;
    public static final double RIGHT_PARKING_DISTANCE = 5;
    public static final double PARKING_DISTANCE = 15;


    private CustomPhoneCameraSkyStone.SkyStonePosition position = CustomPhoneCameraSkyStone.SkyStonePosition.UNKNOWN;
    private SkystoneDetector detector;
    public MainBase base;

    public int initialAngle;

    private File angleFile;
    private PrintWriter angleWriter;

    private File logFile;
    private PrintWriter logWriter;

    @Override
    public void runOpMode(){

        base = new MainBase(hardwareMap,telemetry,this);
        base.init();

        base.arms.shutRightClamp();
        base.arms.openLeftClamp();

        detector = new SkystoneDetector(this, true, false);

        base.drivetrain.setInitalAngle(0);
        base.gyro.gyro.resetZAxisIntegrator();
        initialAngle = base.gyro.gyro.getIntegratedZValue();

        try{
            angleFile = new File(Environment.getExternalStorageDirectory(), "angle");
            if (angleFile.exists()){
                angleFile.delete();
            }
            angleFile.createNewFile();
            angleWriter = new PrintWriter(new BufferedWriter(new FileWriter(angleFile, false)));

            logFile = new File(Environment.getExternalStorageDirectory(), "log");
            logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
        }
        catch(Exception e){
            telemetry.addLine(e.getMessage());
        }

        telemetry.clearAll();
        telemetry.addLine("May the Force be with us");
        telemetry.update();

        waitForStart();

        base.drivetrain.setInitalAngle(0);
        base.gyro.gyro.resetZAxisIntegrator();
        initialAngle = base.gyro.gyro.getIntegratedZValue();

        position = detector.getDecision();

        telemetry.addData("position is ", position);
        telemetry.update();

        Calendar c = Calendar.getInstance();
        logWriter.println("NEW BLUE " + position + " " + c.getTime());
        log("initial position");

        if (position == CustomPhoneCameraSkyStone.SkyStonePosition.UNKNOWN){
            position = CustomPhoneCameraSkyStone.SkyStonePosition.LEFT;
        }

        switch(position){
            case LEFT:

                getFirstStoneLeft();

                depositFirstStoneLeft();

                getSecondStoneLeft();

                depositSecondStoneLeft();

                getThirdStoneLeft();

                depositThirdStoneLeft();

                leftPark();

                break;

            case MIDDLE:

                getFirstStoneMiddle();

                depositFirstStoneMiddle();

                getSecondStoneMiddle();

                depositSecondStoneMiddle();

                getThirdStoneMiddle();

                depositThirdStoneMiddle();

                middlePark();

                break;

            case RIGHT:

                getFirstStoneRight();

                depositFirstStoneRight();

                getSecondStoneRight();

                depositSecondStoneRight();

                getThirdStoneRight();

                depositThirdStoneRight();

                rightPark();

                break;
        }

        base.drivetrain.setPowers(0);

        writeAngle();

        logWriter.close();
        detector.stopStreaming();
    }

    private void writeAngle(){
        try{
            int angle = base.gyro.heading();
            while (angle > 360){
                angle -= 360;
            }
            while (angle < 0){
                angle += 360;
            }
            angleWriter.println(angle);
            angleWriter.close();
        }
        catch(Exception e){
            telemetry.addLine("problem with i/o");
            telemetry.update();
            e.printStackTrace();
        }

    }

    public void log(String position){
        logWriter.println("position " + position);
        logWriter.println("heading " + base.gyro.heading());
        logWriter.println("frontRightEncoders " + base.drivetrain.frontRight.getCurrentPosition());
        logWriter.println("frontLeftEncoders " + base.drivetrain.frontLeft.getCurrentPosition());
        logWriter.println("backLeftEncoders " + base.drivetrain.backLeft.getCurrentPosition());
        logWriter.println("backRightEncoders " + base.drivetrain.backRight.getCurrentPosition());
        logWriter.println("frontRange " + base.frontRange.customDistanceInInches());
        logWriter.println("leftRange " + base.leftRange.customDistanceInInches());
    }


    public void straightenOut(){
        base.drivetrain.gyroTurn(0.35, 0.5, 0, 2);
        telemetry.addData("Angle is ", base.gyro.heading());
        telemetry.update();
    }

    public void frontRangeDriveToDistance(double distance){
        double error = Math.abs(base.frontRange.customDistanceInInches() - distance);
        double buffer = 0.8;
        if (base.frontRange.distance(DistanceUnit.INCH) < 0 || error > 8){
            return;
        }
        if (error > buffer){
            if (base.frontRange.customDistanceInInches() > distance){
                while (error > buffer){
                    base.drivetrain.setPowers(DISTANCE_ADJUSTMENT_SPEED);
                    error = Math.abs(base.frontRange.customDistanceInInches() - distance);
                }
            }
            else if (base.frontRange.customDistanceInInches() < distance){
                while (error > buffer){
                    base.drivetrain.setPowers(-DISTANCE_ADJUSTMENT_SPEED);
                    error =  Math.abs(base.frontRange.customDistanceInInches() - distance);
                }
            }
        }
        base.drivetrain.stop();
    }

    public void leftRangeDriveToDistance(double distance){
        double error = Math.abs(base.leftRange.customDistanceInInches() - distance);
        double buffer = 0.8;
        if (base.leftRange.distance(DistanceUnit.INCH) < 0 || error > 8){
            return;
        }
        if (error > buffer){
            if (base.leftRange.customDistanceInInches() > distance){
                while (error > buffer){
                    base.drivetrain.frontRight.setPower(DISTANCE_ADJUSTMENT_SPEED);
                    base.drivetrain.frontLeft.setPower(-DISTANCE_ADJUSTMENT_SPEED);
                    base.drivetrain.backLeft.setPower(DISTANCE_ADJUSTMENT_SPEED);
                    base.drivetrain.backRight.setPower(-DISTANCE_ADJUSTMENT_SPEED);
                    error = Math.abs(base.leftRange.customDistanceInInches() - distance);
                }
            }
        }
        base.drivetrain.stop();
    }

    public void lowerArm(){
        base.arms.lowerLeftArm();
    }

    public void lowerArmSleep(){
        lowerArm();
        sleep(250);
    }

    public void raiseArm(){
        base.arms.raiseLeftArm();
    }

    public void grabStone(){
        base.arms.clampLeftClamp();
        sleep(600);
    }

    public void releaseStone(){
        base.arms.openLeftClamp();
    }

    public void getFirstStoneLeft(){
        lowerArm();

        //drives back and left after seeing stones
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -9.5, -24.7, initialAngle);
        log("left to first stone with encoders");

        straightenOut();
        log("left straighten after near first stone");

        //drives to specific distance from both walls
        frontRangeDriveToDistance(LEFT_FIRST_DISTANCE_TO_WALL);
        log("left adjust distance to first stone with range sensor");

        grabStone();

        raiseArm();
    }

    public void depositFirstStoneLeft(){
        //drive right to go to building zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -7, 7, initialAngle);
        log("left strafe right after grabbing first stone");

        straightenOut();
        log("left straighten out after strafing right with first stone");

        //drive to other zone
        base.drivetrain.gyroEncoderDriveCoefficient(DRIVE_SPEED, -62, -8, initialAngle);
        log("left ready to deposit stone");

        lowerArmSleep();

        releaseStone();

        raiseArm();
    }

    public void getSecondStoneLeft(){
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 7.5, 7.5, initialAngle);
        log("left drive forward after depositing first");

        base.arms.shutLeftClamp();

        straightenOut();
        log("left straighten after strafing after depositing first");

        //drives to second stone
        base.drivetrain.gyroEncoderDriveCoefficient(DRIVE_SPEED, 82, 0, initialAngle);
        log("left to other zone second stone with encoders");

        base.arms.openLeftClamp();

        lowerArm();

        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 7, -7, initialAngle);
        log("left next to second stone encoders");

        straightenOut();
        log("left straighten near second stone");

        //drives to distance from both walls
        frontRangeDriveToDistance(LEFT_SECOND_DISTANCE_TO_WALL);
        leftRangeDriveToDistance(COLLECTING_DISTANCE);
        log("left second stone adjustment with sensors");

        grabStone();

        raiseArm();
    }

    public void depositSecondStoneLeft(){
        //drives right to go to building zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -9, 9, initialAngle);
        log("left strafe after grabbing second stone");

        straightenOut();
        log("left straighten after strafe after grabbing second stone");

        //drives back to go to other zone
        base.drivetrain.gyroEncoderDriveCoefficient(DRIVE_SPEED, -53, 0, initialAngle);
        log("left depositing second stone");
    }

    public void getThirdStoneLeft(){ }

    public void depositThirdStoneLeft(){

    }

    public void leftPark(){
        base.drivetrain.gyroTurn(MINIMUM_TURN_SPEED, MAX_TURN_SPEED, 20, 3);
        log("left turning to park");

        lowerArm();

        releaseStone();

        raiseArm();

        sleep(250);
        base.arms.shutLeftClamp();

        //park
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 17, 0, base.gyro.gyro.getIntegratedZValue());
        log("left final park");
    }

    public void getFirstStoneMiddle(){
        lowerArm();

        //drives left after seeing stones
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 0.2, -24.6, initialAngle);
        log("middle encoder near first stone");

        straightenOut();
        log("middle straighten after encoder first stone");

        //drives to specific distance from both walls
        frontRangeDriveToDistance(MIDDLE_FIRST_DISTANCE_TO_WALL);
        leftRangeDriveToDistance(COLLECTING_DISTANCE);
        log("middle sensor adjustment first stone");

        grabStone();

        raiseArm();
    }

    public void depositFirstStoneMiddle(){
        //drive right to go to building zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -7, 7, initialAngle);
        log("drive right after grabbing first stone");

        straightenOut();
        log("straighten out after strafe right after first stone");

        //drive to other zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -68, -8, initialAngle);
        log("drove to foundation with first stone");

        lowerArmSleep();

        releaseStone();

        raiseArm();
    }

    public void getSecondStoneMiddle(){
        straightenOut();
        log("straighten out after delivering first stone");

        //strafes right towards wall
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED,10,10, initialAngle);
        log("small drive to right after delivering first stone");

        base.arms.shutLeftClamp();

        //drives to second stone
        base.drivetrain.gyroEncoderDriveCoefficient(DRIVE_SPEED, 86.5, 0, initialAngle);
        log("drove to second stone before lowering arm");

        base.arms.openLeftClamp();

        lowerArm();

        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 6.25, -6.25, initialAngle);
        log("near second stone with encoders");

        straightenOut();
        log("straighten after near second stone");

        //drives to distance from both walls
        frontRangeDriveToDistance(MIDDLE_SECOND_DISTANCE_TO_WALL);
        leftRangeDriveToDistance(COLLECTING_DISTANCE);
        log("sensor adjustment near second stone");

        grabStone();

        raiseArm();
    }

    public void depositSecondStoneMiddle(){
        //drives right to go to building zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -9, 9, initialAngle);
        log("drive right after collecting second stone");

        straightenOut();
        log("straighten out after collecting second stone");

        //drives back to go to other zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -62, 0, initialAngle);
        log("drove to second stone releasing site");
    }

    public void getThirdStoneMiddle(){}

    public void depositThirdStoneMiddle(){

    }

    public void middlePark(){
        //turns to face parking location
        base.drivetrain.gyroTurn(MINIMUM_TURN_SPEED, MAX_TURN_SPEED, 20, 3);
        log("turned for parking");

        lowerArm();

        releaseStone();

        raiseArm();

        sleep(250);
        base.arms.shutLeftClamp();

        //park
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 12, 0, base.gyro.gyro.getIntegratedZValue());
        log("final parking location");
    }

    public void getFirstStoneRight(){
        lowerArm();

        //drives forward after seeing stones
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 8.3, -23.5, initialAngle);
        log("next to first stone encoders");

        straightenOut();
        log("straighten out after first stone encoders");

        //drives to specific distance from both walls
        frontRangeDriveToDistance(RIGHT_FIRST_DISTANCE_TO_WALL);
        leftRangeDriveToDistance(COLLECTING_DISTANCE);
        log("sensor adjustment first stone");

        grabStone();

        raiseArm();
    }

    public void depositFirstStoneRight(){
        //drive right to go to building zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -7, 7, initialAngle);
        log("drive right after collecting first stone");

        straightenOut();

        //drive to other zone
        base.drivetrain.gyroEncoderDriveCoefficient(DRIVE_SPEED, -76, -7.5, initialAngle);
        log("drive next to foundation");

        lowerArmSleep();

        releaseStone();

        raiseArm();
    }

    public void getSecondStoneRight(){
        //strafe right towards wall
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED,6.5,6.5, initialAngle);
        log("drive right after depositing first stone");

        straightenOut();
        log("straighten out after driving right after depositing first stone");

        base.arms.shutLeftClamp();

        //drives to second stone
        base.drivetrain.gyroEncoderDriveCoefficient(DRIVE_SPEED, 96, 0, initialAngle);
        log("drive forward to second stone encoders");

        base.arms.openLeftClamp();

        lowerArm();

        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 7.5, -7.5, initialAngle);
        log("drive next to second stone encoders");

        straightenOut();
        log("straighten out after next to second stone encoders");

        //drives to distance from both walls
        frontRangeDriveToDistance(RIGHT_SECOND_DISTANCE_TO_WALL);
        leftRangeDriveToDistance(COLLECTING_DISTANCE);
        log("sensor adjustment for second stone");

        grabStone();

        raiseArm();
    }

    public void depositSecondStoneRight(){
        //drives right to go to building zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -9, 9, initialAngle);
        log("drives right after collecting second stone");

        straightenOut();

        //drives back to go to other zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -72, 0, initialAngle);
        log("drive to deposit second stone");
    }

    public void getThirdStoneRight(){ }

    public void depositThirdStoneRight(){

    }

    public void rightPark(){
        //turns to face parking location
        base.drivetrain.gyroTurn(MINIMUM_TURN_SPEED, MAX_TURN_SPEED, 20 ,3);
        log("turn to face parking location");

        lowerArm();

        releaseStone();

        raiseArm();

        sleep(250);
        base.arms.shutLeftClamp();

        //park
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 13, 0, base.gyro.gyro.getIntegratedZValue());
        log("parking position");
    }
}
