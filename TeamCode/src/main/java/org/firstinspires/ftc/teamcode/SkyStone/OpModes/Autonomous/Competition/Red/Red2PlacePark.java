package org.firstinspires.ftc.teamcode.SkyStone.OpModes.Autonomous.Competition.Red;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Red 2 Place Park")
public class Red2PlacePark extends Red1Place1Deliver{

    @Override
    public void runOpMode(){
        super.runOpMode();
    }

    @Override
    public void depositSecondStoneRight(){
        straightenOut();

        //drive left to go to building zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -9, -9, initialAngle);
        log("drive left after grabbing first stone");

        straightenOut();
        log("straighten out after driving left after collecting first stone");

        //drive to other zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -84,5, initialAngle);
        log("drive to foundation");

        lowerArm();

        releaseStone();

        raiseArm();
    }

    @Override
    public void rightPark(){
        //drives left
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 10, -10, initialAngle);

        //parks
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 30, 0, initialAngle);
    }

    @Override
    public void depositSecondStoneMiddle(){
        straightenOut();

        //drive left to go to building zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -9, -9, initialAngle);
        log("drive left after grabbing first stone");

        straightenOut();
        log("straighten out after driving left after collecting first stone");

        //drive to other zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -92,5, initialAngle);
        log("drive to foundation");

        lowerArm();

        releaseStone();

        raiseArm();
    }

    @Override
    public void middlePark(){
        //drives left
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 10, -10, initialAngle);

        //parks
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 30, 0, initialAngle);
    }

    @Override
    public void depositSecondStoneLeft(){
        straightenOut();

        //drive left to go to building zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -9, -9, initialAngle);
        log("drive left after grabbing first stone");

        straightenOut();
        log("straighten out after driving left after collecting first stone");

        //drive to other zone
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, -100,5, initialAngle);
        log("drive to foundation");

        lowerArm();

        releaseStone();

        raiseArm();
    }

    @Override
    public void leftPark(){
        //drives left
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 10, -10, initialAngle);

        //parks
        base.drivetrain.gyroEncoderDrive(DRIVE_SPEED, 30, 0, initialAngle);
    }

}