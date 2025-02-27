package frc.robot.subsystems;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;

public class ExtakeSubsystem extends SubsystemBase{
    /** Motors for lift */
    private final TalonFX liftMotor1 = new TalonFX(31, "rio");
    private final TalonFX liftMotor2 = new TalonFX(32, "rio");
    
    private final DigitalInput limitSwitch = new DigitalInput(0);
    

    public final double LIFT_SCORE_L1 = -1;
    public final double LIFT_SCORE_L2 = -1;
    public final double LIFT_SCORE_L3 = -1;
    public final double LIFT_SCORE_L4 = -67.41;
    public final double LIFT_BOTTOM = 0;
    public final double LIFT_PICKUP_CORAL = 0 ;

    private boolean hasZeroed = false;
    

    // 1 for up, -1 for down
    public Command runMotor(double direction) {
        return run(() -> {
            this.liftMotor1.set(direction * 0.01);
        });
    }
    

    public ExtakeSubsystem(){
        var talonFXConfigs = new TalonFXConfiguration();

        // set slot 0 gains
        var slot0Configs = talonFXConfigs.Slot0;
        slot0Configs.kS = 0.25; // Add 0.25 V output to overcome static friction
        slot0Configs.kV = 0.12; // A velocity target of 1 rps results in 0.12 V output
        slot0Configs.kA = 0.01; // An acceleration of 1 rps/s requires 0.01 V output
        slot0Configs.kP = .4; // A position error of 2.5 rotations results in 12 V output
        slot0Configs.kI = 0; // no output for integrated error
        slot0Configs.kD = 0.1; // A velocity error of 1 rps results in 0.1 V output

        // set Motion Magic settings
        var motionMagicConfigs = talonFXConfigs.MotionMagic;
        motionMagicConfigs.MotionMagicCruiseVelocity = 80; // Target cruise velocity of 80 rps
        motionMagicConfigs.MotionMagicAcceleration = 160; // Target acceleration of 160 rps/s (0.5 seconds)
        motionMagicConfigs.MotionMagicJerk = 1600; // Target jerk of 1600 rps/s/s (0.1 seconds)


        liftMotor1.getConfigurator().apply(slot0Configs);
        liftMotor2.getConfigurator().apply(slot0Configs);
        final Follower m_Follower = new Follower(31, false);
        liftMotor2.setControl(m_Follower);
        liftMotor1.setNeutralMode(NeutralModeValue.Brake);
        liftMotor2.setNeutralMode(NeutralModeValue.Brake);
    }

    public void periodic(){
        SmartDashboard.putNumber("Elevator Position", liftMotor1.getPosition().getValueAsDouble());

        /*
        if(limitSwitch.get() == true){
            hasZeroed = true;
            liftMotor1.setPosition(0);
        }
        if(hasZeroed == false){
            if(this.getCurrentCommand() != null){
                this.getCurrentCommand().cancel();
            }
            final VoltageOut m_request = new VoltageOut(-.45);
            liftMotor1.setControl(m_request);
        }
         */
        
    }

    public Command liftGoToPosCommand(double position){
        final MotionMagicVoltage m_request = new MotionMagicVoltage(0).withSlot(0);
         return run(() -> {
            System.out.println("Test Print");
            liftMotor1.setControl(m_request.withPosition(position));
         });
    }
}
