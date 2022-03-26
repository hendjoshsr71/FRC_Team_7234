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
import com.ctre.phoenix.motorcontrol.VictorSPXControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
 
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
 


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
  private static final String Testing1 = "Testing1";

  private String AutonSelect;
  private SendableChooser<String> autonChoice = new SendableChooser<>(); 

  MotorControllerGroup leftMotors = new MotorControllerGroup(driveLeftA, driveLeftB);
  MotorControllerGroup rightMotors = new MotorControllerGroup(driveRightA, driveRightB);
  //rightMotors.setInverted(true);  // flips to opposite because the powers that be decided to back one of them backward in 2022
 
  //DifferentialDrive = new DifferentialDrive (leftMotors, rightMotors);
  // CHANGED 3/13/22
  DifferentialDrive DifferentialDrive= new DifferentialDrive (leftMotors, rightMotors);
 
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

  final double armTimeUp = 0.99;
  final double armTimeDown = 0.22;

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
    autonChoice.addOption("180's no Cargo (right side alone)", Testing1);
    SmartDashboard.putData("Autonomous Choice", autonChoice);
    //add a thing on the dashboard to turn off auto if needed
    SmartDashboard.updateValues();

  }  // END OF ROBOT INIT

  @Override
  public void robotPeriodic() {
    SmartDashboard.putNumber("Left Position", driveLeftA.getEncoder().getPosition());
    SmartDashboard.putNumber("Right Position", driveRightA.getEncoder().getPosition());
    SmartDashboard.putNumber("Left Back Position", driveLeftB.getEncoder().getPosition());
    SmartDashboard.putNumber("Right Back Position", driveRightB.getEncoder().getPosition());
  }  // END OF ROBOT PERIODIC

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
 }  //  END OF AUTON INIT

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
    case OneCargo:     //////////     CASE 1  -  ONE BALL     //////////     
      Auton1();
    break;      
    case TwoCargo:     //////////     CASE 2  -  TWO BALL     //////////
      Auton2();
    break;
    case ThreeCargo:   //////////     CASE 3  -  THREE BALL   //////////
      Auton3();
    break;
    case Testing1:   //////////     CASE 3  -  THREE BALL   //////////
      Auton4();
    break;

    }  // end switch
  }  // end Autonomous Periodic

  
