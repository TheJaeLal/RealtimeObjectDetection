import urllib.request
import cv2
import numpy as np

import tensorflow as tf
import os

import utils
import config

#Load the frozen inference graph..
detection_graph = tf.Graph()

with detection_graph.as_default():
    od_graph_def = tf.GraphDef()
    with tf.gfile.GFile(config.path_to_infer_graph,mode='rb') as graph_file:
        serialized_graph = graph_file.read()
        od_graph_def.ParseFromString(serialized_graph)
        tf.import_graph_def(od_graph_def)

#Get the class_label_map dict and list of image names in input directory..
class_map = utils.get_class_map(config.class_map_file)

# names = [n.name for n in sess.graph.as_graph_def().node]

with tf.Session(graph = detection_graph) as sess:
    frame_count = 0
    while True:

        with urllib.request.urlopen(config.stream_url) as url:
            img_response = url.read()
            #print(img_response)
            img_array = np.array(bytearray(img_response),dtype=np.uint8)
            test_img = cv2.imdecode(img_array,-1)
            frame_count +=1
            if frame_count%2==0:
                frame_count = 0
                cv2.imshow("Realtime Detection",test_img)
                continue

            test_img = np.expand_dims(test_img,axis=0)

            #Get access to the relevant input and output tensors
            input_ = sess.graph.get_tensor_by_name("import/image_tensor:0")
            boxes = sess.graph.get_tensor_by_name("import/detection_boxes:0")
            scores = sess.graph.get_tensor_by_name("import/detection_scores:0")
            classes = sess.graph.get_tensor_by_name("import/detection_classes:0")
            num_detections = sess.graph.get_tensor_by_name("import/num_detections:0")
            
            (boxes, scores, classes, num_detections) = sess.run( 
                            [boxes, scores, classes, num_detections],
                            feed_dict={input_: test_img})

            boxes = np.squeeze(boxes,axis=0)
            scores = np.squeeze(scores,axis=0)
            classes = np.squeeze(classes,axis=0)
            test_img = np.squeeze(test_img,axis=0)

            detections = utils.get_detections(scores,config.threshold_score)
            
            utils.draw_bounding_box(test_img,detections,boxes,classes,class_map)
            
            cv2.imshow("Realtime Detection",test_img)
            if ord('q') == cv2.waitKey(10):
                exit(0)