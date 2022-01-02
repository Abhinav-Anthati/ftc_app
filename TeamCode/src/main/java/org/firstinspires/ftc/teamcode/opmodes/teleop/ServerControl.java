package org.firstinspires.ftc.teamcode.opmodes.teleop;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.input.ControllerMap;
import org.firstinspires.ftc.teamcode.util.websocket.InetSocketServer;
import org.firstinspires.ftc.teamcode.util.websocket.Server;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ServerControl extends ControlModule{
    private Server server;

    public ServerControl(String name) {
        super(name);
    }

    @Override
    public void initialize(Robot robot, ControllerMap controllerMap, ControlMgr manager) {
        try {
            server = new Server(new InetSocketServer(18888));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Plotter
        server.registerProcessor(0x1, (cmd, payload, resp) -> {
            ByteBuffer buf = ByteBuffer.allocate(500);

            buf.putDouble(robot.lift.getLiftCurrentPos());

            buf.putDouble(robot.lift.getPower());
            double[] pid_terms = robot.lift.getPIDTerms();
            buf.putDouble(pid_terms[0]); // P Term
            buf.putDouble(pid_terms[1]); // I Term
            buf.putDouble(pid_terms[2]); // D Term

            buf.flip();
            resp.respond(buf);
        });

        // Odo Drawer
        server.registerProcessor(0x2, (cmd, payload, resp) -> {
            ByteBuffer buf = ByteBuffer.allocate(300);

            buf.flip();
            resp.respond(buf);
        });


        server.startServer();
    }

    @Override
    public void update(Telemetry telemetry) {
        // No update
    }

    public void stop(){
        server.close();
    }
}
