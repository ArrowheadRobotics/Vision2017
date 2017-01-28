package org.usfirst.frc.team706.robot;

import com.ctre.CANTalon;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.SerialPort;

public class Robot extends SampleRobot {
	
	Joystick leftJoy, rightJoy;
	AnalogInput ultraOne, ultraTwo;
	DigitalInput leftAuto, rightAuto;
	CANTalon leftMot,rightMot;
	CameraServer frontCam, backCam;
	AHRS nav;
	
	boolean currentMet;
	int step;
	double offset;
	
	public double doSomeEasyMath() {
		return Constants.DIST_M * ultraTwo.getValue() - Constants.DIST_B;
	}
	
	public double getMeSomeSpeed() {
		double x = nav.getVelocityX();
		double y = nav.getVelocityY();
		double z = nav.getVelocityZ();
		double t = x*x + y*y + z*z;
		return Math.sqrt(t);
	}
	
	public void printDatGoodShit() {
		System.out.println("nav:\t" + (nav.getAngle()+offset) + "\tultra:\t" + ultraTwo.getValue() + "\tdist:\t" + doSomeEasyMath() + "\tvelo:\t" + getMeSomeSpeed());
	}
	
	public void getAwayFromMe() {
		double err = doSomeEasyMath() - Constants.GOAL_DISTANCE;
		double pg = err * Constants.P_VAL;
		pg = pg < 0 ? Math.max(-1, pg) : Math.min(1, pg);
		leftMot.set(pg * Constants.SPEED_LIMIT);
		rightMot.set(pg*-1 * Constants.SPEED_LIMIT);
		if (err < Constants.MIN_ERR && Math.abs(pg) < Constants.MIN_CURR) {
			leftMot.set(0);
			rightMot.set(0);
			step++;
		}
	}
	
	public void gottaGetTilted() {
		double ang = nav.getAngle() + offset;
		double err = ang - Constants.GOAL_BEARING;
		double spd = err*0.01;
		spd = spd < 0 ? Math.max(-1, spd) : Math.min(1, spd);
		leftMot.set(spd*Constants.SPEED_LIMIT);
		rightMot.set(spd*Constants.SPEED_LIMIT);
		if (err < Constants.MIN_ERR && Math.abs(spd) < Constants.MIN_CURR/2) {
			leftMot.set(0);
			rightMot.set(0);
			step++;
		}
	}
	
	public void iFoundSomeEyes() {
		leftMot.set(leftAuto.get() ? Constants.AUTO_HIGH : Constants.AUTO_LOW);
		rightMot.set(rightAuto.get() ? -1 * Constants.AUTO_HIGH : -1* Constants.AUTO_LOW);	
	}
	
	public void currentlyOvercurrented() {
		System.out.println(leftMot.getOutputCurrent());
		double driveSpeed = 0;
		if (!currentMet) {
			driveSpeed = (Constants.MAX_CURRENT > leftMot.getOutputCurrent()) ? Constants.SPEED_LIMIT : 0;
		}
		if (driveSpeed == 0) {
			currentMet = true;
		}
		leftMot.set(driveSpeed);
	}
	
	public Robot() {
		leftJoy = new Joystick(Constants.LEFT_JOY);
		rightJoy = new Joystick(Constants.RIGHT_JOY);
		leftAuto = new DigitalInput(Constants.LEFT_AUTO);
		rightAuto = new DigitalInput(Constants.RIGHT_AUTO);
		leftMot = new CANTalon(Constants.LEFT_MOT);
		rightMot = new CANTalon(Constants.RIGHT_MOT);
		ultraOne = new AnalogInput(Constants.ULTRA_ONE);
		ultraTwo = new AnalogInput(Constants.ULTRA_TWO);
		nav = new AHRS(SerialPort.Port.kMXP);
		frontCam = CameraServer.getInstance();
		backCam = CameraServer.getInstance();
		currentMet = false;
		step = 0;
		offset = nav.getAngle()*-1;
	}
	
	@Override
	public void robotInit() {
		frontCam.startAutomaticCapture(Constants.FRONT_CAM);
		backCam.startAutomaticCapture(Constants.BACK_CAM);
	}

	@Override
	public void autonomous() {
		offset = nav.getAngle()*-1;
		step = 0;
		while (isEnabled()) {
			printDatGoodShit();
			switch(step) {
			case 0:
				getAwayFromMe();
				break;
			case 1:
				gottaGetTilted();
				break;
			case 2:
				iFoundSomeEyes();
				break;
			}
		}
		step = 0;
		offset = nav.getAngle()*-1;
	}

	@Override
	public void operatorControl() {
		offset = nav.getAngle()*-1;
		step = 0;
		while (isOperatorControl() && isEnabled()) {
			printDatGoodShit();
			double leftJoy_val = leftJoy.getY() * -1.0f;
			double rightJoy_val = rightJoy.getY();
			leftMot.set(Math.abs(leftJoy_val) > Constants.DEAD_ZONE ? leftJoy_val : 0.0f);
			rightMot.set(Math.abs(rightJoy_val) > Constants.DEAD_ZONE ? rightJoy_val : 0.0f);
		}
		step = 0;
		offset = nav.getAngle()*-1;
	}
	
	@Override
	public void test() {}
}

