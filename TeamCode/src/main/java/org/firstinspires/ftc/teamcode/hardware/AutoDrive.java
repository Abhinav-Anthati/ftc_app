package org.firstinspires.ftc.teamcode.hardware;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.util.Status;

import java.lang.Math;

public class AutoDrive {
    private final Drivetrain drivetrain;
    private final IMU imu;

    private boolean drivetrain_reached;

    private double delta_x; // change in robot x-position (forward/backward)
    private double delta_y; // change in robot y-position (strafing)
    private double delta_a; // change in robot heading (turning)


    private double fl_enc;
    private double fr_enc;
    private double bl_enc;
    private double br_enc;
    private double heading;

    private double fl_enc_was;
    private double fr_enc_was;
    private double bl_enc_was;
    private double br_enc_was;
    private double heading_was;

    private double delta_field_x = 0.0;
    private double delta_field_y = 0.0;

    private double field_x = 0.0;
    private double field_y = 0.0;

    private double target_x;
    private double target_y;
    private double target_a;

    private double error_x;
    private double error_y;
    private double error_a;

    private double old_error_forward;
    private double old_error_strafe;
    private double old_error_a;

    private double d_forward;
    private double d_strafe;
    private double d_turn;

    private double i_sum_forward;
    private double i_sum_strafe;
    private double i_sum_turn;

    private final double strafe_efficiency = 0.9014;
    private final double radians_per_tick = (10 * Math.PI) / (10246); // 10246 was the tick count after we spun the robot 2pi radians 10 times
    private final double inches_per_tick = (1 / Status.TICKS_PER_ROTATION) * (96 * Math.PI / 25.4) * (1 / 15.2);
    private double loop_start = 0.0;
    private double loop_end = 0.0;
    private double loop_time = 0.0;

    public boolean wall_shove = false;

    public AutoDrive(Drivetrain drivetrain, IMU imu){
        this.drivetrain = drivetrain;
        this.imu = imu;
    }

    public boolean ifReached(){
        double deadband = 1;
        double deadband_a = 0.011;
        if (wall_shove) {
            if (Math.abs(error_y) <= deadband) {
                drivetrain_reached = true;
                return true;
            } else {
                return false;
            }
        } else {
            if (!drivetrain_reached && Math.abs(error_x) <= deadband && Math.abs(error_y) <= deadband && Math.abs(error_a) <= deadband_a) {
                drivetrain_reached = true;
                return true;
            } else {
                return false;
            }
        }

    }

    public void getFieldPos() {
        loop_start = System.nanoTime() / 1000000000.0;
        heading = Math.toRadians(imu.getHeading());

        double error;
        double direction;// direction of robot motion

        fl_enc = drivetrain.getEncoderValue(Drivetrain.encoderNames.FRONT_LEFT);
        fr_enc = drivetrain.getEncoderValue(Drivetrain.encoderNames.FRONT_RIGHT);
        bl_enc = drivetrain.getEncoderValue(Drivetrain.encoderNames.BACK_LEFT);
        br_enc = drivetrain.getEncoderValue(Drivetrain.encoderNames.BACK_RIGHT);

        double delta_fl = fl_enc - fl_enc_was;
        double delta_fr = fr_enc - fr_enc_was;
        double delta_bl = bl_enc - bl_enc_was;
        double delta_br = br_enc - br_enc_was;

        // converts encoder values to changes in robot x, y, and a
        delta_x = inches_per_tick * (delta_fl + delta_fr) / 2;
        delta_y = inches_per_tick * strafe_efficiency * (delta_bl - delta_fl) / 2;
        //delta_a = -radians_per_tick * (delta_fr-delta_bl) / 2; // heading based on wheel encoders
        delta_a = heading - heading_was;

        double s = Math.sqrt((delta_x * delta_x) + (delta_y * delta_y)); // arc length, or distance travelled

        // Straight lines method
        direction = heading + Math.atan2(delta_x, delta_y); // arctangent of y/x
        delta_field_x = s * Math.cos(direction);
        delta_field_y = s * Math.sin(direction);

        fl_enc_was = fl_enc;
        fr_enc_was = fr_enc;
        bl_enc_was = bl_enc;
        br_enc_was = br_enc;
        heading_was = heading;

        field_x += delta_field_x;
        field_y += delta_field_y;

        loop_end = System.nanoTime() / 1000000000.0;
        loop_time = loop_end - loop_start;

    }

