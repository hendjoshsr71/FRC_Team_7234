 /*
  2022 everybot code
  written by carson graf 
  don't email me, @ me on discord
*/
// Egeler 2/1/22 first attempt with SparkMax 

/*
  This is catastrophically poorly written code for the sake of being easy to follow
  If you know what the word "refactor" means, you should refactor this code
*/
 
package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.UsbCamera;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.sendable.Sendable;

import javax.security.auth.callback.ChoiceCallback;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.VictorSPXControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
 
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
//import edu.wpi.first.wpilibj.MotorControllerGroup;
 


public class Robot extends TimedRobot {
  
  //Definitions for the hardware. Change this if you change what stuff you have plugged in
  CANSparkMax driveLeftA = new CANSparkMax(1, MotorType.kBrushless);
  CANSparkMax driveLeftB = new CANSparkMax(2, MotorType.kBrushless);
  CANSparkMax driveRightA = new CANSparkMax(3, MotorType.kBrushless);
  CANSparkMax driveRightB = new CANSparkMax(4, MotorType.kBrushless);
  CANSparkMax arm = new CANSparkMax(5, MotorType.kBrushless);
  VictorSPX intake = new VictorSPX(6);

  private static final String OneCargo = "OneCargo";
  private static final String TwoCargo = "TwoCargo";
  private static final String ThreeCargo = "ThreeCargo";

  private String AutonSelect;
  private SendableChooser<String> autonChoice = new SendableChooser<>(); 

 //MotorControllerGroup rightMotors = new MotorControllerGroup(RightRear, RightFront);
  //MotorControllerGroup leftMotors = new MotorControllerGroup(LeftRear, LeftFront);
  //rightMotors.setInverted(true);  // flips to opposite because the powers that be decided to back one of them backward in 2022
 
  //DifferentialDrive = new DifferentialDrive (leftMotors, rightMotors);
  DifferentialDrive DifferentialDrive= new DifferentialDrive (driveLeftA, driveRightA);
 
  Joystick gamePad1 = new Joystick(0);
  Joystick gamePad2 = new Joystick(1);
  Timer myTimer = new Timer();
  final double Left = 0;
  final double Right = 0;

  int autonStep = 0;

 
  //Constants for controlling the arm. consider tuning these for your particular robot
  final double armHoldUp = 0.10;
  final double armHoldDown = 0.9;
  final double armTravel = 0.30;

  final double armTimeUp = 0.85;
  final double armTimeDown = 0.40;

  //Varibles needed for the code
  boolean armUp = true; //Arm initialized to up because that's how it would start a match
  boolean pressedMode = false;
  double lastPressedTime = 0;

  double autoStart = 0;

  UsbCamera camera1;
  UsbCamera camera2;
  Joystick joy1 = new Joystick(0);
  NetworkTableEntry cameraSelection;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    //Configure motors to turn correct direction. You may have to invert some of your motors
    CameraServer.startAutomaticCapture();
    driveLeftA.setInverted(false);
    driveLeftA.burnFlash();
    driveLeftB.setInverted(false);
    driveLeftB.burnFlash();
    driveRightA.setInverted(true);
    driveRightA.burnFlash();
    driveRightB.setInverted(true);
    driveRightB.burnFlash();
    
    arm.setInverted(false);
    arm.setIdleMode(IdleMode.kBrake);
    arm.burnFlash();

    camera1 = CameraServer.startAutomaticCapture(0);
    camera2 = CameraServer.startAutomaticCapture(1);
    cameraSelection = NetworkTableInstance.getDefault().getTable("").getEntry("CameraSelection");

