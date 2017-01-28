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
		nav.setAngleAdjustment(nav.getAngle() * -1);
	}

	@Override
	public void autonomous() {
		double ang = nav.getAngle();
		double off = Constants.GOAL_BEARING - ang;
		System.out.println("Ang: " + ang + "\tOffset: " + off);
		if (Math.abs(off) > 5) {
			leftMot.set(off*Constants.SPIN_PRO);
			rightMot.set(off*Constants.SPIN_PRO);
		}
		else {
			rightMot.set(0);
			leftMot.set(0);
		}
	}

	@Override
	public void operatorControl() {
		double lastErr = 0;
		double totErr = 0;
		while (isOperatorControl() && isEnabled()) {
			if (leftJoy.getRawButton(Constants.TRIGGER)) {
				leftMot.set(leftAuto.get() ? 0.3 : 0);
				rightMot.set(rightAuto.get() ? -0.3 : 0);
			}
			else if (rightJoy.getRawButton(Constants.TRIGGER)) {
				double pos = 0.050942 * ultraTwo.getValue() - 2.57894;
				double err = pos - Constants.GOAL_DISTANCE;
				totErr += err;
				double pg = err * Constants.P_VAL;
				double ig = totErr * Constants.I_VAL;
				double dg = (lastErr-err) * Constants.D_VAL;
				lastErr = err;	
				double gain = pg + ig + dg;
				leftMot.set(gain);
				rightMot.set(gain*-1);
			}
			else if (leftJoy.getRawButton(Constants.BUTTON_TWO)) {
				double ang = nav.getAngle();
				double err = ang - Constants.GOAL_BEARING;
				double spd = err*0.01;
				leftMot.set(spd);
				rightMot.set(spd);
				System.out.println("ang: " + ang + "\terr" + err);
			}
			else if (rightJoy.getRawButton(Constants.BUTTON_TWO)) {
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
			else {
				currentMet = false;
				double leftJoy_val = leftJoy.getY() * -1.0f;
				double rightJoy_val = rightJoy.getY();
				leftMot.set(Math.abs(leftJoy_val) > Constants.DEAD_ZONE ? leftJoy_val : 0.0f);
				rightMot.set(Math.abs(rightJoy_val) > Constants.DEAD_ZONE ? rightJoy_val : 0.0f);
			}
		}
	}
	
	@Override
	public void test() {}
}

