package frc.robot.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.LimelightHelpers;
import frc.robot.subsystems.swervedrive.SwerveSubsystem;

public class AutoAlign extends Command{
    private PIDController xController, yController, rotController;
    private boolean isRightSCore;
    private Timer dontSeeTagTimer, stopTimer;
    private double tagID = -1;
    private SwerveSubsystem drivebase;

    public final double DONT_SEE_TAG_WAIT_TIME = 3;
    public final double POSE_VALIDATION_TIME = 0.3;

    public final double X_REEF_ALIGNMENT_P = 0.001;
    public final double Y_REEF_ALIGNMENT_P = 0.001;
    public final double ROT_REEF_ALIGNMENT_P = 0.001;

    public final double ROT_SETPOINT_REEF_ALIGNMENT = 0;
    public final double ROT_TOLERANCE_REEF_ALIGNMENT = 5;

    public final double X_SETPOINT_REEF_ALIGNMENT = 3;
    public final double X_TOLERANCE_REEF_ALIGNMENT = 5;

    public final double Y_SETPOINT_REEF_ALIGNMENT = 3;
    public final double Y_TOLERANCE_REEF_ALIGNMENT = 5;

    public AutoAlign(boolean isRightSCore, SwerveSubsystem drivebase){
        xController = new PIDController(X_REEF_ALIGNMENT_P, 0, 0);
        yController = new PIDController(Y_REEF_ALIGNMENT_P, 0, 0);
        rotController = new PIDController(ROT_REEF_ALIGNMENT_P, 0, 0);
        this.isRightSCore = isRightSCore;
        this.drivebase = drivebase;
        addRequirements(drivebase);
    }

    public void initialize(){
        this.stopTimer = new Timer();
        this.stopTimer.start();
        this.dontSeeTagTimer = new Timer();
        this.dontSeeTagTimer.start();

        rotController.setSetpoint(ROT_SETPOINT_REEF_ALIGNMENT);
        rotController.setTolerance(ROT_TOLERANCE_REEF_ALIGNMENT);

        xController.setSetpoint(X_SETPOINT_REEF_ALIGNMENT);
        xController.setTolerance(X_TOLERANCE_REEF_ALIGNMENT);

        yController.setSetpoint(Y_SETPOINT_REEF_ALIGNMENT);
        yController.setTolerance(Y_TOLERANCE_REEF_ALIGNMENT);

        if(LimelightHelpers.getTV("")){
            tagID = LimelightHelpers.getFiducialID("");
        }
    }

    public void execute(){
        if(LimelightHelpers.getTV("") && LimelightHelpers.getFiducialID("") == tagID){
            this.dontSeeTagTimer.reset();

            double[] positions = LimelightHelpers.getBotPose_TargetSpace("");

            double xSpeed = xController.calculate(positions[2]);
            double ySpeed = -yController.calculate(positions[0]);
            double rotValue = -rotController.calculate(positions[4]);

            drivebase.drive(
                new Translation2d(yController.getError() < Y_TOLERANCE_REEF_ALIGNMENT ? xSpeed : 0, ySpeed)
                , rotValue, false);

                if(!rotController.atSetpoint() ||
                !yController.atSetpoint() ||
                !xController.atSetpoint()){
                    stopTimer.reset();
                } else {
                    drivebase.drive(new Translation2d(), 0, false);
                }
        }
    }

    public boolean isFinished(){
        return this.dontSeeTagTimer.hasElapsed(DONT_SEE_TAG_WAIT_TIME) || 
            stopTimer.hasElapsed(POSE_VALIDATION_TIME);
    }
}
