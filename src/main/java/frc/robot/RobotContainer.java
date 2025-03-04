// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.OperatorConstants;
import frc.robot.subsystems.ArmSubsystem;
import frc.robot.subsystems.ExtakeSubsystem;
import frc.robot.subsystems.GrabberSubsystem;
import frc.robot.subsystems.swervedrive.SwerveSubsystem;
import java.io.File;
import swervelib.SwerveInputStream;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a "declarative" paradigm, very
 * little robot logic should actually be handled in the {@link Robot} periodic methods (other than the scheduler calls).
 * Instead, the structure of the robot (including subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {
  // Initializing our two controllers
  public static final CommandXboxController driverController = new CommandXboxController(0);
  public static final CommandXboxController utilityController = new CommandXboxController(1);
  // The robot's subsystems and commands are defined here...
  final SwerveSubsystem drivebase = new SwerveSubsystem(
    new File(Filesystem.getDeployDirectory(), "swerve")
  );

  final double DRIVE_CONTROLLER_SLOW_DOWN = 0.5;

  final double DRIVE_SPEED_DEFAULT = 0.6;
  final double DRIVE_SPEED_SLOW = 0.25;
  boolean driveIsDefaultSpeed = true;
  double driveSpeed = DRIVE_SPEED_DEFAULT;

  void toggleDriveIsDefault() {
    this.driveIsDefaultSpeed = !this.driveIsDefaultSpeed;
    if(this.driveIsDefaultSpeed) {
      
    } else {

    }
    this.driveSpeed = this.driveIsDefaultSpeed ? DRIVE_SPEED_DEFAULT : DRIVE_SPEED_SLOW;
  }


  SendableChooser<Command> auto_chooser = new SendableChooser<>();

  //Initializes our three subsystems
  final ExtakeSubsystem extake = new ExtakeSubsystem();
  final GrabberSubsystem grabber = new GrabberSubsystem();
  final ArmSubsystem arm = new ArmSubsystem();

  // private final SendableChooser<Command> autoChooser;

  /**
   * Converts driver input into a field-relative ChassisSpeeds that is controlled by angular velocity.
   */
  SwerveInputStream driveAngularVelocity = SwerveInputStream.of(
    drivebase.getSwerveDrive(),
    () -> Math.signum(driverController.getLeftY()) * Math.abs(Math.pow(driverController.getLeftY(), 2)) * DRIVE_CONTROLLER_SLOW_DOWN,
    () -> Math.signum(driverController.getLeftX()) * Math.abs(Math.pow(driverController.getLeftX(), 2)) * DRIVE_CONTROLLER_SLOW_DOWN
  ).withControllerRotationAxis(driverController::getRightX)
  .deadband(OperatorConstants.DEADBAND)
  .scaleTranslation(0.8)
  .allianceRelativeControl(true);

  /**
   * Clone's the angular velocity input stream and converts it to a fieldRelative input stream.
   */
  SwerveInputStream driveDirectAngle = driveAngularVelocity.copy()
    .withControllerHeadingAxis(
      () -> Math.signum(driverController.getRightX()) * Math.abs(Math.pow(driverController.getRightX(), 2)) * DRIVE_CONTROLLER_SLOW_DOWN,
      () -> Math.signum(driverController.getRightY()) * Math.abs(Math.pow(driverController.getRightY(), 2)) * DRIVE_CONTROLLER_SLOW_DOWN
    ).headingWhile(true);

  /**
   * Clone's the angular velocity input stream and converts it to a robotRelative input stream.
   */
  SwerveInputStream driveRobotOriented = driveAngularVelocity.copy()
    .robotRelative(true)
    .allianceRelativeControl(false);

  SwerveInputStream driveAngularVelocityKeyboard = SwerveInputStream.of(
    drivebase.getSwerveDrive(),
    () -> -driverController.getLeftY(),
    () -> -driverController.getLeftX()
  ).withControllerRotationAxis(() -> driverController.getRawAxis(2))
    .deadband(OperatorConstants.DEADBAND)
    .scaleTranslation(0.8)
    .allianceRelativeControl(true);
  // Derive the heading axis with math!
  SwerveInputStream driveDirectAngleKeyboard = driveAngularVelocityKeyboard.copy()
    .withControllerHeadingAxis(
      () -> Math.sin(driverController.getRawAxis(2) * Math.PI) * (Math.PI * 2),
      () -> Math.cos(driverController.getRawAxis(2) * Math.PI) * (Math.PI * 2)
    ).headingWhile(true);

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer()
  {
    // Configure the trigger bindings
    configureBindings();
    DriverStation.silenceJoystickConnectionWarning(true);

    //Named commands for autonomous. In path planner, match the name of the command to the one set in here and it will
    //run the command associated with it
    NamedCommands.registerCommand("test", Commands.print("I EXIST"));
    NamedCommands.registerCommand("grabCoral", grabber.grab2());
    NamedCommands.registerCommand("releaseCoral", grabber.release());
    NamedCommands.registerCommand("LiftScoreHigh", extake.goToScoringGoal(ScoringGoal.L4));
    NamedCommands.registerCommand("LiftPickupCoral", extake.goToScoringGoal(ScoringGoal.Intake));
    NamedCommands.registerCommand("ArmScoreHigh", arm.goToScoringGoal(ScoringGoal.L4));
    NamedCommands.registerCommand("ArmPickupCoral", arm.goToScoringGoal(ScoringGoal.Intake));
    NamedCommands.registerCommand("ArmScoreLow", arm.goToScoringGoal(ScoringGoal.L1));

    // autoChooser = AutoBuilder.buildAutoChooser();
    // SmartDashboard.putData("Auto Chooser", autoChooser);
    //Zeroes the gyro when the robot container is initialized
    drivebase.zeroGyro();
    //When the grabber is neither grabbing or releasing, it runs inward very slowly to hold any coral
    grabber.setDefaultCommand(grabber.brake());
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary predicate, or via the
   * named factories in {@link edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for
   * {@link CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller PS4}
   * controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight joysticks}.
   */
  private void configureBindings()
  {

    Command driveFieldOrientedDirectAngle      = drivebase.driveFieldOriented(driveDirectAngle);
    Command driveFieldOrientedAnglularVelocity = drivebase.driveFieldOriented(driveAngularVelocity);
    Command driveRobotOrientedAngularVelocity  = drivebase.driveFieldOriented(driveRobotOriented);
    Command driveSetpointGen = drivebase.driveWithSetpointGeneratorFieldRelative(
        driveDirectAngle);
    Command driveFieldOrientedDirectAngleKeyboard      = drivebase.driveFieldOriented(driveDirectAngleKeyboard);
    Command driveFieldOrientedAnglularVelocityKeyboard = drivebase.driveFieldOriented(driveAngularVelocityKeyboard);
    Command driveSetpointGenKeyboard = drivebase.driveWithSetpointGeneratorFieldRelative(driveDirectAngleKeyboard);

    if (RobotBase.isSimulation()) {
      drivebase.setDefaultCommand(driveFieldOrientedDirectAngleKeyboard);
    } else {
      drivebase.setDefaultCommand(driveFieldOrientedAnglularVelocity);
    }

    if (Robot.isSimulation()) {
      driverController.start().onTrue(Commands.runOnce(() -> drivebase.resetOdometry(new Pose2d(3, 3, new Rotation2d()))));
      driverController.button(1).whileTrue(drivebase.sysIdDriveMotorCommand());

    } // idk if else can be here or not, it wasn't when I got here
    if (DriverStation.isTest()) {
      drivebase.setDefaultCommand(driveFieldOrientedAnglularVelocity); // Overrides drive command above!

      driverController.x().whileTrue(Commands.runOnce(drivebase::lock, drivebase).repeatedly());
      driverController.y().whileTrue(drivebase.driveToDistanceCommand(1.0, 0.2));
      driverController.start().onTrue((Commands.runOnce(drivebase::zeroGyro)));
      driverController.back().whileTrue(drivebase.centerModulesCommand());
      driverController.leftBumper().onTrue(Commands.none());
      driverController.rightBumper().onTrue(Commands.none());
    } else /* teleop */ {
      //All controller inputs for main teleop mode
      // driver
      driverController.start().onTrue((Commands.runOnce(drivebase::zeroGyro)));
      driverController.rightBumper().onChange(Commands.runOnce(() -> {
        this.toggleDriveIsDefault();
      }));
      // operator
      utilityController.a().onTrue(Commands.deadline(
        arm.goToScoringGoal(ScoringGoal.Intake),
        extake.goToScoringGoal(ScoringGoal.Intake)
      ));
      utilityController.leftBumper().whileTrue(Commands.deadline(
        grabber.release()
      ));
      utilityController.y().onTrue(Commands.deadline(
        extake.goToScoringGoal(ScoringGoal.L4),
        arm.goToScoringGoal(ScoringGoal.L4)
      ));
      utilityController.x().onTrue(Commands.deadline(
        extake.goToScoringGoal(ScoringGoal.L3),
        arm.goToScoringGoal(ScoringGoal.L3)
      ));
      utilityController.b().onTrue(Commands.deadline(
        extake.goToScoringGoal(ScoringGoal.L1),
        arm.goToScoringGoal(ScoringGoal.L1)
      ));
      utilityController.rightBumper().whileTrue(grabber.grab2());
      // manual override toggle for controlling the elevator
      utilityController.povLeft()
        .onTrue(Commands.runOnce(() -> {
          extake.toggleManual();
        }));
      // manual override toggle for controlling the arm
      utilityController.povRight()
        .onTrue(Commands.runOnce(() -> {
          arm.toggleManual();
        }));
      // sheath
      utilityController.povDown()
        .onTrue(Commands.runOnce(() -> {
          arm.sheath();
        }));
      // unsheath
      utilityController.povDown()
        .onFalse(Commands.runOnce(() -> {
          arm.unsheath();
        }));

      // set elevator zero position manually
      utilityController.start()
        .onTrue(Commands.runOnce(extake::zeroPosition));
    }
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */

  long startingTime = 0;
  long runningTime = 2000;
  public Command getAutonomousCommand()
  {
    // if (startingTime == 0)
    // {
    //   startingTime = RobotController.getFPGATime();
    // }
    // if (RobotController.getFPGATime() <= startingTime + runningTime)
    // {

    //private final Command m_NothingAuto;

    // drive
    auto_chooser.setDefaultOption("Drive Auto", Commands.run(() -> {
      drivebase.drive( new ChassisSpeeds(0, 1, 0));
    }, drivebase).withTimeout(2));
    // prepare to score
    auto_chooser.addOption(
      "Score Auto", 
      Commands.sequence(
        // go forward into reef
        Commands.run(
            () -> {
              drivebase.drive( new ChassisSpeeds(0, 1, 0));
            },
            drivebase
          ).withTimeout(3),
        // go backwards a little
        Commands.run(
            () -> {
              drivebase.drive( new ChassisSpeeds(0, -1, 0));
            },
            drivebase
          ).withTimeout(0.5),
        // position for score
        Commands.deadline(
          this.extake.goToScoringGoal(ScoringGoal.L1),
          this.arm.goToScoringGoal(ScoringGoal.L1)
        ),
        // score
        Commands.run(() ->this.grabber.release())
          .withTimeout(1)
      )
    );

    SmartDashboard.putData(auto_chooser);
    return auto_chooser.getSelected();
    // }
    // An example command will be run in autonomous
    //return autoChooser.getSelected();

    // return Commands.none();
  
  }

  public void setMotorBrake(boolean brake)
  {
    drivebase.setMotorBrake(brake);
  }
}
