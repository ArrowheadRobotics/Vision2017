import cv2
import numpy
import urllib

HSV = 0
RGB = 1
BOTH = 2

CALIBRATION_MODE = list((True, BOTH))
IGNORE_COLOR = -1
LOWER_BOUND_RGB = numpy.array([250, 250, 250], dtype=numpy.uint8)
UPPER_BOUND_RGB = numpy.array([255,255,255], dtype=numpy.uint8)
LOWER_BOUND_HSV = numpy.array([45, 0, 225], dtype=numpy.uint8)
UPPER_BOUND_HSV = numpy.array([80,10,255], dtype=numpy.uint8)

def nothing(a):
    pass

if (CALIBRATION_MODE[0]):
    if (CALIBRATION_MODE[1] == HSV):
        cv2.namedWindow("Trackbars")
        cv2.createTrackbar("hue_lower", "Trackbars", 0, 180, nothing)
        cv2.createTrackbar("hue_upper", "Trackbars", 180, 180, nothing)
        cv2.createTrackbar("sat_lower", "Trackbars", 0, 255, nothing)
        cv2.createTrackbar("sat_upper", "Trackbars", 255, 255, nothing)
        cv2.createTrackbar("val_lower", "Trackbars", 0, 255, nothing)
        cv2.createTrackbar("val_upper", "Trackbars", 255, 255, nothing)
    elif (CALIBRATION_MODE[1] == RGB):
        cv2.namedWindow("Trackbars")
        cv2.createTrackbar("red_lower", "Trackbars", 0, 255, nothing)
        cv2.createTrackbar("red_upper", "Trackbars", 255, 255, nothing)
        cv2.createTrackbar("green_lower", "Trackbars", 0, 255, nothing)
        cv2.createTrackbar("green_upper", "Trackbars", 255, 255, nothing)
        cv2.createTrackbar("blue_lower", "Trackbars", 0, 255, nothing)
        cv2.createTrackbar("blue_upper", "Trackbars", 255, 255, nothing)

stream = urllib.urlopen("http://10.7.6.35:1181/stream.mjpg")
resp = ''

while (True):
    resp += stream.read(25800)
    if (len(resp) > 50000):
        resp = ''
    a = resp.find('\xff\xd8')
    b = resp.find('\xff\xd9')
    try:
        if (a != -1 and b != -1):
            img = resp[a:b+2]
            resp = resp[b+2:]
            frame = cv2.imdecode(numpy.fromstring(img, dtype=numpy.uint8), cv2.IMREAD_COLOR)
            frame = cv2.blur(frame, (5,5))
            if (CALIBRATION_MODE[1] == HSV):
                hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
                lower = numpy.array([cv2.getTrackbarPos("hue_lower", "Trackbars"), cv2.getTrackbarPos("sat_lower", "Trackbars"), cv2.getTrackbarPos("val_lower", "Trackbars")], dtype=numpy.uint8)
                upper = numpy.array([cv2.getTrackbarPos("hue_upper", "Trackbars"), cv2.getTrackbarPos("sat_upper", "Trackbars"), cv2.getTrackbarPos("val_upper", "Trackbars")], dtype=numpy.uint8)
                thresh = cv2.inRange(hsv, lower, upper)
            elif (CALIBRATION_MODE[1] == RGB):
                lower = numpy.array([cv2.getTrackbarPos("blue_lower", "Trackbars"), cv2.getTrackbarPos("green_lower", "Trackbars"), cv2.getTrackbarPos("red_lower", "Trackbars")], dtype=numpy.uint8)
                upper = numpy.array([cv2.getTrackbarPos("blue_upper", "Trackbars"), cv2.getTrackbarPos("green_upper", "Trackbars"), cv2.getTrackbarPos("red_upper", "Trackbars")], dtype=numpy.uint8)
                thresh = cv2.inRange(frame, lower, upper)
            elif (CALIBRATION_MODE[1] == BOTH):
                hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
                hsv = cv2.blur(hsv, (5,5))
                threshRGB = cv2.inRange(frame, LOWER_BOUND_RGB, UPPER_BOUND_RGB)
                threshHSV = cv2.inRange(hsv, LOWER_BOUND_HSV, UPPER_BOUND_HSV)
                thresh = cv2.bitwise_and(threshRGB, threshHSV)
            out = cv2.bitwise_and(frame, frame, mask=thresh)
            try:
                coor = numpy.where(numpy.all(out != (0,0,0), axis=-1))
                xAvg = numpy.mean(coor[1])
                yAvg = numpy.mean(coor[0])
                cv2.circle(out, (int(xAvg), int(yAvg)), 5, (0, 255, 0), -1)
            except ValueError:
                print "Can\'t See Anything"
            cv2.imshow("Frame", out)
    except cv2.error:
        pass
    userin = cv2.waitKey(1)
    if (userin == 27):
        break

cv2.destroyAllWindows()