    autonChoice.addOption("One Cargo", OneCargo);
    autonChoice.setDefaultOption("Two Cargo", TwoCargo);
    autonChoice.addOption("Three Cargo (right side alone)", ThreeCargo);
    SmartDashboard.putData("Autonomous Choice", autonChoice);
    //add a thing on the dashboard to turn off auto if needed
    SmartDashboard.updateValues();

  }

  @Override
  public void robotPeriodic() {
    SmartDashboard.putNumber("Left Position", driveLeftA.getEncoder().getPosition());
  }

  @Override
  public void autonomousInit() {
    myTimer = new Timer ();
    myTimer.reset();
    myTimer.start(); 
    autonStep = 0;
    driveLeftA.getEncoder().setPosition(0.0);	

    AutonSelect = autonChoice.getSelected();
    System.out.println("Auto   .*.Auto   .*.Auto >>>>>>>" + autonChoice);
    SmartDashboard.updateValues();
 }

  protected void execute() {
    SmartDashboard.putNumber("Left Position", driveLeftA.getEncoder().getPosition());
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    SmartDashboard.updateValues();

    System.out.println("Auto Auto Auto Auto Auto -------- " + autonChoice);
    System.out.println("                                  " + autonChoice);
    System.out.println("                                  " + autonChoice);
    System.out.println("                                  " + autonChoice);
    switch (AutonSelect) {
    
      ///////////////////////////////////////////////////   CASE 1  -  ONE BALL    ///////////////////      
    case OneCargo:
      if (autonStep == 0)  //  wait 2.5 seconds
          {
            intake.set(VictorSPXControlMode.PercentOutput, 0.0);
            System.out.println("Step One-------- " + autonChoice);
            arm.set(0.13); // hold arm
            if (myTimer.get() > 2.5)
              {
                autonStep = autonStep + 1;
                driveLeftA.getEncoder().setPosition(0.0);
              }
          }
        else if (autonStep == 1)   // forward to hub
          {
            intake.set(VictorSPXControlMode.PercentOutput, 0);
            DifferentialDrive.tankDrive(0.65,0.65); // 
            arm.set(0.13); // hold arm up
            if ((driveLeftA.getEncoder().getPosition() > 19) || (myTimer.get() > 6.0)) //  11??? 
              {
                autonStep++;
                driveLeftA.getEncoder().setPosition(0.0);
                DifferentialDrive.tankDrive(0.0, 0.0);
              }
          }
        else if (autonStep == 2) // Launch
          {
            // how do we put in a wait 0.5 seconds?
            intake.set(VictorSPXControlMode.PercentOutput, -0.9);
            DifferentialDrive.tankDrive(-0.55, -0.55); // back up slow to measure 
            arm.set(0.13); // hold arm up
            if (driveLeftA.getEncoder().getPosition() < -12) // turn 10 ticks
              {
                autonStep++;
                intake.set(VictorSPXControlMode.PercentOutput, 0.0);
                driveLeftA.getEncoder().setPosition(0.0);
                DifferentialDrive.tankDrive(0.0, 0.0);
              }
          }
      
        else if (autonStep == 3) // Leave Tarmac
          {
            intake.set(VictorSPXControlMode.PercentOutput, 0.0); // turn on 1
            DifferentialDrive.tankDrive(-0.65, -0.65); // forward to cargo
            arm.set(0.13); //  lower arm
            if (driveLeftA.getEncoder().getPosition() < -12.0) // backward 10 ticks
             {
                autonStep++;
                driveLeftA.getEncoder().setPosition(0.0);
                DifferentialDrive.tankDrive(0.0, 0.0);
             }
          }
    break;      
///////////////////////////////////////////////////   CASE 2  -  TWO BALL    ///////////////////
    case TwoCargo:
      if (autonStep == 0)	
        {		// Arm Down
          intake.set(VictorSPXControlMode.PercentOutput, 0);	
          arm.set(-0.20);	
          DifferentialDrive.tankDrive(0.0, 0.0); // wait for arm	
          if (myTimer.get() > 1.5)        
          {
            autonStep++;
            driveLeftA.getEncoder().setPosition(0.0);	
          }	
        }		

    else if (autonStep == 1)	
      {		//  Forward
        intake.set(VictorSPXControlMode.PercentOutput, 0);	
        arm.set(-0.13);	
        DifferentialDrive.tankDrive(0.45, 0.45); // forward to ball	
        if (driveLeftA.getEncoder().getPosition() > 10.0)   // before ball 		
        {
          autonStep++;
          driveLeftA.getEncoder().setPosition(0.0);	
        }	
      }		
    else if (autonStep == 2)	
      {		// Forward - Intake
        intake.set(VictorSPXControlMode.PercentOutput, 0.60);	
        arm.set(-0.13);	
        DifferentialDrive.tankDrive(0.40, 0.40);  // slow near ball	
        if (driveLeftA.getEncoder().getPosition() > 6.0)   // thru ball 	was 20
        {	
          autonStep++;	
          driveLeftA.getEncoder().setPosition(0.0);	
        }	
      }		
    else if (autonStep == 3)	
      {		// turn in place 180 / Arm up
        intake.set(VictorSPXControlMode.PercentOutput,  0.0);   // Could be 0???	
        arm.set(0.13);   // starting up	
        DifferentialDrive.tankDrive(-0.45, 0.45);  // turn in place 180	
        if (driveLeftA.getEncoder().getPosition() < -8.1)   // turning   why not 12.2?		
        {	//diff surfaces
          autonStep++;	
          driveLeftA.getEncoder().setPosition(0.0);	
        }	
      }		
    else if (autonStep == 4)	
      {		// Curved Turn
        intake.set(VictorSPXControlMode.PercentOutput, 0.0);	
        arm.set(0.13);	
        DifferentialDrive.tankDrive(0.40, 0.45);  // fastToNearHub, CURVED TURN 60,65?	
        if (driveLeftA.getEncoder().getPosition() > 24.0)   // close hub  practice 20 up to 30or40		
        {                                                   // raise to 0.85	
          autonStep++;	
          driveLeftA.getEncoder().setPosition(0.0);	
        }	
      }		
    else if (autonStep == 5)	
      {		// Nudge / Launch  
        intake.set(VictorSPXControlMode.PercentOutput, -0.90);	
        arm.set(0.13);	
        DifferentialDrive.tankDrive(0.30, 0.50);  // slow near hub with turn	
        if (driveLeftA.getEncoder().getPosition() > 6.0)   // push on wall		
        {	//MIGHT NOT WORK
          autonStep++;	
          driveLeftA.getEncoder().setPosition(0.0);	
        //autoStart = Timer.getFPGATimestamp();
        }	
      }		
    //if (autonStep == 6)	{		// Launch
    //    intake.set(VictorSPXControlMode.PercentOutput, -0.90);  // launch, score 4	
    //    arm.set(0.13);	
    //  if (autoStart > 8)   ?????
    //    {	
    //    autonStep++;	
    //    intake.set(VictorSPXControlMode.PercentOutput, 0.0);
    //    }	
    //  }		

    else if (autonStep == 6)	
      {		
        intake.set(VictorSPXControlMode.PercentOutput,  0.0);	
        arm.set(0.13);	
        DifferentialDrive.tankDrive(-0.65, -0.65);  // back out of tarmac	
        if (driveLeftA.getEncoder().getPosition() < -20)   // back out of tarmac		
        {	
          autonStep++;	
          driveLeftA.getEncoder().setPosition(0.0);	
        }	
      }		
    
    break;

///////////////////////////////////////////////////   CASE 3  -  Three BALL    ///////////////////
case ThreeCargo:
if (autonStep == 0)	
  {		// Launch One / back out   (maybe wait 0.5 to 1.0)
    intake.set(VictorSPXControlMode.PercentOutput, -0.90);	
    arm.set(0.13);	
    DifferentialDrive.tankDrive(-0.65, -0.65); // wait for arm	
    if (myTimer.get() > 10.5)        
    {
      autonStep++;
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		

else if (autonStep == 1)	
  {		//  180
    intake.set(VictorSPXControlMode.PercentOutput, 0);	
    arm.set(-0.13);	
    DifferentialDrive.tankDrive(0.55, -0.55); // 180 right	
    if (driveLeftA.getEncoder().getPosition() > 12.2)   		
    {
      autonStep++;
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
else if (autonStep == 2)	
  {		// Forward - Intake
    intake.set(VictorSPXControlMode.PercentOutput, 0.60);	
    arm.set(-0.13);	
    DifferentialDrive.tankDrive(0.55, 0.55);  // slow near ball	
    if (driveLeftA.getEncoder().getPosition() > 10.0)   // thru ball 	was 20
    {	
      autonStep++;	
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
else if (autonStep == 3)	
  {		// back to parrallel   arm stays down
    intake.set(VictorSPXControlMode.PercentOutput,  0.0);   // Could be 0???	
    arm.set(0.0); 
    DifferentialDrive.tankDrive(-0.65, -0.65);  // turn in place 180	
    if (driveLeftA.getEncoder().getPosition() < -10)   // 		
    {
      autonStep++;	
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
else if (autonStep == 4)	
  {		// 90 degree right
    intake.set(VictorSPXControlMode.PercentOutput, 0.0);	
    arm.set(0.0);	
    DifferentialDrive.tankDrive(0.55, -0.55); //	
    if (driveLeftA.getEncoder().getPosition() > 6.1)   // 	
    {                                                  
      autonStep++;	
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
else if (autonStep == 5)	
  {		// Forward to 2nd ball
    intake.set(VictorSPXControlMode.PercentOutput, 0.0);	
    arm.set(0.0);	
    DifferentialDrive.tankDrive(0.65, 0.65);  // 
    if (driveLeftA.getEncoder().getPosition() > 10.0)   // 		
    {	
      autonStep++;	
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
else if (autonStep == 6)	
  {		// Forward / Intake
    intake.set(VictorSPXControlMode.PercentOutput, 0.60);  	
    arm.set(-0.13);	
    DifferentialDrive.tankDrive(0.55, 0.55);  // 
    if (driveLeftA.getEncoder().getPosition() > 10.0)   // 
    {	
      autonStep++;	
      intake.set(VictorSPXControlMode.PercentOutput, 0.0);
    }	
  }		
else if (autonStep == 7)	
  {		// 95 degree turn / arm up
    intake.set(VictorSPXControlMode.PercentOutput,  0.60);	
    arm.set(0.13);	
    DifferentialDrive.tankDrive(0.5, -0.5);  // 
    if (driveLeftA.getEncoder().getPosition() > 6)   // 		
    {	
      autonStep++;	
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
  else if (autonStep == 8)	
  {		// Curved Turn
    intake.set(VictorSPXControlMode.PercentOutput, 0.0);	
    arm.set(0.13);	
    DifferentialDrive.tankDrive(0.60, 0.65);  // fastToNearHub, CURVED TURN 60,65?	
    if (driveLeftA.getEncoder().getPosition() > 15.0)   // close hub  practice 20 up to 30or40		
    {                                                   // raise to 0.85	
      autonStep++;	
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
  else if (autonStep == 9)	
  {		// Nudge / Launch  
    intake.set(VictorSPXControlMode.PercentOutput, -0.90);	
    arm.set(0.13);	
    DifferentialDrive.tankDrive(0.30, 0.50);  // slow near hub with turn	
    if (driveLeftA.getEncoder().getPosition() > 6.0)   // push on wall		
    {	//MIGHT NOT WORK
      autonStep++;	
      driveLeftA.getEncoder().setPosition(0.0);	
    //autoStart = Timer.getFPGATimestamp();
    }	
  }		
break;

    }  // end switch
    }  // end autonomous

  
////////     TELEOP     //////////////////

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopPeriodic() {
    final double TrueSpeed = 0.80;
    double SpeedLimit = TrueSpeed;
    double TurnSpeed = 0;

			
    if (gamePad1.getRawButton(8)) {		
      SpeedLimit = 0.95;	
      } else if (gamePad1.getRawButton(7)) {		
      SpeedLimit = -0.95;
      		
      } else {		
      SpeedLimit = 0;		
      }		

      SpeedLimit = Math.pow(SpeedLimit,3);		
      SpeedLimit = Math.pow(SpeedLimit,3);
      //  check code, why two times???  it works!

      double leftSpeed = SpeedLimit;		
      double rightSpeed = SpeedLimit;	

      TurnSpeed = -gamePad1.getRawAxis (0);		
    
      if (TurnSpeed != 0)
      {SpeedLimit = 0.5;}

      TurnSpeed =  TurnSpeed * 0.60;   
      leftSpeed = leftSpeed - TurnSpeed;		
      rightSpeed = rightSpeed + TurnSpeed;		
          
    DifferentialDrive.tankDrive(leftSpeed,rightSpeed);

    if (joy1.getTriggerPressed()) {
      System.out.println("Setting camera 2");
      cameraSelection.setString(camera2.getName());
  } else if (joy1.getTriggerReleased()) {
      System.out.println("Setting camera 1");
      cameraSelection.setString(camera1.getName());
  
}
////////////////////////////////
  //Intake controls
////////////////////////////////
// GAME PAD 2 - Does Not Work with gamePad1 having same control
if(gamePad2.getRawButton(5)){	
  intake.set(VictorSPXControlMode.PercentOutput, 0.60);;	
  }	
  else if(gamePad2.getRawButton(7)){	
  intake.set(VictorSPXControlMode.PercentOutput, -0.95);	
  }	
  else{	
  intake.set(VictorSPXControlMode.PercentOutput, 0);	
  }	

  
  // GAME PAD 1
if(gamePad1.getRawButton(5)){	
  intake.set(VictorSPXControlMode.PercentOutput, 0.60);;	
  }	
  else if(gamePad1.getRawButton(6)){	
  intake.set(VictorSPXControlMode.PercentOutput, -0.95);	
  }	
  else{	
  intake.set(VictorSPXControlMode.PercentOutput, 0);	
  }	
    
   
// arm control code.  gamePad2
 if(armUp){
  if(Timer.getFPGATimestamp() - lastPressedTime < armTimeUp){
    arm.set(armTravel);   //armTravel
  }
  else{
    arm.set(armHoldUp);  // armHoldUp
  }
}
else{
  if(Timer.getFPGATimestamp() - lastPressedTime < armTimeDown){
    arm.set(-armTravel); // armholdup
  }
  else{
    arm.set(-armHoldUp);  //-armHoldUp
  }
}


if(gamePad2.getRawButtonPressed(6) && !armUp){	
lastPressedTime = Timer.getFPGATimestamp();	
armUp = true;	
}	
else if(gamePad2.getRawButtonPressed(8) && armUp){	
lastPressedTime = Timer.getFPGATimestamp();	
armUp = false;	
}	

}

////////////////////////////////

  @Override
  public void disabledInit() {
    //On disable turn off everything
    //done to solve issue with motors "remembering" previous setpoints after reenable
    driveLeftA.set(0);
    driveLeftB.set(0);
    driveRightA.set(0);
    driveRightB.set(0);
    arm.set(0);

// TE 2/1    intake.set(ControlMode.PercentOutput, 0);
  }
    
}
