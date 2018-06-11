import tensorflow as tf

def load_labelmap(path):
  """Loads label map proto.

  Args:
    path: path to StringIntLabelMap proto text file.
  Returns:
    a StringIntLabelMapProto
  """
  with tf.gfile.GFile(path, 'r') as fid:
    label_map_string = fid.read()
    label_map = string_int_label_map_pb2.StringIntLabelMap()
    try:
      text_format.Merge(label_map_string, label_map)
    except text_format.ParseError:
      label_map.ParseFromString(label_map_string)
  return label_map


def get_label_map_dict(label_map_path):
  """Reads a label map and returns a dictionary of label names to id.

  Args:
    label_map_path: path to label_map.

  Returns:
    A dictionary mapping label names to id.
  """
  label_map = load_labelmap(label_map_path)
  label_map_dict = {}
  for item in label_map.item:
    label_map_dict[item.name] = item.id
  return label_map_dict