    public void moveToPosition(double x, double y, double a, double power, boolean tracking) {
        final double power_cap = power; // NOT ACTUALLY CAPPING THE MOTOR POWER! Caps the values of fwd, strafe, and turn

        target_x = x;
        target_y = y;
        target_a = a;
        final double KP = 0.046;
        final double KI = 0.000285;
        final double KD = 0;
        final double KPturn = .6;
        final double KIturn = 0;
        final double KDturn = 0;

        error_x = target_x - field_x;
        error_y = target_y - field_y;
        error_a = target_a - heading;
        double error_d = Math.sqrt((error_x * error_x) + (error_y * error_y)); // distance between target position and actual position
        double theta = Math.atan2(error_x, error_y) - heading; // angle between direction of motion and heading

        error_d = Range.clip(error_d, -1/KP, 1/KP);

        double forward = (error_d * Math.cos(theta));
        double strafe = (error_d * Math.sin(theta));
        double turn = (error_a);

        d_forward = ((forward - old_error_forward) / loop_time) * KD;
        d_strafe = ((strafe - old_error_strafe) / loop_time) * KD;
        d_turn = ((turn - old_error_a) / loop_time) * KDturn;

        old_error_forward = forward;
        old_error_strafe = strafe;
        old_error_a = turn;

        i_sum_forward += forward * KI * loop_time;
        i_sum_strafe += strafe * KI * loop_time;
        i_sum_turn += error_a * KIturn * loop_time;

        i_sum_forward = Range.clip(i_sum_forward, -1, 1);
        i_sum_strafe = Range.clip(i_sum_strafe, -1, 1);
        i_sum_turn = Range.clip(i_sum_turn, -1, 1);

        forward = (forward * KP) + i_sum_forward + d_forward;
        strafe = (strafe * KP) + i_sum_strafe + d_strafe;
        turn = (turn * KPturn) + i_sum_turn + d_turn;

        forward = Range.clip(forward, -power_cap, power_cap);
        strafe = -Range.clip(strafe, -power_cap, power_cap);
        turn = -Range.clip(turn, -power_cap, power_cap);

//        if (wall_shove) {
//            error_x = 0;
//            target_x = 0;
//            strafe = -0.2;
//            field_x = 0;
//        }

        if (ifReached()){
            forward = 0.0;
            strafe = 0.0;
            turn = 0.0;
        }

        if (tracking){
            drivetrain_reached = false;
        }

        drivetrain.move(forward, strafe, turn);
    }

    public void zeroX() {
        field_x = 0;
    }

    public double[] getFieldPositions(){
        return new double[]{field_x, field_y, heading};
    }

    public void update(Telemetry telemetry) {

        getFieldPos();
        /*
        telemetry.addData("encoder FL: ", fl_enc);
        telemetry.addData("encoder FR: ", fr_enc);
        telemetry.addData("encoder BL: ", bl_enc);
        telemetry.addData("encoder BR: ", br_enc);
         */
        telemetry.addData("x Error: ", error_x);
        telemetry.addData("y Error: ", error_y);
        telemetry.addData("a Error: ", error_a);

        //telemetry.addData("delta x: ", delta_x);
        //telemetry.addData("delta y: ", delta_y);

        telemetry.addData("x position: ", field_x);
        telemetry.addData("y position: ", field_y);
        telemetry.addData("angle: ", heading);
        //telemetry.addData("Server status ", server.getStatus());
        //telemetry.addData("Loop Time in seconds", loop_time);
    }
}

