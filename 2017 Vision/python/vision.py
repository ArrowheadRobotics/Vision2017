import cv2
import numpy
import os
import math
import RPi.GPIO as GPIO

BLUR_SIZE = 5
ESC_KEY = 27
A_KEY = 97

cap = cv2.VideoCapture(0)

GPIO.setmode(GPIO.BCM)
GPIO.setup(23, GPIO.OUT, initial=GPIO.LOW)
GPIO.setup(24, GPIO.OUT, initial=GPIO.LOW)

def left():
    GPIO.output(23, GPIO.HIGH)

def right():
    GPIO.output(24, GPIO.HIGH)

def stop():
    GPIO.output(23, GPIO.LOW)
    GPIO.output(24, GPIO.LOW)

while (True):
    ret, frame, = cap.read()
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    blur = cv2.blur(hsv, (BLUR_SIZE, BLUR_SIZE))
    thresh = cv2.inRange(blur, numpy.array([35, 0, 230], numpy.uint8), numpy.array([55, 5, 240], numpy.uint8))
    out = cv2.bitwise_and(frame, frame, mask=thresh)
    userin = cv2.waitKey(1)
    coor = numpy.where(numpy.all(out != (0, 0, 0), axis=-1))
    stop()
    try:
        avg = (min(coor[1]) + max(coor[1]))/2
        if (avg < int(float(len(frame[0]))*9/16)):
            right()
        if (avg > int(float(len(frame[0]))*7/16)):
            left()
        if (userin == ESC_KEY):
            break
    except Exception:
        pass

cv2.destroyAllWindows()
