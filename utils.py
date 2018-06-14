import os
import cv2
import numpy as np

def get_dir_images(dir_name):
    img_names = [fname for fname in os.listdir(dir_name) 
                 if fname.lower().endswith(".png") or fname.lower().endswith(".jpg")]

    img_names.sort()
    
    return img_names

def load_image(path):
    image = cv2.imread(path)
    image = np.expand_dims(image,axis=0)
    return image

def save_image(path,img):
    cv2.imwrite(path,img)
    

def draw_bounding_box(img,detections,boxes,classes,class_map):
    img_height,img_width,_ = img.shape
    
    for i in detections:
        #Draw bounding box 
        ymin, xmin, ymax, xmax = boxes[i]
        
        xmin = int(xmin*img_width)
        xmax = int(xmax*img_width)
        ymin = int(ymin*img_height)
        ymax = int(ymax*img_height)
        
        cv2.rectangle(img,(xmin,ymin),(xmax,ymax),(0,0,255),thickness = 1)
        
        font = cv2.FONT_HERSHEY_SIMPLEX
        
        size = cv2.getTextSize(class_map[int(classes[i])],cv2.FONT_HERSHEY_SIMPLEX,0.5,1)

        text_width,text_height = size[0]
        
        #Background
        cv2.rectangle(img,(xmin,ymin),(xmin+text_width,ymin-text_height),(0,0,255),thickness = -1)
        
        #Foreground Text
        cv2.putText(img,class_map[int(classes[i])],(xmin,ymin), font, 0.5,(255,255,255),1,cv2.LINE_AA)


def get_class_map(class_map_file):
    with open(class_map_file,'r') as csv_file:
        class_id_list = csv_file.readlines()

    class_map = {}
    for id_name in class_id_list:
        id_,name = id_name.strip().split(",")
        class_map[int(id_)] = name
    
    return class_map

def get_detections(scores,threshold_score):
    detections = []

    for i,score in enumerate(scores):
        if score >= threshold_score:
            detections.append(i)

    return detections
