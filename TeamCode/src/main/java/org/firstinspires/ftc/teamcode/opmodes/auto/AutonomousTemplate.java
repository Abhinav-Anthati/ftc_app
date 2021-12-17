package org.firstinspires.ftc.teamcode.opmodes.auto;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.hardware.Drivetrain;
import org.firstinspires.ftc.teamcode.hardware.Duck;
import org.firstinspires.ftc.teamcode.hardware.Intake;
import org.firstinspires.ftc.teamcode.hardware.Lift;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.input.ControllerMap;
import org.firstinspires.ftc.teamcode.opmodes.teleop.ControlMgr;
import org.firstinspires.ftc.teamcode.util.Logger;
import org.firstinspires.ftc.teamcode.util.Status;
import org.firstinspires.ftc.teamcode.util.websocket.InetSocketServer;
import org.firstinspires.ftc.teamcode.util.websocket.Server;
import org.firstinspires.ftc.teamcode.vision.CapstoneDetector;
import org.firstinspires.ftc.teamcode.vision.webcam.Webcam;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AutonomousTemplate {
    String name;
    private Robot robot;
    private Server server;
    private Drivetrain drivetrain;
    private Intake intake;
    private Duck duck;
    private Lift lift;

    private ControllerMap controller_map;
    private ControlMgr control_mgr;
    private Telemetry telemetry;
    public Logger logger;

    private Webcam webcam;
    private Webcam.SimpleFrameHandler frame_handler;
    private final String WEBCAM_SERIAL = "3522DE6F";
    private Mat detector_frame = new Mat();
    private Mat send_frame = new Mat();

    private ElapsedTime camera_timer;
    public ElapsedTime timer;
    private int id = 0;
    private double timer_delay = 1000; // Set high to not trigger next move
    private boolean waiting_camera = false;
    private boolean waiting = false;
    public int shipping_height = 0;
    public int x_coord = -1;

    static
    {
        OpenCVLoader.initDebug();
    }

    public AutonomousTemplate(String name, Robot robot, HardwareMap hardware_map, ControllerMap controller_map, Telemetry telemetry){
        this.name = name;
        this.robot = Robot.initialize(hardware_map, "Autonomous");
        this.logger = new Logger(name);
        this.telemetry = telemetry;
        this.controller_map = controller_map;
        this.control_mgr = new ControlMgr(robot, this.controller_map);
        this.timer = new ElapsedTime();

        drivetrain = robot.drivetrain;
        duck = robot.duck;
        lift = robot.lift;
        intake = robot.intake;
    }

    public void init_server(){
        try
        {
            server = new Server(new InetSocketServer(20000));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        server.registerProcessor(0x01, (cmd, payload, resp) -> { // Get frame
            if (detector_frame == null) return;

            Bitmap bmp = Bitmap.createBitmap(send_frame.cols(), send_frame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(send_frame, bmp);

            ByteArrayOutputStream os = new ByteArrayOutputStream(16384);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, os); // probably quite slow
            byte[] data = os.toByteArray();
            resp.respond(ByteBuffer.wrap(data));
        });
        server.startServer();
    }

    public void init_odometry(double y, double x, double heading){
        drivetrain.setStart(y, x, heading); // Must match Odo start position
    }

    public void init_lift(){
        lift.extend(0, false);
        lift.rotate(Status.ROTATIONS.get("in"));
        robot.intake.deposit(Status.DEPOSITS.get("carry"));
    }

    public void init_camera(){
        webcam = Webcam.forSerial(WEBCAM_SERIAL);
        if (webcam == null) throw new IllegalArgumentException("Could not find a webcam with serial number " + WEBCAM_SERIAL);
        frame_handler = new Webcam.SimpleFrameHandler();
        webcam.open(ImageFormat.YUY2, 800, 448, 30, frame_handler);
    }

    public void set_timer(double delay){
        timer_delay = delay;
        waiting = true;
    }

    public void check_image(){
        if (shipping_height != 0){
            return;
        }
        webcam.requestNewFrame();
        if (!frame_handler.newFrameAvailable) {
            throw new IllegalArgumentException("New frame not available");
        }
        Utils.bitmapToMat(frame_handler.currFramebuffer, detector_frame);
        CapstoneDetector capstone_detector = new CapstoneDetector(detector_frame, logger);
        x_coord = capstone_detector.detect();
        send_frame = capstone_detector.stored_frame;
//        if (75 < x_coord && x_coord < 300) {
//            shipping_height = 1;
//        } else if (300 < x_coord && x_coord < 500) {
//            shipping_height = 2;
//        } else if (500 < x_coord && x_coord < 800) {
//            shipping_height = 3;
//        }

        logger.i(String.format("X Coord of Block: %d", x_coord));
        logger.i(String.format("Shipping Height: %d", shipping_height));
    }

    public int update() {
        if (!waiting) {
            timer.reset();
        }

        telemetry.addData("Id: ", id);
        telemetry.addData("Shipping Height: ", shipping_height);
        telemetry.addData("X Coord of Block: ", x_coord);

        lift.updateLift();
        telemetry.update();

        if (lift.ifReached(lift.getTargetLiftPos())){
            logger.i("Reached Lift: %d", id);
            id += 1;
        } else if (intake.freightDetected()){
            logger.i("Detected Freight: %d", id);
            id += 1;
        } else if (timer.seconds() > timer_delay){
            logger.i("Reached Timer: %d", id);
            id += 1;
            waiting = false;
            timer.reset();
        }

        return id;
    }

    public void stop(){
        control_mgr.stop();
        if (webcam != null){
            webcam.close();
        }
        if (server != null) {
            server.close();
        }
    }
}
