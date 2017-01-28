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
	
	int step = 0;
	
	public double doSomeEasyMath() {
		return 0.050942 * ultraTwo.getValue() - 2.57894;
	}
	
	public void printDatGoodShit() {
		System.out.println("nav:\t" + nav.getAngle() + "\tultra:\t" + ultraTwo.getValue());
	}
	
	public void getAwayFromMe() {
		double err = doSomeEasyMath() - Constants.GOAL_DISTANCE;
		double pg = err * Constants.P_VAL;
		pg = pg < 0 ? Math.max(-1, pg) : Math.min(1, pg);
		leftMot.set(pg * Constants.SPEED_LIMIT);
		rightMot.set(pg*-1 * Constants.SPEED_LIMIT);
		if (err < 5 && Math.abs(pg) < 0.2) {
			leftMot.set(0);
			rightMot.set(0);
			step++;
		}
	}
	
	public void gottaGetTilted() {
		double ang = nav.getAngle();
		double err = ang - Constants.GOAL_BEARING;
		err = err % 360;
		double spd = err*0.01;
		spd = spd < 0 ? Math.max(-1, spd) : Math.min(1, spd);
		leftMot.set(spd);
		rightMot.set(spd);
		if (err < 5 && Math.abs(spd) < 0.1) {
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
	}
	
	@Override
	public void robotInit() {
		frontCam.startAutomaticCapture(Constants.FRONT_CAM);
		backCam.startAutomaticCapture(Constants.BACK_CAM);
	}

	@Override
	public void autonomous() {
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
	}

	@Override
	public void operatorControl() {
		while (isOperatorControl() && isEnabled()) {
			printDatGoodShit();
			double leftJoy_val = leftJoy.getY() * -1.0f;
			double rightJoy_val = rightJoy.getY();
			leftMot.set(Math.abs(leftJoy_val) > Constants.DEAD_ZONE ? leftJoy_val : 0.0f);
			rightMot.set(Math.abs(rightJoy_val) > Constants.DEAD_ZONE ? rightJoy_val : 0.0f);
		}
	}
	
	@Override
	public void test() {}
}