////////     TELEOP     //////////////////
////////     TELEOP     //////////////////
////////     TELEOP     //////////////////

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopPeriodic() {
    SmartDashboard.updateValues();
    final double TrueSpeed = 0.75;
    double SpeedLimit = TrueSpeed;
    double TurnSpeed = 0;
    if (gamePad1.getRawButton(4)) {
      Auton4(); 
//    }			

    //if ((gamePad1.getRawAxis (2) < 0) && (gamePad1.getRawButton(8))) {
    if (gamePad1.getRawButton(2) && gamePad1.getRawButton(8)) {
          SpeedLimit = 0.93;}
      else if (gamePad1.getRawButton(3) && gamePad1.getRawButton(8)) {
            SpeedLimit = 0.75;}
      else if (gamePad1.getRawButton(8)) {		
          SpeedLimit = 0.83;	
      } else if (gamePad1.getRawButton(7)) {		
          SpeedLimit = -0.75;
      } else {		
          SpeedLimit = 0;		
      }		
      SpeedLimit = Math.pow(SpeedLimit,3);		
      //SpeedLimit = Math.pow(SpeedLimit,3);
      //  check code, why two times???  it works!

      double leftSpeed = SpeedLimit;		
      double rightSpeed = SpeedLimit;	

      TurnSpeed = -gamePad1.getRawAxis (0);		
    
      if (TurnSpeed != 0)
      {SpeedLimit = 0.50;} 

      TurnSpeed =  TurnSpeed * 0.55;   
      leftSpeed = leftSpeed - TurnSpeed;		
      if (leftSpeed > 1) {leftSpeed = 0.85;}

      rightSpeed = rightSpeed + TurnSpeed;
      if (rightSpeed > 1) {rightSpeed = 0.85;}

      //leftSpeed = Math.pow(leftSpeed,3);		
      //rightSpeed = Math.pow(rightSpeed,3);		
          
    DifferentialDrive.tankDrive(leftSpeed,rightSpeed);

    //MotorControllerGroup leftMotors = new MotorControllerGroup(driveLeftA, driveLeftB);
    //MotorControllerGroup rightMotors = new MotorControllerGroup(driveRightA, driveRightB);

    if (joy1.getTriggerPressed()) {
      System.out.println("Setting camera 2");
      cameraSelection.setString(camera2.getName());
  } else if (joy1.getTriggerReleased()) {
      System.out.println("Setting camera 1");
      cameraSelection.setString(camera1.getName());
  
}
////////////////////////////////
//     Intake controls     /////
////////////////////////////////

// GAME PAD 2 - *** Does Not Work with gamePad1 having same control  ***
if(gamePad2.getRawButton(5)){	
  intake.set(VictorSPXControlMode.PercentOutput, 0.60);;	
  }	
  else if(gamePad2.getRawButton(7)){	
  intake.set(VictorSPXControlMode.PercentOutput, -0.95);	
  }	
  else{	
  intake.set(VictorSPXControlMode.PercentOutput, 0);	
  }	

  
// GAME PAD 1    //  Control give to drive for intake and launching
if(gamePad1.getRawButton(5)){	
  intake.set(VictorSPXControlMode.PercentOutput, 0.60);;	
  }	
  else if(gamePad1.getRawButton(6)){	
  intake.set(VictorSPXControlMode.PercentOutput, -0.95);	
  }	
  else{	
  intake.set(VictorSPXControlMode.PercentOutput, 0);	
  }	
    
   
// arm control code.  gamePad2  // Controller esnetially only has control of Arm Up / Arm Down
//  *** add button for arm half way???

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
}  //  END OF TELEOP

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

  }
//////////////////////////////////////////////////////////////////////////////////////
///    END OF MAINLINE     ///////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////

///  Autonomuos Functions ////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////////////
///    1 Cargo     ///////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////

public void Auton1 () {     // Case 1, One Cargo
 
  if (autonStep == 0)  //  wait 2.5 seconds 		                          //  Case 1
  {
    intake.set(VictorSPXControlMode.PercentOutput, 0.0);
    System.out.println("Step One-------- " + autonChoice);
    arm.set(0.13); // hold arm
    if (myTimer.get() > 3)
      {
        autonStep = autonStep + 1;
        driveLeftA.getEncoder().setPosition(0.0);
      }
  }
else if (autonStep == 1)   // forward to hub   		                          //  Case 1.1          
  {
    intake.set(VictorSPXControlMode.PercentOutput, 0);
    DifferentialDrive.tankDrive(0.50,0.50); // 
    arm.set(0.13); // hold arm up
    if ((driveLeftA.getEncoder().getPosition() > 22) || (myTimer.get() > 6.0))  
      {
        autonStep++;
        driveLeftA.getEncoder().setPosition(0.0);
        DifferentialDrive.tankDrive(0.0, 0.0);
      }
  }
else if (autonStep == 2) // Launch         		                               //  Case 1.2
  {
    // how do we put in a wait 0.5 seconds?
    intake.set(VictorSPXControlMode.PercentOutput, -0.9);
    DifferentialDrive.tankDrive(-0.25, -0.25); // back up slow to measure 
    arm.set(0.13); // hold arm up
    if (driveLeftA.getEncoder().getPosition() < -10) // turn 10 ticks
      {
        autonStep++;
        intake.set(VictorSPXControlMode.PercentOutput, 0.0);
        driveLeftA.getEncoder().setPosition(0.0);
        DifferentialDrive.tankDrive(0.0, 0.0);
      }
  }

else if (autonStep == 3) // Leave Tarmac             		                       //  Case 1.3
  {
    intake.set(VictorSPXControlMode.PercentOutput, 0.0); // turn on 1
    DifferentialDrive.tankDrive(-0.50, -0.50); // forward to cargo
    arm.set(0.13); //  lower arm
    if (driveLeftA.getEncoder().getPosition() < -18.0) // backward 10 ticks
     {
        autonStep++;
        driveLeftA.getEncoder().setPosition(0.0);
        DifferentialDrive.tankDrive(0.0, 0.0);
     }
  }                                                		                          //  
} // end of case 1 Auton 1 One Cargo

