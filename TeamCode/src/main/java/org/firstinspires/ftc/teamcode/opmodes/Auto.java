package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
<<<<<<< Updated upstream
import org.firstinspires.ftc.teamcode.hardware.Robot;
=======
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.hardware.Capper;
import org.firstinspires.ftc.teamcode.hardware.Drivetrain;
import org.firstinspires.ftc.teamcode.hardware.Duck;
import org.firstinspires.ftc.teamcode.hardware.Intake;
import org.firstinspires.ftc.teamcode.hardware.Lift;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.util.Status;
import org.firstinspires.ftc.teamcode.util.Storage;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes

@Autonomous(name = "Auto")
public class Auto extends LoggingOpMode{

<<<<<<< Updated upstream
    public Robot robot;

    @Override
    public void init() {
        super.init();
        robot = Robot.initialize(hardwareMap);
    }
=======
    private Robot robot;
    private Drivetrain drivetrain;
    private Lift lift;
    private Intake intake;
    private Duck duck;
    private Capper capper;

    private int id = 0;

    private ElapsedTime timer;

//    Drivetrain Variables
    private double HEADING_CORRECTION_kP;
    private double HEADING_CORRECTION_TARGET_BASED;

//    Lift Variables
    private double PITSTOP;

//    Intake Variables
    private double HOLD_TIME;
    private double CLOSE_CLAW_FREIGHT;
    private double OPEN_CLAW;

//    Duck Variables
    private double spinner_speed = 0.0;
    private boolean stop_duck_spin = false;
>>>>>>> Stashed changes


    public void init() {
        robot = new Robot(hardwareMap);

        this.drivetrain = robot.drivetrain;
        this.lift = robot.lift;
        this.intake = robot.intake;
        this.duck = robot.duck;
        this.capper = robot.capper;

        timer = new ElapsedTime();
        HEADING_CORRECTION_kP = Storage.getJsonValue("heading_correction_kp");
        HEADING_CORRECTION_TARGET_BASED = Storage.getJsonValue("heading_correction_target_based");

        PITSTOP = Storage.getJsonValue("pitstop");

        HOLD_TIME = Storage.getJsonValue("hold_time");
        CLOSE_CLAW_FREIGHT = Storage.getJsonValue("close_claw_freight");
        OPEN_CLAW = Storage.getJsonValue("open_claw");

        capper.init();
        super.init();
    }

    @Override
    public void init_loop() {
        lift.resetLift();
        super.init_loop();
    }


    public void init() {
        robot = new Robot(hardwareMap);

        this.drivetrain = robot.drivetrain;
        this.lift = robot.lift;
        this.intake = robot.intake;
        this.duck = robot.duck;
        this.capper = robot.capper;

        timer = new ElapsedTime();
        HEADING_CORRECTION_kP = Storage.getJsonValue("heading_correction_kp");
        HEADING_CORRECTION_TARGET_BASED = Storage.getJsonValue("heading_correction_target_based");

        PITSTOP = Storage.getJsonValue("pitstop");

        HOLD_TIME = Storage.getJsonValue("hold_time");
        CLOSE_CLAW_FREIGHT = Storage.getJsonValue("close_claw_freight");
        OPEN_CLAW = Storage.getJsonValue("open_claw");

        capper.init();
        super.init();
    }

    @Override
    public void init_loop() {
        lift.resetLift();
        super.init_loop();
    }


    public void init() {
        robot = new Robot(hardwareMap);

        this.drivetrain = robot.drivetrain;
        this.lift = robot.lift;
        this.intake = robot.intake;
        this.duck = robot.duck;
        this.capper = robot.capper;

        timer = new ElapsedTime();
        HEADING_CORRECTION_kP = Storage.getJsonValue("heading_correction_kp");
        HEADING_CORRECTION_TARGET_BASED = Storage.getJsonValue("heading_correction_target_based");

        PITSTOP = Storage.getJsonValue("pitstop");

        HOLD_TIME = Storage.getJsonValue("hold_time");
        CLOSE_CLAW_FREIGHT = Storage.getJsonValue("close_claw_freight");
        OPEN_CLAW = Storage.getJsonValue("open_claw");

        capper.init();
        super.init();
    }

    @Override
    public void init_loop() {
        lift.resetLift();
        super.init_loop();
    }

    @Override
    public void loop() {
<<<<<<< Updated upstream
        robot.capDetector.redCapstoneDetection();
        telemetry.addData("Right Cap", robot.capDetector.getRightDistance());
        telemetry.addData("Middle Cap", robot.capDetector.getMiddleDistance());
        telemetry.update();
    }
}
=======
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
                    timer.reset();
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
                duck_spin();
                break;

        }
    }
    public void duck_spin() {
        stop_duck_spin = timer.seconds() >= 2;
        if (stop_duck_spin) {
            spinner_speed = 0.0;
            id += 1;
        }
        else{
            spinner_speed = timer.seconds() / 1.2;
        }
        duck.spin(spinner_speed);

    }
}
>>>>>>> Stashed changes
