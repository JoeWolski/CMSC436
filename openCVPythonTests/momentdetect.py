import cv2
import numpy as np

cam = cv2.VideoCapture(0)
cam.set(cv2.cv.CV_CAP_PROP_CONTRAST, 0.75)
cam.set(cv2.cv.CV_CAP_PROP_FPS, 30)
cam.set(cv2.cv.CV_CAP_PROP_GAIN, -100)

while(True):
    ret, img = cam.read()

    #if(last == None):
    #    last = img

    #diffimg = img - last

    hsv = cv2.cvtColor(img, cv2.cv.CV_BGR2HSV)
    gray = cv2.cvtColor(img, cv2.cv.CV_BGR2GRAY)

    h, s, v = cv2.split(hsv)

    can = cv2.Canny(gray, 200, 255)

    contours, heirarchy = cv2.findContours(can, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)


    cv2.drawContours(img, contours, -1, (255, 0, 0), 3)

    cv2.imshow("img", img)
    cv2.waitKey(1)

    #last = img
