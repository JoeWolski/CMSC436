import cv2
import numpy as np

cam = cv2.VideoCapture(0)
cam.set(cv2.cv.CV_CAP_PROP_CONTRAST, 0.75)
cam.set(cv2.cv.CV_CAP_PROP_FPS, 30)
cam.set(cv2.cv.CV_CAP_PROP_GAIN, -100)

last = None 

kernel1 = np.ones((5, 5), 'int')
kernel2 = np.ones((1, 1), 'int')

while(True):
    ret, img = cam.read()

    #if(last == None):
    #    last = img

    #diffimg = img - last

    hsv = cv2.cvtColor(img, cv2.cv.CV_BGR2HSV)
    grey = cv2.cvtColor(img, cv2.cv.CV_BGR2GRAY)

    h, s, v = cv2.split(hsv)

    h = cv2.inRange(h, 125, 150)
    s = cv2.inRange(s, 30, 110)
    v = cv2.inRange(v, 200, 255)

    h = cv2.dilate(h, kernel1)
    v = cv2.erode(v, kernel1)
    s = cv2.erode(s, kernel1)

    #v = cv2.dilate(v, kernel2)
    #s = cv2.dilate(s, kernel2)

    final = cv2.bitwise_and(s, v)
    final = cv2.bitwise_and(final, h)

    #final = cv2.bitwise_not(final)
    #cv2.imshow("final", final)

    contours, heirarchy = cv2.findContours(final, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

    largest = 0
    area = 0
    for i in xrange(0, len(contours)):
        newarea = cv2.contourArea(contours[i])
        if area < newarea:
            area = newarea
            largest = i


    if(len(contours) > 0):
        contour = contours[largest]
        #cv2.drawContours(img, contour, -1, (255, 0, 0), 3)
        moment = cv2.moments(contour)
        print(moment)
        center = (moment['m10']/moment['m00'],moment['m01']/moment['m00'])
        center_in = (int(center[0]),int(center[1]))
        cv2.circle(img,center_in,2,(255,0,0),3)


    #cv2.imshow("h", h)
    #cv2.imshow("s", s)
    #cv2.imshow("v", v)
    cv2.imshow("img", img)
    cv2.waitKey(1)

    #last = img