//////////////////////////////////////////////////////////////////////////////////////
///    2 Cargo     ///////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////

public void Auton2 () {    // Case 2, Two Cargo
  if (autonStep == 0)	
  {		// Arm Down
    intake.set(VictorSPXControlMode.PercentOutput, 0);	
    arm.set(-0.17);	
    DifferentialDrive.tankDrive(0.0, 0.0); // wait for arm	
    if (myTimer.get() > 1.25)        
    {
      autonStep++;
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		

else if (autonStep == 1)	                                          //  Case 2.1
{		//  Forward
  intake.set(VictorSPXControlMode.PercentOutput, 0);	
  arm.set(-0.13);	
  DifferentialDrive.tankDrive(0.50, 0.50); // forward to ball	
  if (driveLeftA.getEncoder().getPosition() > 10.0)   // before ball 		
  {
    autonStep++;
    driveLeftA.getEncoder().setPosition(0.0);	
  }
}		
else if (autonStep == 2)		                                          //  Case 2.2
{		// Forward - Intake
  intake.set(VictorSPXControlMode.PercentOutput, 0.65);	
  arm.set(-0.13);	
  DifferentialDrive.tankDrive(0.45, 0.45);  // slow near ball	
  if (driveLeftA.getEncoder().getPosition() > 12.0)   // thru ball 	was 20
  {	
    autonStep++;	
    driveLeftA.getEncoder().setPosition(0.0);	
  }	
}		
else if (autonStep == 3)		                                          //  Case 2.3
{		// turn in place 180 / Arm up
  intake.set(VictorSPXControlMode.PercentOutput,  0.4);   // 
  arm.set(0.15);   // starting up	
  DifferentialDrive.tankDrive(-0.55, 0.55);  // turn in place 180	
  if (driveLeftA.getEncoder().getPosition() < -13.5)   // switched from 12.2 to 8.1 Test shows 7.2		
  {	
    autonStep++;	
    driveLeftA.getEncoder().setPosition(0.0);	
  }	
}		
else if (autonStep == 4)		                                          //  Case 2.4
{		// Curved Turn
  intake.set(VictorSPXControlMode.PercentOutput, 0.0);	
  arm.set(0.15);	
  DifferentialDrive.tankDrive(0.48, 0.60);  // fastToNearHub, CURVED TURN 60,65?	
  if (driveLeftA.getEncoder().getPosition() > 40.0)    // close hub  practice 20 up to 30or40		
  {                                                   // raise to 0.85	  was 26
    autonStep++;	
    driveLeftA.getEncoder().setPosition(0.0);	
  }	
}		
else if (autonStep == 5)		                                          //  Case 2.5
{		// Nudge / Launch  
  intake.set(VictorSPXControlMode.PercentOutput, -0.95);	
  arm.set(0.15);	
  DifferentialDrive.tankDrive(0.30, 0.50);  // slow near hub with turn	
  if ((driveLeftA.getEncoder().getPosition() > 8.0) || (myTimer.get() > 8.5))  // push on wall		
  
  {	//MIGHT NOT WORK
    autonStep++;	
    driveLeftA.getEncoder().setPosition(0.0);	
  //autoStart = Timer.getFPGATimestamp();
  }	
}		
//if (autonStep == 6)	{		// Launch
//    intake.set(VictorSPXControlMode.PercentOutput, -0.90);  // launch, score four
//    arm.set(0.13);	
//  if (autoStart > 8)   ?????
//    {	
//    autonStep++;	
//    intake.set(VictorSPXControlMode.PercentOutput, 0.0);
//    }	
//  }		

else if (autonStep == 6)		                                          //  Case 2.6
{		
  intake.set(VictorSPXControlMode.PercentOutput,  0.0);	
  arm.set(0.13);	
  DifferentialDrive.tankDrive(-0.55, -0.55);  // back out of tarmac	
  if (driveLeftA.getEncoder().getPosition() < -26)   // back out of tarmac		
  {	
    autonStep++;	
    driveLeftA.getEncoder().setPosition(0.0);	
  }	
}
else if (autonStep == 7)		                                          //  Case 2.7
{		
  intake.set(VictorSPXControlMode.PercentOutput,  0.0);	
  arm.set(0.13);	
  DifferentialDrive.tankDrive(-0.65, 0.65);  // 
  if (driveLeftA.getEncoder().getPosition() < -12.5)   // 90 left
    {	
    autonStep++;	
    driveLeftA.getEncoder().setPosition(0.0);	
  }	
}
else if (autonStep == 8)		                                          //  Case 2.8
{		// forward to Opp cargo
  intake.set(VictorSPXControlMode.PercentOutput,  0.0);	
  arm.set(0.13);	
  DifferentialDrive.tankDrive(0.5, 0.5);  // 
    if (driveLeftA.getEncoder().getPosition() > 15)   // 		
  {	
    autonStep++;	
    driveLeftA.getEncoder().setPosition(0.0);	
  }	
}

}  // end of case 2, Auton 2, Two Cargo


//////////////////////////////////////////////////////////////////////////////////////
///    3 Cargo     ///////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////

public void Auton3 () {    // Case 3, Three Cargo

    if (autonStep == 0)		                                          //  Case 3.0
  {		// Launch One / back out   (maybe wait 0.5 to 1.0)
    intake.set(VictorSPXControlMode.PercentOutput, -0.9);	
    arm.set(0.13);	
    // DifferentialDrive.tankDrive(0, 0); // wait for arm	
    if (myTimer.get() > 0.75)  // if backing and shooting does not work
    // if (driveLeftA.getEncoder().getPosition() > -10.0)   		
        
    {
      autonStep++;
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		

else if (autonStep == 1)			                                          //  Case 3.1
  {		//  Back out
    intake.set(VictorSPXControlMode.PercentOutput, -0.0);	
    arm.set(0.13);	
    DifferentialDrive.tankDrive(-0.55, -0.55); // back	
    if (driveLeftA.getEncoder().getPosition() < -25.0)  //  90-41 = 49 at 15.0 		
    {
      autonStep++;
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
  else if (autonStep == 2)			                                          //  Case 3.2
  {		//  180 left/ arm stay up
    intake.set(VictorSPXControlMode.PercentOutput, -0.00);	
    arm.set(0.13);	
    DifferentialDrive.tankDrive(-0.45, 0.45); // 180 left	
    if (driveLeftA.getEncoder().getPosition() < -18.2) // was set to ">" 12.2 :(  		
    {
      autonStep = autonStep + 1;
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		  
else if (autonStep == 3)			                                          //  Case 3.3
  {		// Forward - Intake
    intake.set(VictorSPXControlMode.PercentOutput, 0.60);	
    arm.set(-0.13);	
    DifferentialDrive.tankDrive(0.55, 0.55);  // slow near ball	
    if (driveLeftA.getEncoder().getPosition() > 10.0)   // thru ball 	was 20
    {	
      autonStep = autonStep + 10;
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
else if (autonStep == 4)			                                          //  Case 3.4
  {		// back to parrallel   arm stays down
    intake.set(VictorSPXControlMode.PercentOutput,  0.0);   // Could be 0???	
    arm.set(0.0); 
    DifferentialDrive.tankDrive(-0.60, -0.60);  	
    if (driveLeftA.getEncoder().getPosition() < -10)   // 		
    {
      autonStep++;	
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
else if (autonStep == 5)			                                          //  Case 3.5
  {		// 95.1 degree right
    intake.set(VictorSPXControlMode.PercentOutput, 0.0);	
    arm.set(0.0);	
    DifferentialDrive.tankDrive(0.55, -0.55); //	
    if (driveLeftA.getEncoder().getPosition() > 6.25)   // 	
    {                                                  
      autonStep++;	
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
else if (autonStep == 6)			                                          //  Case 3.6
  {		// Forward to 2nd ball
    intake.set(VictorSPXControlMode.PercentOutput, 0.0);	
    arm.set(0.0);	
    DifferentialDrive.tankDrive(0.60, 0.60);  // 
    if (driveLeftA.getEncoder().getPosition() > 10.0)   // 		
    {	
      autonStep++;	
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
else if (autonStep == 7)			                                          //  Case 3.7
  {		// Forward / Intake
    intake.set(VictorSPXControlMode.PercentOutput, 0.60);  	
    arm.set(-0.13);	
    DifferentialDrive.tankDrive(0.55, 0.55);  // 
    if (driveLeftA.getEncoder().getPosition() > 7.0)   // 
    {	
      autonStep++;	
      intake.set(VictorSPXControlMode.PercentOutput, 0.0);
    }	
  }		
else if (autonStep == 8)			                                          //  Case 3.8
  {		// 110 degree right turn / arm up
    intake.set(VictorSPXControlMode.PercentOutput,  0.60);	
    arm.set(0.0);	
    DifferentialDrive.tankDrive(0.5, -0.5);  // 
    if (driveLeftA.getEncoder().getPosition() > 7.1)   // 		
    {	
      autonStep++;	
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }		
  else if (autonStep == 9)			                                          //  Case 3.9
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
  else if (autonStep == 10)			                                          //  Case 3.10
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
  else if (autonStep == 11)			                                          //  Case 3.11
  {		//  Back out
    intake.set(VictorSPXControlMode.PercentOutput, -0.90);	
    arm.set(0.13);	
    DifferentialDrive.tankDrive(-0.40, -0.40); // back	
    if (driveLeftA.getEncoder().getPosition() > 20.0)   		
    {
      autonStep++;
      driveLeftA.getEncoder().setPosition(0.0);	
    }	
  }						                                                               
}  // end of case 3 Auton 3 Cargo 3


public void Auton4 () {    // Case 3, Three Cargo

  if (autonStep == 0)		                                          //  Case 4
{		//
  intake.set(VictorSPXControlMode.PercentOutput, 0.00);	
  arm.set(0.13);	
  DifferentialDrive.tankDrive(0.5, 0.5); // wait for arm	
  if (driveLeftA.getEncoder().getPosition() > 15.0)   		
      
  {
    autonStep = autonStep + 1;
    driveLeftA.getEncoder().setPosition(0.0);	
  }	
}		

else if (autonStep == 1)			                                          //  Case 4
{		//  180's
  intake.set(VictorSPXControlMode.PercentOutput, 0);	
  arm.set(0.13);	
  DifferentialDrive.tankDrive(0.55, -0.55); // 180 right	
  if (driveLeftA.getEncoder().getPosition() > 14.0)   		
  {
    autonStep++;
    driveLeftA.getEncoder().setPosition(0.0);	
  }	 
}		
else if (autonStep == 20)			                                          //  Case 4
{		// 180 Left
  intake.set(VictorSPXControlMode.PercentOutput, 0.00);	
  arm.set(-0.13);	
  DifferentialDrive.tankDrive(-0.55, 0.55);  //  left
  if (driveLeftA.getEncoder().getPosition() < -14.0)   // thru ball 	was 20
  {	
    autonStep = autonStep + 1;
    driveLeftA.getEncoder().setPosition(0.0);	
  }	
}		
else if (autonStep == 3)			                                          //  Case 4
{		// back to parrallel   arm stays down
  intake.set(VictorSPXControlMode.PercentOutput,  0.0);   // Could be 0???	
  arm.set(0.0); 
  DifferentialDrive.tankDrive(0.65, -0.65);  // right 70
    if (driveLeftA.getEncoder().getPosition() > 6.0)   // 180 right 65%		
  {
    autonStep++;	
    driveLeftA.getEncoder().setPosition(0.0);	
  }	
}	
	
}  // end of case 4


}// END OF FUNCATIONS
  
