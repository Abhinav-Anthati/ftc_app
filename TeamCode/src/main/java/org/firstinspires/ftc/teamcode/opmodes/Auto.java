package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.hardware.Drivetrain;
import org.firstinspires.ftc.teamcode.hardware.Intake;
import org.firstinspires.ftc.teamcode.hardware.Lift;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.util.Status;

@Autonomous(name = "Auto")
public class Auto extends LoggingOpMode{

    private Robot robot;
    private Drivetrain drivetrain;
    private Lift lift;
    private Intake intake;
    private int id = 0;

    private double forward;
    private double strafe;
    private double turn;

    private ElapsedTime timer;

    private double raise_pos;
    private double rotate_pos;
    @Override
    public void loop() {
        switch(id) {

            case 0:
                if (lift.getLiftPosition() == 0) {
                    id += 1;
                }
                break;
            case 1:
                intake.setPower(1);
                if (intake.freightDetected()){
                    id += 1;
                    timer.reset();
                }
                break;
            case 2:
                intake.setPower(-1);
                if (timer.seconds() >= 0.7) {
                    id += 1;
                    intake.stop();
                }
                break;
//            case 3:
//                lift.raise(raise_pos);
//                if (lift.liftReached()) {
//                    id += 1;
//                }
//                break;
//            case 4:
//                lift.rotate(rotate_pos);
//                if (lift.pivotReached()) {
//                    id += 1;
//                }
//                break;
            case 3:

                break;

        }
    }
}
