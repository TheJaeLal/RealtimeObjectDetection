import urllib.request
import cv2
import numpy as np

stream_url = 'http://192.168.1.19:8080/shot.jpg'

while True:
	with urllib.request.urlopen(stream_url) as url:
		img_response = url.read()
		#print(img_response)
		img_array = np.array(bytearray(img_response),dtype=np.uint8)
		img = cv2.imdecode(img_array,-1)
		

		cv2.imshow("test",img)
		if ord('q') == cv2.waitKey(10):
			exit(0)

